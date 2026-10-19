package com.dusrel.shrinkit.presentation

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.dusrel.shrinkit.R
import com.dusrel.shrinkit.data.local.HistoryDao
import com.dusrel.shrinkit.data.local.ProcessedImageEntity
import com.dusrel.shrinkit.data.storage.MediaStoreExporter
import com.dusrel.shrinkit.domain.engine.BinarySearchCompressor
import com.dusrel.shrinkit.domain.engine.DimensionField
import com.dusrel.shrinkit.domain.engine.ExifSanitizer
import com.dusrel.shrinkit.domain.engine.Limits
import com.dusrel.shrinkit.domain.engine.ResizeEngine
import com.dusrel.shrinkit.domain.engine.SafeBitmapDecoder
import com.dusrel.shrinkit.domain.engine.SizeUnits
import com.dusrel.shrinkit.domain.model.BackgroundColor
import com.dusrel.shrinkit.domain.model.CompressMode
import com.dusrel.shrinkit.domain.model.ImageItem
import com.dusrel.shrinkit.domain.model.RelativeCropRect
import com.dusrel.shrinkit.domain.model.ImagePreset
import com.dusrel.shrinkit.domain.model.ProcessingOptions
import com.dusrel.shrinkit.domain.model.ProcessingStatus
import com.dusrel.shrinkit.domain.model.ResizeMode
import com.dusrel.shrinkit.domain.model.SizeUnit
import com.dusrel.shrinkit.domain.model.TargetFormat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject

data class ShrinkItUiState(
    val images: List<ImageItem> = emptyList(),
    val options: ProcessingOptions = ProcessingOptions(),
    val selectedPresetId: String? = null,
    val isProcessing: Boolean = false,
    val currentProgress: Int = 0,
    val totalProgress: Int = 0,
    val totalSavedBytes: Long = 0L,
    val userMessage: String? = null
)

