package com.dusrel.shrinkit.domain.engine

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import com.dusrel.shrinkit.domain.model.BackgroundColor
import com.dusrel.shrinkit.domain.model.ResizeMode

object ResizeEngine {

    /**
     * Resizes and composites a bitmap according to the requested ResizeMode and target dimensions.
     */
    fun resize(
        source: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        mode: ResizeMode,
        backgroundColor: BackgroundColor = BackgroundColor.WHITE,
        keepAlpha: Boolean = true
    ): Bitmap {
        require(targetWidth > 0 && targetHeight > 0) { "Target dimensions must be > 0" }

        val srcW = source.width
        val srcH = source.height

        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)

        return when (mode) {
            ResizeMode.STRETCH -> {
                if (srcW == targetWidth && srcH == targetHeight && keepAlpha) {
                    source
                } else {
                    val scaled = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, true)
                    if (!keepAlpha) {
                        flattenOnBackground(scaled, targetWidth, targetHeight, backgroundColor, paint)
                    } else {
                        scaled
                    }
                }
            }

            ResizeMode.CROP -> {
                val crop = SizeUnits.cropRect(srcW, srcH, targetWidth, targetHeight)
                val cropped = Bitmap.createBitmap(source, crop.x, crop.y, crop.width, crop.height)
                val scaled = if (cropped.width == targetWidth && cropped.height == targetHeight) {
                    cropped
                } else {
                    val s = Bitmap.createScaledBitmap(cropped, targetWidth, targetHeight, true)
                    if (cropped != source) cropped.recycle()
                    s
                }

                if (!keepAlpha) {
                    flattenOnBackground(scaled, targetWidth, targetHeight, backgroundColor, paint)
                } else {
                    scaled
                }
            }

            ResizeMode.FIT -> {
                val (fitW, fitH) = SizeUnits.fitDims(srcW, srcH, targetWidth, targetHeight)
                val scaled = Bitmap.createScaledBitmap(source, fitW, fitH, true)

                val output = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(output)

                // Fill background
                if (!keepAlpha) {
                    canvas.drawColor(backgroundColor.argb)
                } else {
                    canvas.drawColor(Color.TRANSPARENT)
                }

                // Center the fitted image
                val dx = (targetWidth - fitW) / 2f
                val dy = (targetHeight - fitH) / 2f
                canvas.drawBitmap(scaled, dx, dy, paint)

                if (scaled != source) {
                    scaled.recycle()
                }

                output
            }
        }
    }

    private fun flattenOnBackground(
        source: Bitmap,
        width: Int,
        height: Int,
        backgroundColor: BackgroundColor,
        paint: Paint
    ): Bitmap {
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        canvas.drawColor(backgroundColor.argb)
        canvas.drawBitmap(source, 0f, 0f, paint)
        if (source != output) {
            source.recycle()
        }
        return output
    }
}
