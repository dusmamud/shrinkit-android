package com.dusrel.shrinkit.domain.engine

import com.dusrel.shrinkit.domain.model.SizeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SizeUnitsTest {

    @Test
    fun testRoundToTwoDecimals() {
        assertEquals(12.35, SizeUnits.roundToTwoDecimals(12.3456), 0.001)
        assertEquals(12.34, SizeUnits.roundToTwoDecimals(12.3412), 0.001)
        assertEquals(10.0, SizeUnits.roundToTwoDecimals(10.0), 0.001)
    }

    @Test
    fun testGetDefaultDimensionsForUnit() {
        // Percent
        val percent = SizeUnits.getDefaultDimensionsForUnit(SizeUnit.PERCENT, 1920, 1080)
        assertEquals(70f, percent.first, 0.01f)
        assertEquals(70f, percent.second, 0.01f)

        // Pixels
        val px = SizeUnits.getDefaultDimensionsForUnit(SizeUnit.PIXELS, 1920, 1080)
        assertEquals(1920f, px.first, 0.01f)
        assertEquals(1080f, px.second, 0.01f)

        // Inches @ 72 DPI
        val inches = SizeUnits.getDefaultDimensionsForUnit(SizeUnit.INCHES, 144, 72, dpi = 72)
        assertEquals(2.0f, inches.first, 0.01f)
        assertEquals(1.0f, inches.second, 0.01f)

        // CM @ 72 DPI (72 / 2.54 = 28.346 dots per cm)
        val cm = SizeUnits.getDefaultDimensionsForUnit(SizeUnit.CM, 283, 283, dpi = 72)
        assertEquals(9.98f, cm.first, 0.05f)
    }

    @Test
    fun testCalculateProportionalDimension() {
        // Pixels: 1000x500 original (2:1 aspect ratio)
        // If width changes to 800, height must be 400
        val h = SizeUnits.calculateProportionalDimension(
            changedField = DimensionField.WIDTH,
            newValue = 800f,
            unit = SizeUnit.PIXELS,
            origW = 1000,
            origH = 500
        )
        assertEquals(400f, h, 0.01f)

        // If height changes to 250, width must be 500
        val w = SizeUnits.calculateProportionalDimension(
            changedField = DimensionField.HEIGHT,
            newValue = 250f,
            unit = SizeUnit.PIXELS,
            origW = 1000,
            origH = 500
        )
        assertEquals(500f, w, 0.01f)

        // Percent returns same newValue
        val p = SizeUnits.calculateProportionalDimension(
            changedField = DimensionField.WIDTH,
            newValue = 50f,
            unit = SizeUnit.PERCENT,
            origW = 1000,
            origH = 500
        )
        assertEquals(50f, p, 0.01f)
    }

    @Test
    fun testToPixels() {
        // Percent: 50% of 1000x800 -> 500x400
        val (pw, ph) = SizeUnits.toPixels(50f, 50f, SizeUnit.PERCENT, 72, 1000, 800)
        assertEquals(500, pw)
        assertEquals(400, ph)

        // Pixels: direct
        val (pxw, pxh) = SizeUnits.toPixels(640f, 480f, SizeUnit.PIXELS, 72, 1000, 800)
        assertEquals(640, pxw)
        assertEquals(480, pxh)

        // Inches: 2x3 in @ 300 DPI -> 600x900
        val (inw, inh) = SizeUnits.toPixels(2f, 3f, SizeUnit.INCHES, 300, 1000, 800)
        assertEquals(600, inw)
        assertEquals(900, inh)

        // CM: 2.54 x 5.08 cm @ 100 DPI -> 100 x 200 px
        val (cmw, cmh) = SizeUnits.toPixels(2.54f, 5.08f, SizeUnit.CM, 100, 1000, 800)
        assertEquals(100, cmw)
        assertEquals(200, cmh)
    }

    @Test
    fun testToPixelsInvalidInputs() {
        assertThrows(IllegalArgumentException::class.java) {
            SizeUnits.toPixels(-10f, 50f, SizeUnit.PIXELS, 72, 100, 100)
        }
        assertThrows(IllegalArgumentException::class.java) {
            SizeUnits.toPixels(50f, 50f, SizeUnit.PIXELS, 72, 0, 100)
        }
    }

    @Test
    fun testCropRect() {
        // Source is 1000x500 (2:1), target is 500x500 (1:1 Square)
        // Trim left/right edges: width should be 500, height 500, x = (1000-500)/2 = 250, y = 0
        val crop = SizeUnits.cropRect(1000, 500, 500, 500)
        assertEquals(500, crop.width)
        assertEquals(500, crop.height)
        assertEquals(250, crop.x)
        assertEquals(0, crop.y)

        // Source is 500x1000 (1:2), target is 500x500 (1:1)
        // Trim top/bottom edges: width 500, height 500, x = 0, y = (1000-500)/2 = 250
        val crop2 = SizeUnits.cropRect(500, 1000, 500, 500)
        assertEquals(500, crop2.width)
        assertEquals(500, crop2.height)
        assertEquals(0, crop2.x)
        assertEquals(250, crop2.y)
    }

    @Test
    fun testFitDims() {
        // Source 1000x500 (2:1), target 400x400
        // Scaled to fit inside 400x400 preserving 2:1 -> 400x200
        val (fw, fh) = SizeUnits.fitDims(1000, 500, 400, 400)
        assertEquals(400, fw)
        assertEquals(200, fh)

        // Source 500x1000 (1:2), target 400x400
        // Scaled to fit inside 400x400 preserving 1:2 -> 200x400
        val (fw2, fh2) = SizeUnits.fitDims(500, 1000, 400, 400)
        assertEquals(200, fw2)
        assertEquals(400, fh2)
    }

    @Test
    fun testPercentOf() {
        assertEquals(50, SizeUnits.percentOf(500L, 1000L))
        assertEquals(10, SizeUnits.percentOf(100L, 1000L))
        assertEquals(0, SizeUnits.percentOf(100L, 0L))
    }
}