@HiltViewModel
class ShrinkItViewModel @Inject constructor(
    private val application: Application,
    private val historyDao: HistoryDao
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ShrinkItUiState())
    val uiState: StateFlow<ShrinkItUiState> = _uiState.asStateFlow()

    fun addImages(uris: List<Uri>) {
        if (uris.isEmpty()) return

        viewModelScope.launch(Dispatchers.IO) {
            val currentCount = _uiState.value.images.size
            val availableSlots = Limits.MAX_BATCH - currentCount
            val urisToAdd = uris.take(availableSlots.coerceAtLeast(0))

            val newItems = mutableListOf<ImageItem>()
            var skippedTooLarge = 0

            for (uri in urisToAdd) {
                val bounds = SafeBitmapDecoder.decodeBounds(application, uri)

                // Enforce memory-safety limits: refuse images that would OOM on decode.
                if (Limits.checkInputDimensions(bounds.width, bounds.height) is LimitCheck.Error) {
                    skippedTooLarge++
                    continue
                }

                val fileSize = application.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: 0L

                val displayName = queryDisplayName(uri) ?: "image_${System.currentTimeMillis()}.jpg"

                newItems.add(
                    ImageItem(
                        id = UUID.randomUUID().toString(),
                        uri = uri,
                        filename = displayName,
                        originalSizeBytes = fileSize,
                        originalWidth = bounds.width,
                        originalHeight = bounds.height,
                        mimeType = bounds.mimeType
                    )
                )
            }

            _uiState.update { current ->
                val message = when {
                    skippedTooLarge > 0 -> application.getString(R.string.error_file_too_large)
                    uris.size > availableSlots -> application.getString(R.string.limit_max_batch, Limits.MAX_BATCH)
                    else -> null
                }
                current.copy(
                    images = current.images + newItems,
                    userMessage = message
                )
            }
        }
    }

    fun removeImage(id: String) {
        _uiState.update { current ->
            current.copy(images = current.images.filterNot { it.id == id })
        }
    }

    fun clearAllImages() {
        _uiState.update { it.copy(images = emptyList(), isProcessing = false) }
    }

    fun getImage(id: String): ImageItem? =
        _uiState.value.images.find { it.id == id }

    /** Cycles 0 -> 90 -> 180 -> 270 -> 0 degrees clockwise. */
    fun rotateImage(id: String) {
        _uiState.update { current ->
            current.copy(
                images = current.images.map { item ->
                    if (item.id == id) {
                        item.copy(rotationDegrees = (item.rotationDegrees + 90) % 360)
                    } else item
                }
            )
        }
    }

    fun setCropRect(id: String, rect: RelativeCropRect?) {
        _uiState.update { current ->
            current.copy(
                images = current.images.map { item ->
                    if (item.id == id) item.copy(cropRect = rect) else item
                }
            )
        }
    }

    /** Applies Settings defaults (quality/format) to the current options. */
    fun applyPreferenceDefaults(quality: Int, formatName: String) {
        val format = runCatching { TargetFormat.valueOf(formatName) }
            .getOrDefault(TargetFormat.ORIGINAL)
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(quality = quality, format = format)
            )
        }
    }

    fun applyPreset(preset: ImagePreset) {
        _uiState.update { current ->
            current.copy(
                options = preset.options,
                selectedPresetId = preset.id
            )
        }
    }

    fun updateWidth(newWidth: Float) {
        _uiState.update { current ->
            val opts = current.options
            val newHeight = if (opts.keepAspectRatio && current.images.isNotEmpty()) {
                val first = current.images.first()
                SizeUnits.calculateProportionalDimension(
                    changedField = DimensionField.WIDTH,
                    newValue = newWidth,
                    unit = opts.unit,
                    origW = first.originalWidth,
                    origH = first.originalHeight
                )
            } else {
                opts.height
            }
            current.copy(
                options = opts.copy(width = newWidth, height = newHeight),
                selectedPresetId = null
            )
        }
    }

    fun updateHeight(newHeight: Float) {
        _uiState.update { current ->
            val opts = current.options
            val newWidth = if (opts.keepAspectRatio && current.images.isNotEmpty()) {
                val first = current.images.first()
                SizeUnits.calculateProportionalDimension(
                    changedField = DimensionField.HEIGHT,
                    newValue = newHeight,
                    unit = opts.unit,
                    origW = first.originalWidth,
                    origH = first.originalHeight
                )
            } else {
                opts.width
            }
            current.copy(
                options = opts.copy(width = newWidth, height = newHeight),
                selectedPresetId = null
            )
        }
    }

    fun updateUnit(unit: SizeUnit) {
        _uiState.update { current ->
            val first = current.images.firstOrNull()
            val (defW, defH) = SizeUnits.getDefaultDimensionsForUnit(
                unit = unit,
                origW = first?.originalWidth ?: 1920,
                origH = first?.originalHeight ?: 1080,
                dpi = current.options.dpi
            )
            current.copy(
                options = current.options.copy(unit = unit, width = defW, height = defH),
                selectedPresetId = null
            )
        }
    }

    fun toggleKeepAspectRatio() {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(keepAspectRatio = !current.options.keepAspectRatio),
                selectedPresetId = null
            )
        }
    }

    fun updateResizeMode(mode: ResizeMode) {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(resizeMode = mode),
                selectedPresetId = null
            )
        }
    }

    fun updateFormat(format: TargetFormat) {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(format = format),
                selectedPresetId = null
            )
        }
    }

    fun updateCompressMode(mode: CompressMode) {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(compressMode = mode),
                selectedPresetId = null
            )
        }
    }

    fun updateQuality(quality: Int) {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(quality = quality),
                selectedPresetId = null
            )
        }
    }

    fun updateTargetKb(targetKb: Int) {
        _uiState.update { current ->
            current.copy(
                options = current.options.copy(targetKb = targetKb),
                selectedPresetId = null
            )
        }
    }

    fun updateBackgroundColor(bgColor: BackgroundColor) {
        _uiState.update { current ->
            current.copy(options = current.options.copy(backgroundColor = bgColor))
        }
    }

    fun toggleStripExif() {
        _uiState.update { current ->
            current.copy(options = current.options.copy(stripExif = !current.options.stripExif))
        }
    }

    fun dismissUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    /**
     * Executes the batch compression pipeline directly on Dispatchers.Default / IO
     * with real-time UI updates for each image.
     */
    fun processBatch() {
        val currentImages = _uiState.value.images
        if (currentImages.isEmpty() || _uiState.value.isProcessing) return

        val opts = _uiState.value.options

        _uiState.update {
            it.copy(
                isProcessing = true,
                currentProgress = 0,
                totalProgress = currentImages.size,
                totalSavedBytes = 0L
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            var cumulativeSavedBytes = 0L

            val updatedList = currentImages.toMutableList()

            for (index in updatedList.indices) {
                val item = updatedList[index]
                _uiState.update {
                    it.copy(
                        currentProgress = index + 1,
                        images = updatedList.mapIndexed { i, itm ->
                            if (i == index) itm.copy(status = ProcessingStatus.PROCESSING) else itm
                        }
                    )
                }

                try {
                    val decoded = SafeBitmapDecoder.decode(application, item.uri)
                    val (targetW, targetH) = SizeUnits.toPixels(
                        widthVal = opts.width,
                        heightVal = opts.height,
                        unit = opts.unit,
                        dpi = opts.dpi,
                        origW = decoded.width,
                        origH = decoded.height
                    )

                    // Enforce output memory-safety limits before allocating bitmaps.
                    if (Limits.checkOutputDimensions(targetW, targetH) is LimitCheck.Error) {
                        decoded.bitmap.recycle()
                        throw IllegalArgumentException(
                            application.getString(R.string.error_file_too_large)
                        )
                    }

                    // Apply per-image edits: custom crop first, then rotation.
                    var workBitmap = decoded.bitmap
                    item.cropRect?.let { rect ->
                        workBitmap = applyRelativeCrop(workBitmap, rect) ?: workBitmap
                    }
                    if (item.rotationDegrees != 0) {
                        workBitmap = applyRotation(workBitmap, item.rotationDegrees)
                    }

                    val format = if (opts.format == TargetFormat.ORIGINAL) {
                        when {
                            decoded.mimeType.contains("png") -> TargetFormat.PNG
                            decoded.mimeType.contains("webp") -> TargetFormat.WEBP
                            decoded.mimeType.contains("avif") -> TargetFormat.AVIF
                            else -> TargetFormat.JPEG
                        }
                    } else {
                        opts.format
                    }.encodeSafe()

                    val resized = ResizeEngine.resize(
                        source = workBitmap,
                        targetWidth = targetW,
                        targetHeight = targetH,
                        mode = opts.resizeMode,
                        backgroundColor = opts.backgroundColor,
                        keepAlpha = format.supportsAlpha
                    )

                    val compressionResult = if (opts.compressMode == CompressMode.TARGET_KB) {
                        BinarySearchCompressor.compressToTargetKb(resized, format, opts.targetKb)
                    } else {
                        BinarySearchCompressor.compressWithQuality(resized, format, opts.quality)
                    }

                    if (resized != workBitmap) {
                        resized.recycle()
                    }
                    workBitmap.recycle()

                    val baseName = item.filename.substringBeforeLast(".")
                    val outFileName = "shrinkit_${baseName}.${format.extension}"

                    // Save to Pictures/ShrinkIt in MediaStore
                    val savedUri = MediaStoreExporter.saveToPictures(
                        context = application,
                        data = compressionResult.data,
                        filename = outFileName,
                        mimeType = format.mimeType
                    )

                    // Save local cached file for quick preview / share intent
                    val cachedFile = MediaStoreExporter.saveToInternalCache(
                        context = application,
                        data = compressionResult.data,
                        filename = outFileName
                    )

                    if (opts.stripExif && format == TargetFormat.JPEG) {
                        ExifSanitizer.sanitize(cachedFile)
                    }

                    val outBytes = compressionResult.data.size.toLong()
                    val savedBytes = (item.originalSizeBytes - outBytes).coerceAtLeast(0L)
                    cumulativeSavedBytes += savedBytes

                    val reduction = if (item.originalSizeBytes > 0) {
                        (((item.originalSizeBytes - outBytes).toDouble() / item.originalSizeBytes.toDouble()) * 100.0).toInt()
                    } else 0

                    // Record in History (best effort — never fails the batch)
                    try {
                        historyDao.insert(
                            ProcessedImageEntity(
                                filename = outFileName,
                                contentUri = savedUri.toString(),
                                originalBytes = item.originalSizeBytes,
                                processedBytes = outBytes,
                                width = compressionResult.finalWidth,
                                height = compressionResult.finalHeight,
                                format = format.name,
                                createdAt = System.currentTimeMillis()
                            )
                        )
                    } catch (_: Exception) {
                    }

                    updatedList[index] = item.copy(
                        status = ProcessingStatus.COMPLETED,
                        processedUri = savedUri,
                        processedSizeBytes = outBytes,
                        processedWidth = compressionResult.finalWidth,
                        processedHeight = compressionResult.finalHeight,
                        reductionPercent = reduction
                    )
                } catch (e: Exception) {
                    updatedList[index] = item.copy(
                        status = ProcessingStatus.ERROR,
                        errorMessage = e.localizedMessage ?: "Processing failed"
                    )
                }

                _uiState.update {
                    it.copy(
                        images = updatedList.toList(),
                        totalSavedBytes = cumulativeSavedBytes
                    )
                }
            }

            _uiState.update {
                it.copy(
                    isProcessing = false,
                    userMessage = application.getString(R.string.status_done)
                )
            }
        }
    }

    /**
     * Crops [source] to the relative rect (fractions 0..1). Returns null if the
     * rect is degenerate, leaving the source untouched. Recycles [source] when
     * a new bitmap is produced.
     */
    private fun applyRelativeCrop(source: Bitmap, rect: RelativeCropRect): Bitmap? {
        val w = source.width
        val h = source.height
        if (w <= 0 || h <= 0) return null

        val left = (rect.left.coerceIn(0f, 1f) * w).toInt()
        val top = (rect.top.coerceIn(0f, 1f) * h).toInt()
        val right = (rect.right.coerceIn(0f, 1f) * w).toInt()
        val bottom = (rect.bottom.coerceIn(0f, 1f) * h).toInt()

        val cw = (right - left).coerceIn(1, w - left)
        val ch = (bottom - top).coerceIn(1, h - top)
        if (cw >= w && ch >= h) return null // effectively the whole image

        return try {
            val cropped = Bitmap.createBitmap(source, left, top, cw, ch)
            if (cropped != source) source.recycle()
            cropped
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Rotates [source] clockwise by [degrees] (90/180/270). Recycles [source]
     * when a new bitmap is produced.
     */
    private fun applyRotation(source: Bitmap, degrees: Int): Bitmap {
        if (degrees % 360 == 0) return source
        return try {
            val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
            val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            if (rotated != source) source.recycle()
            rotated
        } catch (_: Exception) {
            source
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            application.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex != -1 && cursor.moveToFirst()) {
                    cursor.getString(nameIndex)
                } else null
            }
        } catch (_: Exception) {
            null
        }
    }
}
