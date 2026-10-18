package com.dusrel.shrinkit.domain.engine

sealed class LimitCheck {
    data class Ok(val warn: Boolean) : LimitCheck()
    data class Error(val reason: LimitErrorReason) : LimitCheck()
}

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
