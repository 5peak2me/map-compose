package com.github.speak2me.app.compose.map.route.plan.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.pointerInteropFilter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.speak2me.app.compose.map.route.plan.data.model.Trackpoint

@Composable
fun ElevationChart(
    modifier: Modifier = Modifier,
    tracks: List<Trackpoint>,
    onChartScroll: (Float) -> Unit = {},
    isChartScrollable: Boolean = false,
) {
    if (tracks.isEmpty()) return

    // 使用轨迹点数据绘制海拔图表
    val allPoints = tracks
    val maxElevation = (allPoints.maxOfOrNull { it.altitude } ?: 0.0).coerceAtLeast(1.0)
    val minElevation = allPoints.minOfOrNull { it.altitude } ?: 0.0
    val elevationRange = maxElevation - minElevation

    // 计算图表尺寸和比例
    val chartHeight = 150.dp
    val textMeasurer = rememberTextMeasurer()
    var sliderPosition by remember { mutableFloatStateOf(0f) }
    var isDragging by remember { mutableStateOf(false) }

    // 定义左侧文字区域宽度
    val leftPadding = 50f // 为左侧文字预留空间

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.White,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp)
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .then(
                    if (isChartScrollable) {
                        Modifier
                            .pointerInput(Unit) {
                                detectTapGestures { offset ->
                                    // 只在图表区域内响应点击
                                    if (offset.x >= leftPadding) {
                                        val chartX = offset.x - leftPadding
                                        val chartWidth = size.width - leftPadding
                                        sliderPosition = (chartX / chartWidth).coerceIn(0f, 1f)
                                        onChartScroll(sliderPosition)
                                    }
                                }
                            }
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging = true },
                                    onDragEnd = { isDragging = false }
                                ) { change, _ ->
                                    // 只在图表区域内响应拖拽
                                    if (change.position.x >= leftPadding) {
                                        val chartX = change.position.x - leftPadding
                                        val chartWidth = size.width - leftPadding
                                        sliderPosition = (chartX / chartWidth).coerceIn(0f, 1f)
                                        onChartScroll(sliderPosition)
                                    }
                                }
                            }
                    } else {
                        Modifier.pointerInteropFilter(onTouchEvent = { true })
                    }
                )
        ) {
            val width = size.width
            val height = size.height

            // 绘制背景网格
            val gridColor = Color.LightGray.copy(alpha = 0.3f)

            // 水平网格线（隐藏底部线条）
            for (i in 0..3) { // 改为 0..3，不绘制最底部的线条
                val y = height * (1 - i * 0.25f) // 调整间距
                drawLine(
                    color = gridColor,
                    start = Offset(leftPadding, y), // 从左侧文字区域后开始绘制
                    end = Offset(width, y),
                    strokeWidth = 1f,
                    pathEffect = PathEffect.dashPathEffect(
                        intervals = floatArrayOf(10f, 5f),
                        phase = 0f
                    )
                )

                // 绘制海拔刻度
                val elevation = (minElevation + elevationRange * i / 3).toInt() // 调整计算
                drawText(
                    textMeasurer = textMeasurer,
                    text = "${elevation}m",
                    topLeft = Offset(5f, y - 15f), // 在左侧文字区域内绘制
                    style = TextStyle.Default
                )
            }

            // 绘制曲线
            val path = Path()
            var firstPoint = true
            val chartWidth = width - leftPadding // 图表实际宽度

            allPoints.forEachIndexed { index, point ->
                // 使用索引来计算 x 坐标，确保均匀分布
                val progress = if (allPoints.size > 1) {
                    index.toFloat() / (allPoints.size - 1)
                } else {
                    0f
                }
                val x = leftPadding + progress * chartWidth
                val normalizedAltitude = if (elevationRange > 0) {
                    (point.altitude - minElevation) / elevationRange
                } else {
                    0.5f
                }
                val y = height * (1 - normalizedAltitude.toFloat())

                if (firstPoint) {
                    path.moveTo(x, y)
                    firstPoint = false
                } else {
                    path.lineTo(x, y)
                }
            }

            // 绘制曲线
            drawPath(
                path = path,
                color = Color.Blue,
                style = Stroke(width = 3f)
            )

            // 绘制滑块
            if (isDragging && width > 0f && height > 0f) {
                val sliderX = leftPadding + sliderPosition * chartWidth // 从左侧文字区域后开始
                drawLine(
                    color = Color.Red,
                    start = Offset(sliderX, 0f),
                    end = Offset(sliderX, height),
                    strokeWidth = 3f
                )

                // 显示当前位置的海拔和距离
                val currentIndex = (sliderPosition * (allPoints.size - 1)).toInt()
                    .coerceIn(0, allPoints.size - 1)
                val currentPoint = allPoints.getOrNull(currentIndex) ?: return@Canvas

                val elevationText = "${currentPoint.altitude.toInt()}m"
                val currentDistance = currentPoint.distance
                val distanceText = if (currentDistance >= 1000) {
                    "${(currentDistance / 1000.0).format(1)}km"
                } else {
                    "${currentDistance.toInt()}m"
                }

                // 绘制信息框背景
                val infoText = "$elevationText / $distanceText"
                val textSize = textMeasurer.measure(infoText).size
                val infoBoxWidth = textSize.width + 16f
                val infoBoxHeight = textSize.height + 8f

                // 确保信息框不会超出画布边界
                if (infoBoxWidth > 0f && infoBoxHeight > 0f && infoBoxWidth <= width) {
                    val maxX = width - infoBoxWidth
                    val infoBoxX =
                        (sliderX - infoBoxWidth / 2).coerceIn(0f, maxX.coerceAtLeast(0f))
                    val infoBoxY = 8f

                    drawRoundRect(
                        color = Color.Black.copy(alpha = 0.8f),
                        topLeft = Offset(infoBoxX, infoBoxY),
                        size = Size(infoBoxWidth, infoBoxHeight),
                        cornerRadius = CornerRadius(4f)
                    )

                    drawText(
                        textMeasurer = textMeasurer,
                        text = infoText,
                        topLeft = Offset(infoBoxX + 8f, infoBoxY + 4f),
                        style = TextStyle.Default.copy(
                            color = Color.White,
                            fontSize = 12.sp
                        )
                    )
                }
            }
        }
    }
}

internal fun Double.format(digits: Int) = "%.${digits}f".format(this)