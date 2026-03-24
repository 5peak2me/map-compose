package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DistanceScaleOverlayLayoutCalculatorTest {

    private val calculator = DefaultDistanceScaleOverlayLayoutCalculator()

    @Test
    fun `calculate returns expected coordinates for normal frame`() {
        val layout = calculator.calculate(
            DistanceScaleOverlayLayoutInput(
                frame = Rect(left = 100f, top = 200f, right = 500f, bottom = 400f),
                widthLabelSize = IntSize(width = 80, height = 20),
                heightLabelSize = IntSize(width = 40, height = 16),
                tickLengthPx = 8f,
                gapPx = 8f,
                lineTextGapPx = 1f
            )
        )

        assertTrue(layout.shouldDrawDistance)
        assertFloatEquals(expected = 184f, actual = layout.topLineY)
        assertFloatEquals(expected = 84f, actual = layout.leftLineX)
        assertFloatEquals(expected = 260f, actual = layout.widthLabelLeft)
        assertFloatEquals(expected = 176f, actual = layout.widthLabelTop)
        assertFloatEquals(expected = 259f, actual = layout.widthLabelBlockLeft)
        assertFloatEquals(expected = 341f, actual = layout.widthLabelBlockRight)
        assertFloatEquals(expected = 279f, actual = layout.heightLabelBlockTop)
        assertFloatEquals(expected = 321f, actual = layout.heightLabelBlockBottom)
        assertFloatEquals(expected = 84f, actual = layout.heightLabelCenter.x)
        assertFloatEquals(expected = 300f, actual = layout.heightLabelCenter.y)
        assertFloatEquals(expected = 40f, actual = layout.heightLabelWidth)
        assertFloatEquals(expected = 16f, actual = layout.heightLabelHeight)
    }

    @Test
    fun `calculate hides scale when frame is narrower than width label`() {
        val layout = calculator.calculate(
            DistanceScaleOverlayLayoutInput(
                frame = Rect(left = 0f, top = 0f, right = 50f, bottom = 40f),
                widthLabelSize = IntSize(width = 80, height = 20),
                heightLabelSize = IntSize(width = 24, height = 12),
                tickLengthPx = 8f,
                gapPx = 8f,
                lineTextGapPx = 1f
            )
        )

        assertFalse(layout.shouldDrawDistance)
    }

    @Test
    fun `calculate hides scale when frame width equals width label`() {
        val layout = calculator.calculate(
            DistanceScaleOverlayLayoutInput(
                frame = Rect(left = 0f, top = 10f, right = 80f, bottom = 50f),
                widthLabelSize = IntSize(width = 80, height = 18),
                heightLabelSize = IntSize(width = 20, height = 10),
                tickLengthPx = 6f,
                gapPx = 6f,
                lineTextGapPx = 2f
            )
        )

        assertFalse(layout.shouldDrawDistance)
        assertFloatEquals(expected = -2f, actual = layout.topLineY)
    }

    private fun assertFloatEquals(expected: Float, actual: Float, delta: Float = 0.0001f) {
        assertEquals(expected, actual, delta)
    }
}
