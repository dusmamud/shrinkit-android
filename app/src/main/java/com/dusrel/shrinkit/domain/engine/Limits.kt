package com.dusrel.shrinkit.domain.engine

import com.dusrel.shrinkit.domain.model.SizeUnit

sealed class LimitCheck {
    data class Ok(val warn: Boolean) : LimitCheck()
    data class Error(val reason: LimitErrorReason) : LimitCheck()
}

/** Per-unit W/H input range (web parity: limits.ts). */
data class UnitLimit(val min: Float, val max: Float, val suffix: String)

enum class LimitErrorReason {
    TOO_LARGE,
    INVALID_DIMENSIONS
}

object Limits {
    /** Refuse images larger than 120 megapixels — memory safety */
    const val MAX_INPUT_MEGAPIXELS = 120

    /** Warn above 40 megapixels */
    const val WARN_INPUT_MEGAPIXELS = 40

    /** Largest single side allowed to decode, in pixels */
    const val MAX_INPUT_SIDE = 16000

    /** Largest output side allowed to render, in pixels */
    const val MAX_OUTPUT_SIDE = 12000

    /** Maximum images in a single batch */
    const val MAX_BATCH = 50

    /** W/H input limits per unit (web parity: limits.ts) */
    val UNIT_LIMITS: Map<SizeUnit, UnitLimit> = mapOf(
        SizeUnit.PERCENT to UnitLimit(1f, 500f, "%"),
        SizeUnit.PIXELS to UnitLimit(1f, 12000f, "px"),
        SizeUnit.CM to UnitLimit(0.1f, 400f, "cm"),
        SizeUnit.INCHES to UnitLimit(0.1f, 160f, "in")
    )

    /** Target-size tools accept 1..2000 KB (web parity) */
    const val TARGET_KB_MIN = 1

    /** Largest target size the KB tools accept */
    const val TARGET_KB_MAX = 2000

    fun checkInputDimensions(width: Int, height: Int): LimitCheck {
        if (width <= 0 || height <= 0) {
            return LimitCheck.Error(LimitErrorReason.INVALID_DIMENSIONS)
        }
        if (width > MAX_INPUT_SIDE || height > MAX_INPUT_SIDE) {
            return LimitCheck.Error(LimitErrorReason.TOO_LARGE)
        }
        val megapixels = (width.toLong() * height.toLong()) / 1_000_000L
        if (megapixels > MAX_INPUT_MEGAPIXELS) {
            return LimitCheck.Error(LimitErrorReason.TOO_LARGE)
        }
        return LimitCheck.Ok(warn = megapixels > WARN_INPUT_MEGAPIXELS)
    }

    fun checkOutputDimensions(width: Int, height: Int): LimitCheck {
        if (width < 1 || height < 1) {
            return LimitCheck.Error(LimitErrorReason.INVALID_DIMENSIONS)
        }
        if (width > MAX_OUTPUT_SIDE || height > MAX_OUTPUT_SIDE) {
            return LimitCheck.Error(LimitErrorReason.TOO_LARGE)
        }
        return LimitCheck.Ok(warn = false)
    }
}
