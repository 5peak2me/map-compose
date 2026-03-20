package com.github.speak2me.app.compose.map

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.speak2me.app.compose.map.platform.GeoPoint
import com.github.speak2me.app.compose.map.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.platform.MapPlatform
import com.github.speak2me.app.compose.map.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.platform.MapUiConfig
import com.github.speak2me.app.compose.map.platform.amap.AMapPlatform
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

private const val MIN_WIDTH_METERS = 16_000f
private const val BORDER_SHRINK_START_METERS = 400_000f
private const val INITIAL_MARGIN_RATIO = 0.1f
private const val INIT_ZOOM_EPSILON = 0.0005f

private val defaultCenter = GeoPoint(31.846594, 117.125279)

@Composable
fun MapCompose(
    modifier: Modifier = Modifier,
    mapPlatform: MapPlatform = remember { AMapPlatform() },
    frameStrategy: SelectionFrameStrategy = remember { CenterAnchoredFrameStrategy() },
    frameResizePolicy: FrameResizePolicy = remember { MaxWidthFrameResizePolicy() },
    distanceCalculator: DistanceCalculator = remember { PlatformDistanceCalculator() },
    distanceFormatter: DistanceFormatter = remember { KilometerDistanceFormatter() },
) {
    val mapController = mapPlatform.rememberController(
        initialCenter = defaultCenter,
        initialZoom = 12f
    )
    val uiConfig = remember { MapUiConfig() }
    var isInitCalibrated by remember { mutableStateOf(false) }
    var maxZoomLimit by remember { mutableStateOf<Float?>(null) }


    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val containerSize = rememberContainerSizePx(maxWidth, maxHeight, density)
        val screenAspectRatio = rememberAspectRatio(containerSize)
        val currentZoom = mapController.zoom

        val initialFrame = remember(containerSize, screenAspectRatio, frameStrategy) {
            frameStrategy.createInitialFrame(containerSize, screenAspectRatio)
        }
        val initialFrameWidthMeters = remember(
            initialFrame,
            currentZoom,
            mapController.projection,
            distanceCalculator
        ) {
            distanceCalculator.calculateWidthMeters(
                projection = mapController.projection,
                frame = initialFrame,
                mapPlatform = mapPlatform
            )
        }
        val resolvedFrame = frameResizePolicy.resolveFrame(
            initialFrame = initialFrame,
            initialWidthMeters = initialFrameWidthMeters
        )

        val widthMeters = distanceCalculator.calculateWidthMeters(
            projection = mapController.projection,
            frame = resolvedFrame,
            mapPlatform = mapPlatform
        )
        val heightMeters = distanceCalculator.calculateHeightMeters(
            projection = mapController.projection,
            frame = resolvedFrame,
            mapPlatform = mapPlatform
        )

        LaunchedEffect(containerSize, mapController.projection, mapController.zoom) {
            if (isInitCalibrated || containerSize.width <= 0) return@LaunchedEffect
            val currentDistance = distanceCalculator.calculateWidthMeters(
                projection = mapController.projection,
                frame = initialFrame,
                mapPlatform = mapPlatform
            ) ?: return@LaunchedEffect
            if (currentDistance <= 0f) return@LaunchedEffect

            val deltaZoom = log2(currentDistance / MIN_WIDTH_METERS)
            if (abs(deltaZoom) > INIT_ZOOM_EPSILON) {
                val targetZoom = (mapController.zoom + deltaZoom).coerceIn(3f, 20f)
                mapController.moveTo(center = mapController.center, zoom = targetZoom)
                return@LaunchedEffect
            }
            maxZoomLimit = mapController.zoom
            isInitCalibrated = true
        }

        Box(modifier = Modifier.fillMaxSize()) {
            mapPlatform.MapView(
                modifier = Modifier.fillMaxSize(),
                controller = mapController,
                cameraConstraint = MapCameraConstraint(
                    minZoom = 3f,
                    maxZoom = maxZoomLimit
                ),
                uiConfig = uiConfig
            )
            SelectionFrameOverlay(frame = resolvedFrame)
//            DistanceScaleOverlay(
//                frame = resolvedFrame,
//                distanceFormatter.format(widthMeters),
//                distanceFormatter.format(heightMeters)
//            )
        }
    }
}

/**
 * 定义初始选区边框的创建策略。
 */
interface SelectionFrameStrategy {
    /**
     * 基于容器尺寸和屏幕宽高比，创建初始选区边框。
     */
    fun createInitialFrame(containerSize: IntSize, screenAspectRatio: Float): Rect
}

class CenterAnchoredFrameStrategy : SelectionFrameStrategy {
    override fun createInitialFrame(containerSize: IntSize, screenAspectRatio: Float): Rect {
        val screenWidth = containerSize.width.toFloat()
        val margin = screenWidth * INITIAL_MARGIN_RATIO
        val width = screenWidth - margin * 2f
        val height = width * screenAspectRatio
        val centerX = screenWidth / 2f
        val centerY = margin + height / 2f
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        return Rect(
            left = centerX - halfWidth,
            top = centerY - halfHeight,
            right = centerX + halfWidth,
            bottom = centerY + halfHeight
        )
    }
}

