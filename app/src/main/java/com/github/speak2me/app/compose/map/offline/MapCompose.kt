package com.github.speak2me.app.compose.map.offline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.unit.center
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.github.speak2me.app.compose.map.offline.platform.amap.AMapPlatform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import java.util.Locale
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

private const val MIN_WIDTH_METERS = 16_000f
private const val DEFAULT_MAX_DISTANCE_METERS = 400_000f
private const val INITIAL_MARGIN_RATIO = 0.1f
private const val INIT_ZOOM_EPSILON = 0.0005f

private val defaultCenter = GeoPoint(31.846594, 117.125279)
typealias FrameDistanceMeters = Size

@Composable
fun MapCompose(
    modifier: Modifier = Modifier,
    mapPlatform: MapPlatform = remember { AMapPlatform() },
    maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
    onCameraChange: ((center: GeoPoint, zoom: Float) -> Unit)? = null,
    frameResolver: FrameResolver = remember(maxDistanceMeters) {
        DefaultFrameResolver(maxDistanceMeters = maxDistanceMeters)
    },
    distanceCalculator: DistanceCalculator = remember { PlatformDistanceCalculator() },
    distanceFormatter: DistanceFormatter = remember(maxDistanceMeters) {
        KilometerDistanceFormatter(
            minDistanceMeters = MIN_WIDTH_METERS,
            maxDistanceMeters = maxDistanceMeters
        )
    },
) {
    val mapController = mapPlatform.rememberController(
        initialCenter = defaultCenter,
        initialZoom = 12f
    )
    val uiConfig = remember { MapUiConfig() }
    var isInitCalibrated by remember { mutableStateOf(false) }
    var maxZoomLimit by remember { mutableStateOf<Float?>(null) }

    LaunchedEffect(mapController, onCameraChange) {
        val callback = onCameraChange ?: return@LaunchedEffect
        snapshotFlow {
            mapController.projection?.let { mapController.center to mapController.zoom }
        }
            .filterNotNull()
            .distinctUntilChanged()
            .collect { (center, zoom) ->
                callback(center, zoom)
            }
    }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val containerSize = rememberContainerSize(maxWidth, maxHeight, density)
        val aspectRatio = rememberAspectRatio(containerSize)
        val initialFrame = remember(containerSize, aspectRatio, frameResolver) {
            frameResolver.resolveFrame(
                containerSize = containerSize,
                aspectRatio = aspectRatio
            )
        }

        val frame = remember(
            containerSize,
            aspectRatio,
            mapController.zoom,
            mapController.projection,
            frameResolver,
            distanceCalculator,
            mapPlatform
        ) {
            val baseFrameWidthMeters = distanceCalculator.calculateDistanceMeters(
                projection = mapController.projection,
                frame = initialFrame,
                mapPlatform = mapPlatform
            ).width
            frameResolver.resolveFrame(
                containerSize = containerSize,
                aspectRatio = aspectRatio,
                frameWidthMeters = baseFrameWidthMeters
            )
        }

        val (widthMeters, heightMeters) = distanceCalculator.calculateDistanceMeters(
            projection = mapController.projection,
            frame = frame,
            mapPlatform = mapPlatform
        )

        LaunchedEffect(
            containerSize,
            initialFrame,
            mapController.projection,
            mapController.zoom
        ) {
            if (isInitCalibrated || containerSize.width <= 0) return@LaunchedEffect
            val currentDistance = distanceCalculator.calculateDistanceMeters(
                projection = mapController.projection,
                frame = initialFrame,
                mapPlatform = mapPlatform
            ).width
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
            SelectionFrameOverlay(frame = frame)
            DistanceScaleOverlay(
                frame = frame,
                distanceFormatter.format(widthMeters),
                distanceFormatter.format(heightMeters)
            )
        }
    }
}

/**
 * 边框求解器：对外暴露单一入口，初始化可以视作 frameWidthMeters 为空时的特殊缩放。
 */
interface FrameResolver {
    /**
     * 基于容器尺寸与可选宽度距离，计算当前应显示的边框。
     * frameWidthMeters 为空时返回初始边框；不为空时按约束进行缩放。
     */
    fun resolveFrame(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMeters: Float? = null,
    ): Rect
}

interface InitialFrameFactory {
    fun create(containerSize: IntSize, aspectRatio: Float): Rect
}

class TopInsetInitialFrameFactory(
    private val marginRatio: Float = INITIAL_MARGIN_RATIO,
) : InitialFrameFactory {
    override fun create(containerSize: IntSize, aspectRatio: Float): Rect {
        val containerWidth = containerSize.width.toFloat()
        val containerHeight = containerSize.height.toFloat()
        val margin = minOf(containerWidth, containerHeight) * marginRatio
        val width = (containerWidth - margin * 2f).coerceAtLeast(0f)
        val height = (width * aspectRatio).coerceAtLeast(0f)
        val halfWidth = width / 2f
        val halfHeight = height / 2f
        val centerX = containerSize.center.x.toFloat()
        val centerY = margin + halfHeight
        return Rect(
            left = centerX - halfWidth,
            top = centerY - halfHeight,
            right = centerX + halfWidth,
            bottom = centerY + halfHeight
        )
    }
}

