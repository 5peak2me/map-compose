package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize

internal data class DistanceScaleOverlayLayoutInput(
    val frame: Rect,
    val widthLabelSize: IntSize,
    val heightLabelSize: IntSize,
    val tickLengthPx: Float,
    val gapPx: Float,
    val lineTextGapPx: Float,
)

internal data class DistanceScaleOverlayLayout(
    val shouldDrawDistance: Boolean,
    val topLineY: Float,
    val leftLineX: Float,
    val widthLabelLeft: Float,
    val widthLabelTop: Float,
    val widthLabelBlockLeft: Float,
    val widthLabelBlockRight: Float,
    val heightLabelBlockTop: Float,
    val heightLabelBlockBottom: Float,
    val heightLabelCenter: Offset,
    val heightLabelWidth: Float,
    val heightLabelHeight: Float,
)

internal class DistanceScaleOverlayLayoutCalculator {
    fun calculate(input: DistanceScaleOverlayLayoutInput): DistanceScaleOverlayLayout {
        val frame = input.frame
        val widthLabelWidth = input.widthLabelSize.width.toFloat()
        val widthLabelHeight = input.widthLabelSize.height.toFloat()
        val topLineY = frame.top - input.tickLengthPx - input.gapPx
        val widthLabelLeft = frame.center.x - widthLabelWidth / 2f
        val widthLabelTop = frame.top - widthLabelHeight - input.tickLengthPx / 2f
        val widthLabelBlockLeft = widthLabelLeft - input.lineTextGapPx
        val widthLabelBlockRight = widthLabelLeft + widthLabelWidth + input.lineTextGapPx

        val leftLineX = frame.left - input.tickLengthPx - input.gapPx
        val heightLabelWidth = input.heightLabelSize.width.toFloat()
        val heightLabelHeight = input.heightLabelSize.height.toFloat()
        val heightLabelTop = frame.center.y - heightLabelWidth / 2f
        val heightLabelBottom = frame.center.y + heightLabelWidth / 2f
        val heightLabelBlockTop = heightLabelTop - input.lineTextGapPx
        val heightLabelBlockBottom = heightLabelBottom + input.lineTextGapPx
        val heightLabelCenter = Offset(leftLineX, frame.center.y)

        return DistanceScaleOverlayLayout(
            shouldDrawDistance = frame.width > widthLabelWidth,
            topLineY = topLineY,
            leftLineX = leftLineX,
            widthLabelLeft = widthLabelLeft,
            widthLabelTop = widthLabelTop,
            widthLabelBlockLeft = widthLabelBlockLeft,
            widthLabelBlockRight = widthLabelBlockRight,
            heightLabelBlockTop = heightLabelBlockTop,
            heightLabelBlockBottom = heightLabelBlockBottom,
            heightLabelCenter = heightLabelCenter,
            heightLabelWidth = heightLabelWidth,
            heightLabelHeight = heightLabelHeight
        )
    }
}
