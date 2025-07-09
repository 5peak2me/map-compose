package com.github.speak2me.app.compose.map

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import com.github.speak2me.compose.map.amap.CameraPositionState
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.pow

private const val EARTH_CIRCUMFERENCE = 40075016.686 // 地球赤道周长（单位：米）

private val zoomToMeters = mapOf(
    20f to 5,
    19f to 10,
    18f to 25,
    17f to 50,
    16f to 100,
    15f to 200,
    14f to 500,
    13f to 1000,
    12f to 2000,
    11f to 5000,
    10f to 10000,
    9f to 20000,
    8f to 30000,
    7f to 50000,
    6f to 100000,
    5f to 200000,
    4f to 500000,
    3f to 1000000
)

@Composable
fun ScaleBar(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    color: Color = Color.Black,
    shadowColor: Color = Color.White,
) {
    val zoom = cameraPositionState.position.zoom
    val target = cameraPositionState.position.target
    val density = LocalDensity.current

    val metersPerPixel = remember(zoom, target) {
        val latitude = target.latitude
        EARTH_CIRCUMFERENCE * cos(Math.toRadians(latitude)) / (2.0.pow(zoom.toDouble()) * 256)
    }

    // 使用浮点数zoom，通过插值计算比例尺，避免在.5时就切换
    val clampedZoom = zoom.coerceIn(3f, 20f)
    val scaleLengthMeters by remember(clampedZoom) {
        derivedStateOf {
            zoomToMeters.entries.firstOrNull { clampedZoom >= it.key }?.value ?: 1000000
        }
    }
    val scaleBarWidthPx = (scaleLengthMeters / metersPerPixel).toFloat() * 3f/*2.5f*/

    val displayText = if (scaleLengthMeters < 1000) "${scaleLengthMeters}m" else "${scaleLengthMeters / 1000}km"

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = displayText,
            fontSize = 12.sp,
            color = color,
            textAlign = TextAlign.End,
            lineHeight = 1.em,
            style = MaterialTheme.typography.headlineSmall.copy(
                shadow = Shadow(
                    color = shadowColor,
                    offset = Offset(2f, 2f),
                    blurRadius = 1f
                )
            )
        )
        Canvas(
            modifier = Modifier
                .height(12.dp)
                .width(with(density) { scaleBarWidthPx.toDp() })
        ) {
            val strokeWidth = 2.dp.toPx()
            val shadowStrokeWidth = strokeWidth + 4
            val centerY = size.height / 2
            val width = size.width
            val tickHeight = 6.dp.toPx()

            drawLine(
                color = shadowColor,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = shadowStrokeWidth
            )
            drawLine(
                color = shadowColor,
                start = Offset(0f, centerY - tickHeight),
                end = Offset(0f, centerY),
                strokeWidth = shadowStrokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = shadowColor,
                start = Offset(width, centerY - tickHeight),
                end = Offset(width, centerY),
                strokeWidth = shadowStrokeWidth,
                cap = StrokeCap.Round
            )

            drawLine(
                color = color,
                start = Offset(0f, centerY),
                end = Offset(width, centerY),
                strokeWidth = strokeWidth
            )
            drawLine(
                color = color,
                start = Offset(0f, centerY - tickHeight),
                end = Offset(0f, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
            drawLine(
                color = color,
                start = Offset(width, centerY - tickHeight),
                end = Offset(width, centerY),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DisappearingScaleBar(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState,
    color: Color = Color.Black,
    shadowColor: Color = Color.White,
    visibilityDurationMillis: Int = 3_000,
    enterTransition: EnterTransition = fadeIn(),
    exitTransition: ExitTransition = fadeOut(),
) {
    val visible = remember {
        MutableTransitionState(true)
    }

    LaunchedEffect(key1 = cameraPositionState.position.zoom) {
        // Show ScaleBar
        visible.targetState = true
        delay(visibilityDurationMillis.toLong())
        // Hide ScaleBar after timeout period
        visible.targetState = false
    }

    AnimatedVisibility(
        visibleState = visible,
        modifier = modifier,
        enter = enterTransition,
        exit = exitTransition
    ) {
        ScaleBar(
            cameraPositionState = cameraPositionState,
            color = color,
            shadowColor = shadowColor,
        )
    }
}
