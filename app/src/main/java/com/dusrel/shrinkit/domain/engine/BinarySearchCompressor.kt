package com.dusrel.shrinkit.domain.engine

import android.graphics.Bitmap
import android.os.Build
import com.dusrel.shrinkit.domain.model.TargetFormat
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

data class CompressionResult(
    val data: ByteArray,
    val finalWidth: Int,
    val finalHeight: Int,
    val achievedQuality: Int,
    val format: TargetFormat
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as CompressionResult
        return data.contentEquals(other.data) &&
                finalWidth == other.finalWidth &&
                finalHeight == other.finalHeight &&
                achievedQuality == other.achievedQuality &&
                format == other.format
    }

    override fun hashCode(): Int {
        var result = data.contentHashCode()
        result = 31 * result + finalWidth
        result = 31 * result + finalHeight
        result = 31 * result + achievedQuality
        result = 31 * result + format.hashCode()
        return result
    }
}

object BinarySearchCompressor {

    /**
     * Resolves the appropriate Android CompressFormat
     */
    fun getCompressFormat(format: TargetFormat): Bitmap.CompressFormat {
        return when (format) {
            TargetFormat.JPEG -> Bitmap.CompressFormat.JPEG
            TargetFormat.PNG -> Bitmap.CompressFormat.PNG
            TargetFormat.WEBP -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    @Suppress("DEPRECATION")
                    Bitmap.CompressFormat.WEBP
                }
            }
            TargetFormat.AVIF -> {
                // Android's Bitmap.compress() has NO AVIF encoder (Bitmap.CompressFormat
                // only defines JPEG/PNG/WEBP/WEBP_LOSSY/WEBP_LOSSLESS). AVIF output would
                // require a custom MediaCodec route, so fall back to the best available.
                // Callers must map TargetFormat.AVIF through encodeSafe() first so the
                // file extension and MIME type stay truthful.
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Bitmap.CompressFormat.WEBP_LOSSY
                } else {
                    Bitmap.CompressFormat.JPEG
                }
            }
            TargetFormat.ORIGINAL -> Bitmap.CompressFormat.JPEG
        }
    }

    /**
     * Compress bitmap with a fixed quality percentage (1..100)
     */
    fun compressWithQuality(
        bitmap: Bitmap,
        format: TargetFormat,
        quality: Int
    ): CompressionResult {
        val clampedQuality = quality.coerceIn(1, 100)
        val compressFormat = getCompressFormat(format)
        val stream = ByteArrayOutputStream()
        bitmap.compress(compressFormat, clampedQuality, stream)
        val bytes = stream.toByteArray()
        return CompressionResult(
            data = bytes,
            finalWidth = bitmap.width,
            finalHeight = bitmap.height,
            achievedQuality = clampedQuality,
            format = format
        )
    }

    /**
     * Binary search compression to converge within a requested Target KB budget.
     * If the target is unachievable even at quality = 1 (e.g. 20 KB requested for a 4K image),
     * downscales the bitmap dimensions proportionally until the target budget is satisfied.
     */
    fun compressToTargetKb(
        initialBitmap: Bitmap,
        format: TargetFormat,
        targetKb: Int
    ): CompressionResult {
        require(targetKb > 0) { "Target KB must be greater than zero." }
        val targetBytes = targetKb * 1024L
        val compressFormat = getCompressFormat(format)

        // PNG is lossless and cannot be quantized via quality parameter
        if (format == TargetFormat.PNG) {
            return compressPngToTargetKb(initialBitmap, targetBytes)
        }

        var currentBitmap = initialBitmap
        var isCloned = false

        try {
            // Downscale loop if needed
            for (attempt in 0 until 5) {
                var low = 1
                var high = 100
                var bestQuality = 1
                var bestBytes: ByteArray? = null

                while (low <= high) {
                    val mid = (low + high) / 2
                    val stream = ByteArrayOutputStream()
                    currentBitmap.compress(compressFormat, mid, stream)
                    val candidate = stream.toByteArray()

                    if (candidate.size <= targetBytes) {
                        bestQuality = mid
                        bestBytes = candidate
                        // Try higher quality
                        low = mid + 1
                    } else {
                        // Exceeded target, try lower quality
                        high = mid - 1
                    }
                }

                if (bestBytes != null) {
                    return CompressionResult(
                        data = bestBytes,
                        finalWidth = currentBitmap.width,
                        finalHeight = currentBitmap.height,
                        achievedQuality = bestQuality,
                        format = format
                    )
                }

                // If even quality = 1 exceeded budget, downscale dimensions by 25% and retry
                val newW = (currentBitmap.width * 0.75f).roundToInt().coerceAtLeast(10)
                val newH = (currentBitmap.height * 0.75f).roundToInt().coerceAtLeast(10)
                val scaled = Bitmap.createScaledBitmap(currentBitmap, newW, newH, true)

                if (isCloned) {
                    currentBitmap.recycle()
                }
                currentBitmap = scaled
                isCloned = true
            }

            // Fallback: Return best effort at quality 1
            val fallbackStream = ByteArrayOutputStream()
            currentBitmap.compress(compressFormat, 1, fallbackStream)
            return CompressionResult(
                data = fallbackStream.toByteArray(),
                finalWidth = currentBitmap.width,
                finalHeight = currentBitmap.height,
                achievedQuality = 1,
                format = format
            )
        } finally {
            if (isCloned && currentBitmap != initialBitmap) {
                currentBitmap.recycle()
            }
        }
    }

    private fun compressPngToTargetKb(initialBitmap: Bitmap, targetBytes: Long): CompressionResult {
        var currentBitmap = initialBitmap
        var isCloned = false

        try {
            for (attempt in 0 until 6) {
                val stream = ByteArrayOutputStream()
                currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val bytes = stream.toByteArray()

                if (bytes.size <= targetBytes || attempt == 5) {
                    return CompressionResult(
                        data = bytes,
                        finalWidth = currentBitmap.width,
                        finalHeight = currentBitmap.height,
                        achievedQuality = 100,
                        format = TargetFormat.PNG
                    )
                }

                val newW = (currentBitmap.width * 0.75f).roundToInt().coerceAtLeast(10)
                val newH = (currentBitmap.height * 0.75f).roundToInt().coerceAtLeast(10)
                val scaled = Bitmap.createScaledBitmap(currentBitmap, newW, newH, true)

                if (isCloned) {
                    currentBitmap.recycle()
                }
                currentBitmap = scaled
                isCloned = true
            }

            val fallbackStream = ByteArrayOutputStream()
            currentBitmap.compress(Bitmap.CompressFormat.PNG, 100, fallbackStream)
            return CompressionResult(
                data = fallbackStream.toByteArray(),
                finalWidth = currentBitmap.width,
                finalHeight = currentBitmap.height,
                achievedQuality = 100,
                format = TargetFormat.PNG
            )
        } finally {
            if (isCloned && currentBitmap != initialBitmap) {
                currentBitmap.recycle()
            }
        }
    }
}
