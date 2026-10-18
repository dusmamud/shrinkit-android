package com.dusrel.shrinkit.domain.engine

import com.dusrel.shrinkit.domain.model.SizeUnit
import kotlin.math.max
import kotlin.math.roundToInt

data class CropRect(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

enum class DimensionField {
    WIDTH,
    HEIGHT
}

object SizeUnits {
    /** Centimeters per inch — exact international standard */
    const val CM_PER_INCH: Double = 2.54

    /** Round double to two decimal places */
    fun roundToTwoDecimals(num: Double): Double {
        return (num * 100.0).roundToInt() / 100.0
    }

    /**
     * Default width/height values when switching units
     */
    fun getDefaultDimensionsForUnit(
        unit: SizeUnit,
        origW: Int,
        origH: Int,
        dpi: Int = 72
    ): Pair<Float, Float> {
        val effectiveDpi = if (dpi > 0) dpi else 72
        return when (unit) {
            SizeUnit.PERCENT -> Pair(70f, 70f)
            SizeUnit.PIXELS -> Pair(max(1, origW).toFloat(), max(1, origH).toFloat())
            SizeUnit.CM -> {
                val dotsPerCm = effectiveDpi / CM_PER_INCH
                val w = roundToTwoDecimals(origW / dotsPerCm).toFloat()
                val h = roundToTwoDecimals(origH / dotsPerCm).toFloat()
                Pair(w, h)
            }
            SizeUnit.INCHES -> {
                val w = roundToTwoDecimals(origW.toDouble() / effectiveDpi).toFloat()
                val h = roundToTwoDecimals(origH.toDouble() / effectiveDpi).toFloat()
                Pair(w, h)
            }
        }
    }

    /**
     * Proportional dimension calculation when aspect ratio is locked
     */
    fun calculateProportionalDimension(
        changedField: DimensionField,
        newValue: Float,
        unit: SizeUnit,
        origW: Int,
        origH: Int
    ): Float {
        if (origW <= 0 || origH <= 0) return newValue
        if (unit == SizeUnit.PERCENT) return newValue

        return if (unit == SizeUnit.PIXELS) {
            if (changedField == DimensionField.WIDTH) {
                max(1, ((origH.toLong() * newValue.toLong()) / origW).toInt()).toFloat()
            } else {
                max(1, ((origW.toLong() * newValue.toLong()) / origH).toInt()).toFloat()
            }
        } else {
            val ratio = if (changedField == DimensionField.WIDTH) {
                (origH.toDouble() * newValue.toDouble()) / origW.toDouble()
            } else {
                (origW.toDouble() * newValue.toDouble()) / origH.toDouble()
            }
            max(0.01, roundToTwoDecimals(ratio)).toFloat()
        }
    }

    /**
     * Convert user-entered width/height into target pixels
     */
    fun toPixels(
        widthVal: Float,
        heightVal: Float,
        unit: SizeUnit,
        dpi: Int,
        origW: Int,
        origH: Int
    ): Pair<Int, Int> {
        require(widthVal > 0f && heightVal > 0f) { "Width and height must be greater than zero." }
        require(origW > 0 && origH > 0) { "Original image dimensions are invalid." }

        val effectiveDpi = if (dpi > 0) dpi else 72
        val (w, h) = when (unit) {
            SizeUnit.PERCENT -> {
                val pw = (origW * widthVal) / 100f
                val ph = (origH * heightVal) / 100f
                Pair(pw, ph)
            }
            SizeUnit.PIXELS -> Pair(widthVal, heightVal)
            SizeUnit.INCHES -> {
                val pw = widthVal * effectiveDpi
                val ph = heightVal * effectiveDpi
                Pair(pw, ph)
            }
            SizeUnit.CM -> {
                val pw = (widthVal / CM_PER_INCH.toFloat()) * effectiveDpi
                val ph = (heightVal / CM_PER_INCH.toFloat()) * effectiveDpi
                Pair(pw, ph)
            }
        }

        val targetW = max(1, w.roundToInt())
        val targetH = max(1, h.roundToInt())
        return Pair(targetW, targetH)
    }

    /**
     * Center-crop rectangle of source matching target aspect ratio
     */
    fun cropRect(srcW: Int, srcH: Int, targetW: Int, targetH: Int): CropRect {
        require(srcW > 0 && srcH > 0 && targetW > 0 && targetH > 0) {
            "Dimensions must be greater than zero."
        }
        val targetAspect = targetW.toDouble() / targetH.toDouble()
        val srcAspect = srcW.toDouble() / srcH.toDouble()

        val (w, h) = if (srcAspect > targetAspect) {
            Pair(srcH * targetAspect, srcH.toDouble())
        } else {
            Pair(srcW.toDouble(), srcW / targetAspect)
        }

        val width = max(1, w.roundToInt())
        val height = max(1, h.roundToInt())
        val x = max(0, ((srcW - width) / 2))
        val y = max(0, ((srcH - height) / 2))

        return CropRect(x = x, y = y, width = width, height = height)
    }

    /**
     * Largest dimensions fitting INSIDE the target box preserving aspect ratio
     */
    fun fitDims(srcW: Int, srcH: Int, targetW: Int, targetH: Int): Pair<Int, Int> {
        require(srcW > 0 && srcH > 0 && targetW > 0 && targetH > 0) {
            "Dimensions must be greater than zero."
        }
        val scale = minOf(targetW.toDouble() / srcW.toDouble(), targetH.toDouble() / srcH.toDouble())
        val fittedW = max(1, (srcW * scale).roundToInt())
        val fittedH = max(1, (srcH * scale).roundToInt())
        return Pair(fittedW, fittedH)
    }

    /**
     * Percentage calculation for UI badges
     */
    fun percentOf(target: Long, original: Long): Int {
        if (original <= 0L) return 0
        return ((target.toDouble() / original.toDouble()) * 100.0).roundToInt()
    }
}
