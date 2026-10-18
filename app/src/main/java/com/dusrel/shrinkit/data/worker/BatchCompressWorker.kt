package com.dusrel.shrinkit.data.worker

import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.dusrel.shrinkit.data.storage.MediaStoreExporter
import com.dusrel.shrinkit.domain.engine.BinarySearchCompressor
import com.dusrel.shrinkit.domain.engine.ExifSanitizer
import com.dusrel.shrinkit.domain.engine.ResizeEngine
import com.dusrel.shrinkit.domain.engine.SafeBitmapDecoder
import com.dusrel.shrinkit.domain.engine.SizeUnits
import com.dusrel.shrinkit.domain.model.BackgroundColor
import com.dusrel.shrinkit.domain.model.CompressMode
import com.dusrel.shrinkit.domain.model.ResizeMode
import com.dusrel.shrinkit.domain.model.SizeUnit
import com.dusrel.shrinkit.domain.model.TargetFormat
import com.dusrel.shrinkit.presentation.util.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.text.DecimalFormat

@HiltWorker
class BatchCompressWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    companion object {
        const val KEY_IMAGE_URIS = "key_image_uris"
        const val KEY_WIDTH = "key_width"
        const val KEY_HEIGHT = "key_height"
        const val KEY_UNIT = "key_unit"
        const val KEY_RESIZE_MODE = "key_resize_mode"
        const val KEY_FORMAT = "key_format"
        const val KEY_COMPRESS_MODE = "key_compress_mode"
        const val KEY_QUALITY = "key_quality"
        const val KEY_TARGET_KB = "key_target_kb"
        const val KEY_DPI = "key_dpi"
        const val KEY_BACKGROUND_COLOR = "key_bg_color"
        const val KEY_STRIP_EXIF = "key_strip_exif"
        const val KEY_KEEP_ASPECT = "key_keep_aspect"

        const val PROGRESS_CURRENT = "progress_current"
        const val PROGRESS_TOTAL = "progress_total"
        const val RESULT_SUCCESS_COUNT = "result_success_count"
        const val RESULT_TOTAL_SAVED_BYTES = "result_total_saved_bytes"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val uriStrings = inputData.getStringArray(KEY_IMAGE_URIS) ?: return@withContext Result.failure()
        if (uriStrings.isEmpty()) return@withContext Result.success()

        val width = inputData.getFloat(KEY_WIDTH, 70f)
        val height = inputData.getFloat(KEY_HEIGHT, 70f)
        val unit = SizeUnit.fromId(inputData.getString(KEY_UNIT) ?: SizeUnit.PERCENT.id)
        val resizeMode = ResizeMode.valueOf(inputData.getString(KEY_RESIZE_MODE) ?: ResizeMode.STRETCH.name)
        val targetFormat = TargetFormat.valueOf(inputData.getString(KEY_FORMAT) ?: TargetFormat.ORIGINAL.name)
        val compressMode = CompressMode.valueOf(inputData.getString(KEY_COMPRESS_MODE) ?: CompressMode.QUALITY.name)
        val quality = inputData.getInt(KEY_QUALITY, 85)
        val targetKb = inputData.getInt(KEY_TARGET_KB, 200)
        val dpi = inputData.getInt(KEY_DPI, 72)
        val bgColor = BackgroundColor.valueOf(inputData.getString(KEY_BACKGROUND_COLOR) ?: BackgroundColor.WHITE.name)
        val stripExif = inputData.getBoolean(KEY_STRIP_EXIF, true)

        val total = uriStrings.size
        var successCount = 0
        var totalSavedBytes = 0L

        // Set initial foreground service info for Android 14+ dataSync
        setForeground(createForegroundInfo(0, total, uriStrings.firstOrNull() ?: ""))

