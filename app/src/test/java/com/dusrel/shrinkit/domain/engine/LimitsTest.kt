package com.dusrel.shrinkit.domain.engine

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LimitsTest {

    @Test
    fun testCheckInputDimensions() {
        // Normal 1920x1080 (approx 2 MP)
        val normal = Limits.checkInputDimensions(1920, 1080)
        assertTrue(normal is LimitCheck.Ok)
        assertEquals(false, (normal as LimitCheck.Ok).warn)

        // Large 8000x6000 (48 MP > 40 MP warning threshold)
        val large = Limits.checkInputDimensions(8000, 6000)
        assertTrue(large is LimitCheck.Ok)
        assertEquals(true, (large as LimitCheck.Ok).warn)

        // Huge > 120 MP
        val huge = Limits.checkInputDimensions(15000, 10000) // 150 MP
        assertTrue(huge is LimitCheck.Error)
        assertEquals(LimitErrorReason.TOO_LARGE, (huge as LimitCheck.Error).reason)

        // Single side > 16000
        val sideTooBig = Limits.checkInputDimensions(16001, 100)
        assertTrue(sideTooBig is LimitCheck.Error)
        assertEquals(LimitErrorReason.TOO_LARGE, (sideTooBig as LimitCheck.Error).reason)

        // Invalid dimensions <= 0
        val invalid = Limits.checkInputDimensions(0, 100)
        assertTrue(invalid is LimitCheck.Error)
        assertEquals(LimitErrorReason.INVALID_DIMENSIONS, (invalid as LimitCheck.Error).reason)
    }

    @Test
    fun testCheckOutputDimensions() {
        // Valid 1080x1080
        val valid = Limits.checkOutputDimensions(1080, 1080)
        assertTrue(valid is LimitCheck.Ok)

        // Single side > 12000
        val tooLarge = Limits.checkOutputDimensions(12001, 1080)
        assertTrue(tooLarge is LimitCheck.Error)
        assertEquals(LimitErrorReason.TOO_LARGE, (tooLarge as LimitCheck.Error).reason)

        // Invalid < 1
        val invalid = Limits.checkOutputDimensions(0, 100)
        assertTrue(invalid is LimitCheck.Error)
        assertEquals(LimitErrorReason.INVALID_DIMENSIONS, (invalid as LimitCheck.Error).reason)
    }
}