/**
 * 定义缩放过程中边框何时以及如何改变大小。
 */
interface FrameResizePolicy {
    /**
     * 计算当前缩放状态下应渲染的边框。
     *
     * @param initialFrame 按策略生成的初始边框。
     * @param initialWidthMeters 初始边框在当前投影下对应的真实宽度（米）。
     */
    fun resolveFrame(
        initialFrame: Rect,
        initialWidthMeters: Float?,
    ): Rect
}

class MaxWidthFrameResizePolicy : FrameResizePolicy {
    override fun resolveFrame(
        initialFrame: Rect,
        initialWidthMeters: Float?,
    ): Rect {
        if (initialWidthMeters == null || initialWidthMeters <= BORDER_SHRINK_START_METERS) {
            return initialFrame
        }
        val scale = (BORDER_SHRINK_START_METERS / initialWidthMeters).coerceIn(0f, 1f)
        return initialFrame.scaleFromCenter(scale)
    }
}

/**
 * 负责将边框在屏幕上的宽高转换为真实距离（米）。
 */
interface DistanceCalculator {
    /**
     * 计算指定边框对应的真实宽度（米）。
     */
    fun calculateWidthMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Float?

    /**
     * 计算指定边框对应的真实高度（米）。
     */
    fun calculateHeightMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Float?
}

class PlatformDistanceCalculator : DistanceCalculator {
    override fun calculateWidthMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Float? {
        projection ?: return null
        val centerY = frame.center.y.roundToInt()
        val left = projection.fromScreenLocation(frame.left.roundToInt(), centerY) ?: return null
        val right = projection.fromScreenLocation(frame.right.roundToInt(), centerY) ?: return null
        return mapPlatform.distanceMeters(left, right)
    }

    override fun calculateHeightMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Float? {
        projection ?: return null
        val centerX = frame.center.x.roundToInt()
        val top = projection.fromScreenLocation(centerX, frame.top.roundToInt()) ?: return null
        val bottom =
            projection.fromScreenLocation(centerX, frame.bottom.roundToInt()) ?: return null
        return mapPlatform.distanceMeters(top, bottom)
    }
}

/**
 * 负责格式化边框周边展示的距离文案。
 */
interface DistanceFormatter {
    /**
     * 将米单位距离转换为用户可读文案。
     */
    fun format(distanceMeters: Float?): String
}

class KilometerDistanceFormatter : DistanceFormatter {
    override fun format(distanceMeters: Float?): String {
        if (distanceMeters == null) return "--"
        val kilometers = distanceMeters / 1000f
        return "%.2f公里".format(
            Locale.getDefault(),
            kilometers.coerceAtMost(400f).coerceAtLeast(16f)
        )
    }
}

private fun log2(value: Float): Float = (ln(value.toDouble()) / ln(2.0)).toFloat()

@Composable
private fun rememberContainerSizePx(
    maxWidth: Dp,
    maxHeight: Dp,
    density: Density,
): IntSize = remember(maxWidth, maxHeight, density) {
    with(density) {
        IntSize(maxWidth.roundToPx(), maxHeight.roundToPx())
    }
}

@Composable
private fun rememberAspectRatio(containerSize: IntSize): Float = remember(containerSize) {
    if (containerSize.width <= 0) 1f
    else containerSize.height.toFloat() / containerSize.width.toFloat()
}

private fun Rect.scaleFromCenter(scale: Float): Rect {
    val center = center
    val halfWidth = width * scale / 2f
    val halfHeight = height * scale / 2f
    return Rect(
        left = center.x - halfWidth,
        top = center.y - halfHeight,
        right = center.x + halfWidth,
        bottom = center.y + halfHeight
    )
}

@Composable
private fun SelectionFrameOverlay(
    frame: Rect,
    maskColor: Color = Color.Black.copy(alpha = 0.7f),
    borderColor: Color = Color(0xFF3A90FF),
    borderWidth: Dp = 4.dp,
) {
    Box(
        Modifier
            .fillMaxSize()
            .drawWithCache {

                println("1111111111111111")

                onDrawWithContent {
                    println("2222222222222222")

                    drawContent()
                    val selectionPath = Path().apply { addRect(frame) }
                    clipPath(path = selectionPath, clipOp = ClipOp.Difference) {
                        drawRect(color = maskColor, size = size)
                        println("3333333333333333")
                    }
                    drawRect(
                        color = borderColor,
                        topLeft = Offset(frame.left, frame.top),
                        size = Size(frame.width, frame.height),
                        style = Stroke(borderWidth.toPx())
                    )
                }
            }
    )
}

