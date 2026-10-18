package com.dusrel.shrinkit.domain.engine

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.exifinterface.media.ExifInterface
import java.io.InputStream

data class DecodedBitmapResult(
    val bitmap: Bitmap,
    val width: Int,
    val height: Int,
    val mimeType: String
)

data class ImageBounds(
    val width: Int,
    val height: Int,
    val mimeType: String
)

object SafeBitmapDecoder {

    /**
     * Quickly inspect dimensions and MIME type without allocating pixel memory.
     */
    fun decodeBounds(context: Context, uri: Uri): ImageBounds {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }

        var width = options.outWidth
        var height = options.outHeight
        val mimeType = options.outMimeType ?: ""

        // Handle EXIF rotation for bounds
        if (width > 0 && height > 0) {
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val exif = ExifInterface(stream)
                    val orientation = exif.getAttributeInt(
                        ExifInterface.TAG_ORIENTATION,
                        ExifInterface.ORIENTATION_NORMAL
                    )
                    if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                        orientation == ExifInterface.ORIENTATION_ROTATE_270 ||
                        orientation == ExifInterface.ORIENTATION_TRANSPOSE ||
                        orientation == ExifInterface.ORIENTATION_TRANSVERSE
                    ) {
                        val temp = width
                        width = height
                        height = temp
                    }
                }
            } catch (_: Exception) {
                // Ignore EXIF read errors
            }
        }

        return ImageBounds(
            width = maxOf(0, width),
            height = maxOf(0, height),
            mimeType = mimeType
        )
    }

    /**
     * Decode full bitmap safely using Dual Engine (ImageDecoder on API 28+ with AVIF/HEIC guards;
     * BitmapFactory fallback on API 24–27 or on decoding failure).
     */
    fun decode(context: Context, uri: Uri): DecodedBitmapResult {
        val bounds = decodeBounds(context, uri)
        val mime = bounds.mimeType.lowercase()

        // Guard against AVIF on unsupported platforms (AVIF decoding added in API 31 / Android 12)
        val isAvif = mime.contains("avif")
        if (isAvif && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            throw UnsupportedOperationException("AVIF decoding requires Android 12 (API 31) or higher.")
        }

        // Dual Engine: Try ImageDecoder on API 28+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val isHeic = mime.contains("heic") || mime.contains("heif")
            val canAttemptImageDecoder = if (isHeic) {
                try {
                    ImageDecoder.isMimeTypeSupported("image/heif")
                } catch (_: Exception) {
                    true
                }
            } else {
                true
            }

            if (canAttemptImageDecoder) {
                try {
                    val source = ImageDecoder.createSource(context.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                    return DecodedBitmapResult(
                        bitmap = bitmap,
                        width = bitmap.width,
                        height = bitmap.height,
                        mimeType = bounds.mimeType
                    )
                } catch (e: Exception) {
                    // Fall through to BitmapFactory
                }
            }
        }

        // Fallback Engine: BitmapFactory
        val rawBitmap = decodeWithBitmapFactory(context, uri)
            ?: throw IllegalStateException("Failed to decode image from uri: $uri")

        // Bake EXIF orientation manually if BitmapFactory was used
        val orientedBitmap = applyExifOrientation(context, uri, rawBitmap)

        return DecodedBitmapResult(
            bitmap = orientedBitmap,
            width = orientedBitmap.width,
            height = orientedBitmap.height,
            mimeType = bounds.mimeType
        )
    }

    private fun decodeWithBitmapFactory(context: Context, uri: Uri): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inMutable = true
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        return context.contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, options)
        }
    }

    private fun applyExifOrientation(context: Context, uri: Uri, source: Bitmap): Bitmap {
        var orientation = ExifInterface.ORIENTATION_NORMAL
        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                )
            }
        } catch (_: Exception) {
            return source
        }

        if (orientation == ExifInterface.ORIENTATION_NORMAL || orientation == ExifInterface.ORIENTATION_UNDEFINED) {
            return source
        }

        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
        }

        return try {
            val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
            if (rotated != source) {
                source.recycle()
            }
            rotated
        } catch (_: Exception) {
            source
        }
    }
}
