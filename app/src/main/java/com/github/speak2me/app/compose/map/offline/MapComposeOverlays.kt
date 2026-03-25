package com.github.speak2me.app.compose.map.offline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun MapComposeOverlay(
    frame: Rect,
    widthText: String,
    heightText: String,
    maskColor: Color = Color.Black.copy(alpha = 0.7f),
    borderColor: Color = Color(0xFF3A90FF),
    borderWidth: Dp = 4.dp,
    guideColor: Color = Color(0xFF4FA0FF),
    layoutCalculator: DistanceScaleOverlayLayoutCalculator = remember {
        DistanceScaleOverlayLayoutCalculator()
    },
) {
    val textMeasurer = rememberTextMeasurer()
    val distanceLabelStyle = remember {
        TextStyle(
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.Normal
        )
    }

    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {
                val selectionPath = Path().apply { addRect(frame) }
                val strokePx = 1.dp.toPx()
                val tickLengthPx = 8.dp.toPx()
                val gapPx = tickLengthPx
                val lineTextGapPx = 1.dp.toPx()
                val widthTextLayout = textMeasurer.measure(text = widthText, style = distanceLabelStyle)
                val heightTextLayout = textMeasurer.measure(text = heightText, style = distanceLabelStyle)
                val layout = layoutCalculator.calculate(
                    input = DistanceScaleOverlayLayoutInput(
                        frame = frame,
                        widthLabelSize = widthTextLayout.size,
                        heightLabelSize = heightTextLayout.size,
                        tickLengthPx = tickLengthPx,
                        gapPx = gapPx,
                        lineTextGapPx = lineTextGapPx
                    )
                )

                onDrawWithContent {
                    drawContent()

                    // 1. Draw Mask
                    clipPath(path = selectionPath, clipOp = ClipOp.Difference) {
                        drawRect(color = maskColor, size = size)
                    }

                    // 2. Draw Selection Border
                    drawRect(
                        color = borderColor,
                        topLeft = Offset(frame.left, frame.top),
                        size = Size(frame.width, frame.height),
                        style = Stroke(borderWidth.toPx())
                    )

                    // 3. Draw Center Guides
                    drawFrameCenterGuides(
                        frame = frame,
                        guideColor = guideColor,
                        strokePx = strokePx
                    )

                    // 4. Draw Distance Scales
                    if (layout.shouldDrawDistance) {
                        drawHorizontalDistanceScale(
                            frame = frame,
                            layout = layout,
                            color = guideColor,
                            strokePx = strokePx,
                            tickLengthPx = tickLengthPx
                        )
                        drawText(
                            textLayoutResult = widthTextLayout,
                            topLeft = Offset(x = layout.widthLabelLeft, y = layout.widthLabelTop)
                        )

                        drawVerticalDistanceScale(
                            frame = frame,
                            layout = layout,
                            color = guideColor,
                            strokePx = strokePx,
                            tickLengthPx = tickLengthPx
                        )
                        rotate(degrees = -90f, pivot = layout.heightLabelCenter) {
                            drawText(
                                textLayoutResult = heightTextLayout,
                                topLeft = Offset(
                                    x = layout.heightLabelCenter.x - layout.heightLabelWidth / 2f,
                                    y = layout.heightLabelCenter.y - layout.heightLabelHeight / 4f,
                                )
                            )
                        }
                    }
                }
            }
    )
}

private fun DrawScope.drawFrameCenterGuides(
    frame: Rect,
    guideColor: Color,
    strokePx: Float,
) {
    drawLine(
        color = guideColor,
        start = Offset(frame.left, frame.center.y),
        end = Offset(frame.right, frame.center.y),
        strokeWidth = strokePx
    )
    drawLine(
        color = guideColor,
        start = Offset(frame.center.x, frame.top),
        end = Offset(frame.center.x, frame.bottom),
        strokeWidth = strokePx
    )
}

private fun DrawScope.drawHorizontalDistanceScale(
    frame: Rect,
    layout: DistanceScaleOverlayLayout,
    color: Color,
    strokePx: Float,
    tickLengthPx: Float,
) {
    drawLine(
        color = color,
        start = Offset(frame.left, layout.topLineY),
        end = Offset(frame.left, layout.topLineY + tickLengthPx),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(frame.left, layout.topLineY + tickLengthPx / 2f),
        end = Offset(layout.widthLabelBlockLeft, layout.topLineY + tickLengthPx / 2f),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(layout.widthLabelBlockRight, layout.topLineY + tickLengthPx / 2f),
        end = Offset(frame.right, layout.topLineY + tickLengthPx / 2f),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(frame.right, layout.topLineY),
        end = Offset(frame.right, layout.topLineY + tickLengthPx),
        strokeWidth = strokePx
    )
}

private fun DrawScope.drawVerticalDistanceScale(
    frame: Rect,
    layout: DistanceScaleOverlayLayout,
    color: Color,
    strokePx: Float,
    tickLengthPx: Float,
) {
    drawLine(
        color = color,
        start = Offset(layout.leftLineX, frame.top - strokePx),
        end = Offset(layout.leftLineX + tickLengthPx, frame.top - strokePx),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(layout.leftLineX + tickLengthPx / 2f, frame.top - strokePx),
        end = Offset(layout.leftLineX + tickLengthPx / 2f, layout.heightLabelBlockTop),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(layout.leftLineX + tickLengthPx / 2f, layout.heightLabelBlockBottom),
        end = Offset(layout.leftLineX + tickLengthPx / 2f, frame.bottom + strokePx),
        strokeWidth = strokePx
    )
    drawLine(
        color = color,
        start = Offset(layout.leftLineX, frame.bottom + strokePx),
        end = Offset(layout.leftLineX + tickLengthPx, frame.bottom + strokePx),
        strokeWidth = strokePx
    )
}