@Composable
private fun DistanceScaleOverlay(
    frame: Rect,
    widthText: String,
    heightText: String,
    guideColor: Color = Color(0xFF4FA0FF),
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
                // 标尺线宽度（像素）
                val strokePx = 1.dp.toPx()
                // 端点刻度长度（像素）
                val tickLengthPx = 8.dp.toPx()
                // 标尺线与边框左边的间距（像素）
                val gapPx = tickLengthPx
                // 左右内边距（像素）
                val labelPaddingHorizontalPx = 0.dp.toPx()
                // 标签上下内边距（像素）
                val labelPaddingVerticalPx = 0.dp.toPx()
                // 标尺线与文字之间的最小间隔（像素）
                val lineTextGapPx = 1.dp.toPx()

                // 顶部标尺线 Y 坐标
                val topLineY = frame.top - tickLengthPx - gapPx

                // 顶部距离文案测量结果
                val widthTextLayout =
                    textMeasurer.measure(text = widthText, style = distanceLabelStyle)
                // 顶部标签总宽（含内边距）
                val widthLabelWidth = widthTextLayout.size.width + labelPaddingHorizontalPx * 2f
                // 顶部标签总高（含内边距）
                val widthLabelHeight = widthTextLayout.size.height + labelPaddingVerticalPx * 2f

                // 顶部标签左上角 X（水平居中到边框中心）
                val widthLabelLeft = frame.center.x - widthLabelWidth / 2f
                // 顶部标签左上角 Y（锚定边框上边）
                val widthLabelTop = frame.top - widthLabelHeight - tickLengthPx / 2
                // 顶部标尺线被文字占用区间左边界
                val widthLabelBlockLeft = widthLabelLeft - lineTextGapPx
                // 顶部标尺线被文字占用区间右边界
                val widthLabelBlockRight =
                    widthLabelLeft + widthTextLayout.size.width + lineTextGapPx

                // 当边框宽度不足以容纳顶部标签时，隐藏整组距离标注
                val shouldDrawDistance = frame.width > widthLabelWidth

                // 左侧标尺线 X 坐标
                val leftLineX = frame.left - tickLengthPx - gapPx

                // 左侧距离文案测量结果
                val heightTextLayout =
                    textMeasurer.measure(text = heightText, style = distanceLabelStyle)
                // 左侧标签总宽（含内边距，旋转前）
                val heightLabelWidth =
                    heightTextLayout.size.width + labelPaddingHorizontalPx * 2f
                // 左侧标签总高（含内边距，旋转前）
                val heightLabelHeight =
                    heightTextLayout.size.height + labelPaddingVerticalPx * 2f

                val heightLabelTop = frame.center.y - heightLabelWidth / 2
                val heightLabelBottom = frame.center.y + heightLabelWidth / 2
                val heightLabelBlockTop = heightLabelTop - lineTextGapPx
                val heightLabelBlockBottom = heightLabelBottom + lineTextGapPx

                // 左侧标签旋转中心点
                val heightLabelCenter = Offset(leftLineX, frame.center.y)

                onDrawWithContent {
                    drawContent()
                    // <editor-fold desc="中心辅助线" defaultstate="collapsed">
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
                    // </editor-fold>

                    if (!shouldDrawDistance) return@onDrawWithContent

                    // horizontal: |-----16KM-----|
                    drawLine(
                        color = guideColor,
                        start = Offset(frame.left, topLineY),
                        end = Offset(frame.left, topLineY + tickLengthPx),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(frame.left, topLineY + tickLengthPx / 2),
                        end = Offset(widthLabelBlockLeft, topLineY + tickLengthPx / 2),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(widthLabelBlockRight, topLineY + tickLengthPx / 2),
                        end = Offset(frame.right, topLineY + tickLengthPx / 2),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(frame.right, topLineY),
                        end = Offset(frame.right, topLineY + tickLengthPx),
                        strokeWidth = strokePx
                    )
                    drawText(
                        textLayoutResult = widthTextLayout,
                        topLeft = Offset(x = widthLabelLeft, y = widthLabelTop)
                    )

                    // vertical:
                    drawLine(
                        color = guideColor,
                        start = Offset(leftLineX, frame.top - strokePx),
                        end = Offset(leftLineX + tickLengthPx, frame.top - strokePx),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(leftLineX + tickLengthPx / 2, frame.top - strokePx),
                        end = Offset(leftLineX + tickLengthPx / 2, heightLabelBlockTop),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(leftLineX + tickLengthPx / 2, heightLabelBlockBottom),
                        end = Offset(leftLineX + tickLengthPx / 2, frame.bottom + strokePx),
                        strokeWidth = strokePx
                    )
                    drawLine(
                        color = guideColor,
                        start = Offset(leftLineX, frame.bottom + strokePx),
                        end = Offset(leftLineX + tickLengthPx, frame.bottom + strokePx),
                        strokeWidth = strokePx
                    )
                    rotate(degrees = -90f, pivot = heightLabelCenter) {
                        drawText(
                            textLayoutResult = heightTextLayout,
                            topLeft = Offset(
                                x = heightLabelCenter.x - heightLabelWidth / 2f,
                                y = heightLabelCenter.y - heightLabelHeight / 4
                            )
                        )
                    }
                }
            }

    )
}
