package com.dusrel.shrinkit.domain.model

import android.net.Uri
import androidx.compose.runtime.Immutable

/**
 * Crop rectangle expressed as fractions (0..1) of the source bitmap.
 * Set by the crop editor; null means "no custom crop".
 */
@Immutable
data class RelativeCropRect(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float
)

@Immutable
data class ImageItem(
    val id: String,
    val uri: Uri,
    val filename: String,
    val originalSizeBytes: Long,
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val mimeType: String = "",
    val status: ProcessingStatus = ProcessingStatus.IDLE,
    val processedUri: Uri? = null,
    val processedSizeBytes: Long = 0L,
    val processedWidth: Int = 0,
    val processedHeight: Int = 0,
    val reductionPercent: Int = 0,
    val errorMessage: String? = null,
    /** Clockwise rotation to apply before resize: 0, 90, 180 or 270. */
    val rotationDegrees: Int = 0,
    /** Custom crop from the crop editor; null = no custom crop. */
    val cropRect: RelativeCropRect? = null
) {
    /**
     * Dimensions after the custom crop (if any). Used for aspect-lock math:
     * after a crop, width/height edits must preserve the CROPPED ratio,
     * not the original bitmap ratio.
     */
    val effectiveWidth: Int
        get() = cropRect?.let { ((it.right - it.left) * originalWidth).toInt().coerceAtLeast(1) }
            ?: originalWidth
    val effectiveHeight: Int
        get() = cropRect?.let { ((it.bottom - it.top) * originalHeight).toInt().coerceAtLeast(1) }
            ?: originalHeight
}