        for (index in uriStrings.indices) {
            val uriString = uriStrings[index]
            val uri = android.net.Uri.parse(uriString)
            val filename = uri.lastPathSegment ?: "image_${index + 1}.jpg"

            // Update foreground notification and worker progress
            setProgress(workDataOf(PROGRESS_CURRENT to (index + 1), PROGRESS_TOTAL to total))
            setForeground(createForegroundInfo(index + 1, total, filename))

            try {
                // 1. Decode using Dual Engine
                val decoded = SafeBitmapDecoder.decode(appContext, uri)
                val origW = decoded.width
                val origH = decoded.height

                // Query original file size
                val origBytes = appContext.contentResolver.openFileDescriptor(uri, "r")?.use {
                    it.statSize
                } ?: 0L

                // 2. Compute Target Pixel Dimensions
                val (targetW, targetH) = SizeUnits.toPixels(width, height, unit, dpi, origW, origH)

                // 3. Resolve Target Format
                val format = if (targetFormat == TargetFormat.ORIGINAL) {
                    when {
                        decoded.mimeType.contains("png") -> TargetFormat.PNG
                        decoded.mimeType.contains("webp") -> TargetFormat.WEBP
                        decoded.mimeType.contains("avif") -> TargetFormat.AVIF
                        else -> TargetFormat.JPEG
                    }
                } else {
                    targetFormat
                }

                // 4. Resize and Composite
                val resizedBitmap = ResizeEngine.resize(
                    source = decoded.bitmap,
                    targetWidth = targetW,
                    targetHeight = targetH,
                    mode = resizeMode,
                    backgroundColor = bgColor,
                    keepAlpha = format.supportsAlpha
                )

                // 5. Compress (Quality or Binary Search Target KB)
                val compressionResult = if (compressMode == CompressMode.TARGET_KB) {
                    BinarySearchCompressor.compressToTargetKb(resizedBitmap, format, targetKb)
                } else {
                    BinarySearchCompressor.compressWithQuality(resizedBitmap, format, quality)
                }

                if (resizedBitmap != decoded.bitmap) {
                    resizedBitmap.recycle()
                }
                decoded.bitmap.recycle()

                // 6. Name and Write to Public Storage
                val baseName = filename.substringBeforeLast(".")
                val outFileName = "shrinkit_${baseName}.${format.extension}"

                // Save to MediaStore (Pictures/ShrinkIt)
                val savedUri = MediaStoreExporter.saveToPictures(
                    context = appContext,
                    data = compressionResult.data,
                    filename = outFileName,
                    mimeType = format.mimeType
                )

                // Save internal cached copy for preview / share
                val cachedFile = MediaStoreExporter.saveToInternalCache(
                    context = appContext,
                    data = compressionResult.data,
                    filename = outFileName
                )

                // 7. Sanitize EXIF if requested
                if (stripExif && format == TargetFormat.JPEG) {
                    ExifSanitizer.sanitize(cachedFile)
                }

                val outputSize = compressionResult.data.size.toLong()
                if (origBytes > outputSize) {
                    totalSavedBytes += (origBytes - outputSize)
                }
                successCount++

            } catch (e: Exception) {
                // Continue with next images in batch
            }
        }

        // Show batch completion notification
        val formattedSavings = formatFileSize(totalSavedBytes)
        NotificationHelper.showCompletionNotification(appContext, successCount, formattedSavings)

        Result.success(
            workDataOf(
                RESULT_SUCCESS_COUNT to successCount,
                RESULT_TOTAL_SAVED_BYTES to totalSavedBytes
            )
        )
    }

    private fun createForegroundInfo(current: Int, total: Int, currentFilename: String): ForegroundInfo {
        val notification = NotificationHelper.createProcessingNotification(
            appContext,
            current,
            total,
            currentFilename
        )
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(
                NotificationHelper.PROCESSING_NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            ForegroundInfo(NotificationHelper.PROCESSING_NOTIFICATION_ID, notification)
        }
    }

    private fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 KB"
        val df = DecimalFormat("#.##")
        return when {
            bytes >= 1024 * 1024 -> "${df.format(bytes / (1024.0 * 1024.0))} MB"
            else -> "${df.format(bytes / 1024.0)} KB"
        }
    }
}