interface FrameScalePolicy {
    fun apply(frame: Rect, frameWidthMeters: Float?): Rect
}

class MaxDistanceFrameScalePolicy(
    maxDistanceMeters: Float,
) : FrameScalePolicy {
    private val safeMaxDistanceMeters = maxDistanceMeters.coerceAtLeast(MIN_WIDTH_METERS)

    override fun apply(frame: Rect, frameWidthMeters: Float?): Rect {
        val widthMeters = frameWidthMeters ?: return frame
        if (widthMeters <= safeMaxDistanceMeters) return frame
        val scale = (safeMaxDistanceMeters / widthMeters).coerceIn(0f, 1f)
        return frame.scaleFromCenter(scale)
    }
}

class DefaultFrameResolver(
    private val initialFrameFactory: InitialFrameFactory,
    private val frameScalePolicy: FrameScalePolicy,
) : FrameResolver {
    constructor(maxDistanceMeters: Float) : this(
        initialFrameFactory = TopInsetInitialFrameFactory(),
        frameScalePolicy = MaxDistanceFrameScalePolicy(maxDistanceMeters = maxDistanceMeters),
    )

    override fun resolveFrame(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMeters: Float?,
    ): Rect {
        val initialFrame = initialFrameFactory.create(containerSize, aspectRatio)
        return frameScalePolicy.apply(initialFrame, frameWidthMeters)
    }
}

/**
 * 负责将边框在屏幕上的宽高转换为真实距离（米）。
 */
interface DistanceCalculator {
    /**
     * 计算指定边框对应的真实宽高距离（米）。
     */
    fun calculateDistanceMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): FrameDistanceMeters
}

class PlatformDistanceCalculator : DistanceCalculator {
    override fun calculateDistanceMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): FrameDistanceMeters {
        projection ?: return Size.Zero
        val centerX = frame.center.x.roundToInt()
        val centerY = frame.center.y.roundToInt()
        val left = projection.fromScreenLocation(frame.left.roundToInt(), centerY)
            ?: return Size.Zero
        val right = projection.fromScreenLocation(frame.right.roundToInt(), centerY)
            ?: return Size.Zero
        val top = projection.fromScreenLocation(centerX, frame.top.roundToInt())
            ?: return Size.Zero
        val bottom = projection.fromScreenLocation(centerX, frame.bottom.roundToInt())
            ?: return Size.Zero
        return Size(
            mapPlatform.distanceMeters(left, right),
            mapPlatform.distanceMeters(top, bottom)
        )
    }
}

/**
 * 负责格式化边框周边展示的距离文案。
 */
interface DistanceFormatter {
    /**
     * 将米单位距离转换为用户可读文案。
     */
    fun format(distanceMeters: Float): String
}

class KilometerDistanceFormatter(
    private val minDistanceMeters: Float = MIN_WIDTH_METERS,
    private val maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
) : DistanceFormatter {
    override fun format(distanceMeters: Float): String {
        val kilometers = distanceMeters.coerceIn(minDistanceMeters, maxDistanceMeters) / 1000f
        return "%.2f公里".format(Locale.getDefault(), kilometers)
    }
}

private fun log2(value: Float): Float = (ln(value.toDouble()) / ln(2.0)).toFloat()

@Composable
private fun rememberContainerSize(
    width: Dp,
    height: Dp,
    density: Density,
): IntSize = remember(width, height, density) {
    with(density) {
        IntSize(width.roundToPx(), height.roundToPx())
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

                val selectionPath = Path().apply { addRect(frame) }

                onDrawWithContent {
                    drawContent()
                    clipPath(path = selectionPath, clipOp = ClipOp.Difference) {
                        drawRect(color = maskColor, size = size)
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
                // 标尺线与文字之间的最小间隔（像素）
                val lineTextGapPx = 1.dp.toPx()

                // 顶部标尺线 Y 坐标
                val topLineY = frame.top - tickLengthPx - gapPx

                // 顶部距离文案测量结果
                val widthTextLayout =
                    textMeasurer.measure(text = widthText, style = distanceLabelStyle)
                val widthLabelWidth = widthTextLayout.size.width.toFloat()
                val widthLabelHeight = widthTextLayout.size.height.toFloat()

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
                val heightLabelWidth = heightTextLayout.size.width.toFloat()
                val heightLabelHeight = heightTextLayout.size.height.toFloat()

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
                                y = heightLabelCenter.y - heightLabelHeight / 4f,
                            )
                        )
                    }
                }
            }

    )
}
