package com.github.speak2me.app.compose.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.speak2me.compose.map.amap.CameraPositionState
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.pow

data class ScaleBar(val label: String, val lengthInMeters: Int, val pixelLength: Float)

private inline fun metersPerPixel(zoom: Float, latitude: Double): Double {
    val earthCircumference = 40075016.686  // 地球赤道周长（单位：米）
    return earthCircumference * cos(toRadians(latitude)) / (256 * 2.0.pow(zoom.toDouble()))
}

fun calculateScaleBarStep(
    zoom: Float,
    latitude: Double,
    targetPxRange: ClosedFloatingPointRange<Float> = 50f..130f
): ScaleBar {
    val metersPerPx = metersPerPixel(zoom, latitude)

    // amap
    val candidates = listOf(
        5, 10, 25, 50, 100, 200, 500,
        1000, 2000, 5000, 10000, 20000, 30000,
        50000, 100000, 200000, 500000, 1_000_000
    )

    // gmap
//    val candidates = listOf(
//        2, 5, 10, 20, 50, 100, 200, 500,
//        1000, 2000, 5000, 10000, 20000,
//        50000, 100000, 200000, 500000, 1_000_000
//    )

    val valid = candidates.firstOrNull { scale ->
        val px = scale / metersPerPx
        println("px = $px")
        px in targetPxRange
    }

    return if (valid != null) {
        val px = valid / metersPerPx
        val label = if (valid >= 1000) "${valid / 1000} km" else "$valid m"
        ScaleBar(label, valid, px.toFloat())
    } else {
        // fallback：使用最大1000km比例尺，但按实际 px 计算
        val maxScale = candidates.max()
        val px = maxScale / metersPerPx
        val label = "${maxScale / 1000} km"
        ScaleBar(label, maxScale, px.toFloat().coerceAtLeast(40f)) // 最低保证40dp宽度
    }
}

@Composable
fun SmartScaleBar(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    color: Color = Color.Black,
) {
    val cameraPosition by rememberUpdatedState(cameraPositionState.position)

    // 提取 zoom & latitude
    val zoom = cameraPosition.zoom
    val latitude = cameraPosition.target.latitude

    val scale = remember(zoom, latitude) {
//        calculateScaleBarStep(zoom, latitude, 40f..120f)
//        calculateScaleBarStep(zoom, latitude, 40f..180f)
        calculateScaleBarStep(zoom, latitude)
    }

    val textMeasurer = rememberTextMeasurer()
    Canvas(
        modifier = modifier
            .height(32.dp)
            .width(scale.pixelLength.dp)
    ) {
        val barHeight = 2.dp.toPx()
        val centerY = size.height / 2
        val width = size.width

        // 主比例尺线
        drawLine(
            color = color,
            start = Offset(0f, centerY),
            end = Offset(width, centerY),
            strokeWidth = barHeight
        )

        // 左右刻度线
        val tickHeight = 6.dp.toPx()
        drawLine(
            color = color,
            start = Offset(0f, centerY - tickHeight),
            end = Offset(0f, centerY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(width, centerY - tickHeight),
            end = Offset(width, centerY),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )

        // 使用 textMeasurer 测量文本尺寸
        val textLayoutResult = textMeasurer.measure(
            text = scale.label,
            style = TextStyle(
                color = color,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        )

        val textX = (size.width - textLayoutResult.size.width) / 2f
        // 计算文字 Y 坐标
        val textHeight = textLayoutResult.size.height
        val spacing = 4.dp.toPx()
        val textY = centerY - barHeight / 2 - spacing - textHeight

        // 绘制文字
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(textX, textY)
        )
    }
}
