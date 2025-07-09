package com.github.speak2me.app.compose.map.demo.google.test

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@Composable
fun MapElevationScreen(viewModel: MapElevationViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // 处理加载状态
    if (uiState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    // 处理错误状态
    uiState.error?.let { error ->
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "加载轨迹数据失败: $error",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(onClick = { viewModel.refreshTrackPoints() }) {
                    Text("重试")
                }
            }
        }
        return
    }

    // 如果没有轨迹数据，显示空状态
    if (uiState.trackPoints.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无轨迹数据",
                style = MaterialTheme.typography.bodyLarge
            )
        }
        return
    }

    // 计算地图中心点
    val centerPoint = remember(uiState.trackPoints) {
        viewModel.getMapCenter() ?: LatLng(0.0, 0.0)
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(centerPoint, 13f)
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 地图部分 - 占据上方2/3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    viewModel.onMapClick(latLng)
                }
            ) {
                // 绘制轨迹线
                Polyline(
                    points = uiState.trackPoints.map { it.latLng },
                    color = Color.Blue,
                    width = 8f
                )

                // 只有在有选中点且hasMarker为true时才绘制marker
                if (uiState.hasMarker) {
                    val selectedTrackPoint = viewModel.getSelectedTrackPoint()
                    selectedTrackPoint?.let { trackPoint ->
                        Marker(
                            state = MarkerState(position = trackPoint.latLng),
                            title = "海拔: ${trackPoint.elevation}m",
                            snippet = "距离: ${(trackPoint.distance / 1000).toInt()}km"
                        )
                    }
                }
            }

            // 添加一个清除marker的按钮
            if (uiState.hasMarker) {
                FloatingActionButton(
                    onClick = { viewModel.clearMarker() },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                ) {
                    Text("清除")
                }
            }
        }

        // 海拔图表部分 - 占据下方1/3
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .background(Color.White)
        ) {
            ElevationChart(
                trackPoints = uiState.trackPoints,
                selectedIndex = uiState.selectedPointIndex,
                hasMarker = uiState.hasMarker,
                onDragPositionChange = { newPosition ->
                    viewModel.onChartDragPositionChange(newPosition)
                }
            )
        }
    }
}

@Composable
fun ElevationChart(
    trackPoints: List<TrackPoint>,
    selectedIndex: Int?,
    hasMarker: Boolean,
    onDragPositionChange: (Float) -> Unit
) {
    val density = LocalDensity.current

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        // 当开始拖拽时，计算初始位置
                        val newPosition = (offset.x / size.width).coerceIn(0f, 1f)
                        onDragPositionChange(newPosition)
                    }
                ) { change, _ ->
                    val newPosition = (change.position.x / size.width).coerceIn(0f, 1f)
                    onDragPositionChange(newPosition)
                }
            }
    ) {
        if (trackPoints.isEmpty()) return@Canvas

        val padding = 50.dp.toPx()
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val minElevation = trackPoints.minOf { it.elevation }
        val maxElevation = trackPoints.maxOf { it.elevation }
        val elevationRange = maxElevation - minElevation

        // 绘制背景网格
        drawGrid(padding, chartWidth, chartHeight, minElevation, maxElevation)

        // 绘制海拔曲线
        val path = Path()
        trackPoints.forEachIndexed { index, point ->
            val x = padding + (index.toFloat() / (trackPoints.size - 1)) * chartWidth
            val y = padding + chartHeight - ((point.elevation - minElevation) / elevationRange * chartHeight).toFloat()

            if (index == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }
        }

        // 绘制曲线
        drawPath(
            path = path,
            color = Color.Blue,
            style = Stroke(width = 4.dp.toPx())
        )

        // 只有在有marker且selectedIndex不为null时才绘制选中点
        if (hasMarker && selectedIndex != null && selectedIndex < trackPoints.size) {
            val selectedPoint = trackPoints[selectedIndex]
            val x = padding + (selectedIndex.toFloat() / (trackPoints.size - 1)) * chartWidth
            val y = padding + chartHeight - ((selectedPoint.elevation - minElevation) / elevationRange * chartHeight).toFloat()

            drawCircle(
                color = Color.Red,
                radius = 8.dp.toPx(),
                center = Offset(x, y)
            )

            // 绘制垂直指示线
            drawLine(
                color = Color.Red,
                start = Offset(x, padding),
                end = Offset(x, padding + chartHeight),
                strokeWidth = 2.dp.toPx()
            )
        }

        // 绘制坐标轴标签
        drawLabels(padding, chartWidth, chartHeight, minElevation, maxElevation, trackPoints)
    }
}

private fun DrawScope.drawGrid(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    minElevation: Double,
    maxElevation: Double
) {
    val gridColor = Color.Gray.copy(alpha = 0.3f)

    // 绘制水平网格线
    for (i in 0..5) {
        val y = padding + (i * chartHeight / 5)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx()
        )
    }

    // 绘制垂直网格线
    for (i in 0..10) {
        val x = padding + (i * chartWidth / 10)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx()
        )
    }
}

private fun DrawScope.drawLabels(
    padding: Float,
    chartWidth: Float,
    chartHeight: Float,
    minElevation: Double,
    maxElevation: Double,
    trackPoints: List<TrackPoint>
) {
    val paint = Paint().asFrameworkPaint().apply {
        isAntiAlias = true
        textSize = 12.sp.toPx()
        color = android.graphics.Color.BLACK
    }

    // Y轴标签（海拔）
    for (i in 0..5) {
        val elevation = minElevation + (maxElevation - minElevation) * (5 - i) / 5
        val y = padding + (i * chartHeight / 5)
        drawContext.canvas.nativeCanvas.drawText(
            "${elevation.toInt()}m",
            padding - 40.dp.toPx(),
            y + 5.dp.toPx(),
            paint
        )
    }

    // X轴标签（距离）
    for (i in 0..5) {
        val distance = trackPoints.lastOrNull()?.distance ?: 0.0
        val distanceValue = distance * i / 5
        val x = padding + (i * chartWidth / 5)
        drawContext.canvas.nativeCanvas.drawText(
            "${(distanceValue / 1000).toInt()}km",
            x - 15.dp.toPx(),
            padding + chartHeight + 30.dp.toPx(),
            paint
        )
    }
}