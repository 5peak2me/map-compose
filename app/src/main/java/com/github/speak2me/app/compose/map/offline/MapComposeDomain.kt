package com.github.speak2me.app.compose.map.offline

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.center
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import java.util.Locale
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

// --- 核心模型 ---

internal data class FrameMetrics(
    val initialFrame: Rect,
    val initialFrameWidthMeters: Float,
    val resolveFrame: Rect,
    val sizeInMeters: Size,
)

internal data class SelectionFrame(
    val bounds: GeoBounds,
    val size: Size,
)

// --- 选区计算逻辑 ---

internal interface FrameResolver {
    fun resolve(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMetersProvider: (Rect) -> Float,
    ): FrameMetrics
}

internal interface InitialFrameFactory {
    fun create(containerSize: IntSize, aspectRatio: Float): Rect
}

internal class TopInsetInitialFrameFactory(
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

internal interface FrameScalePolicy {
    fun apply(frame: Rect, frameWidthMeters: Float?): Rect
}

internal class MaxDistanceFrameScalePolicy(
    maxDistanceMeters: Float,
) : FrameScalePolicy {
    private val safeMaxDistanceMeters = maxDistanceMeters.coerceAtLeast(MIN_WIDTH_METERS)

    override fun apply(frame: Rect, frameWidthMeters: Float?): Rect {
        val widthMeters = frameWidthMeters ?: return frame
        if (widthMeters <= safeMaxDistanceMeters) return frame
        val scale = (safeMaxDistanceMeters / widthMeters).coerceIn(0f, 1f)
        val rectCenter = frame.center
        val halfWidth = frame.width * scale / 2f
        val halfHeight = frame.height * scale / 2f
        return Rect(
            left = rectCenter.x - halfWidth,
            top = rectCenter.y - halfHeight,
            right = rectCenter.x + halfWidth,
            bottom = rectCenter.y + halfHeight
        )
    }
}

internal class DefaultFrameResolver(
    private val initialFrameFactory: InitialFrameFactory,
    private val frameScalePolicy: FrameScalePolicy,
) : FrameResolver {
    constructor(maxDistanceMeters: Float) : this(
        initialFrameFactory = TopInsetInitialFrameFactory(),
        frameScalePolicy = MaxDistanceFrameScalePolicy(maxDistanceMeters = maxDistanceMeters),
    )

    override fun resolve(
        containerSize: IntSize,
        aspectRatio: Float,
        frameWidthMetersProvider: (Rect) -> Float,
    ): FrameMetrics {
        val initialFrame = initialFrameFactory.create(containerSize, aspectRatio)
        val initialWidthMeters = frameWidthMetersProvider(initialFrame)
        val finalFrame = frameScalePolicy.apply(initialFrame, initialWidthMeters)

        return FrameMetrics(
            initialFrame = initialFrame,
            initialFrameWidthMeters = initialWidthMeters,
            resolveFrame = finalFrame,
            sizeInMeters = if (finalFrame === initialFrame) {
                Size(initialWidthMeters, initialWidthMeters * aspectRatio)
            } else {
                val finalWidth = frameWidthMetersProvider(finalFrame)
                Size(finalWidth, finalWidth * aspectRatio)
            }
        )
    }
}

// --- 距离计算与格式化 ---

internal interface DistanceCalculator {
    fun calculateDistanceMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Size
}

internal interface DistanceFormatter {
    fun format(distanceMeters: Float): String
}

@Stable
internal interface DistanceResolver : DistanceCalculator, DistanceFormatter

internal class DefaultDistanceResolver(
    private val calculator: DistanceCalculator,
    private val formatter: DistanceFormatter,
) : DistanceResolver, DistanceCalculator by calculator, DistanceFormatter by formatter

internal class PlatformDistanceCalculator : DistanceCalculator {
    override fun calculateDistanceMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): Size {
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

internal class KilometerDistanceFormatter(
    private val minDistanceMeters: Float = MIN_WIDTH_METERS,
    private val maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
) : DistanceFormatter {
    override fun format(distanceMeters: Float): String {
        val kilometers = distanceMeters.coerceIn(minDistanceMeters, maxDistanceMeters) / 1000f
        return "%.2f公里".format(Locale.getDefault(), kilometers)
    }
}

// --- 指标解析 ---

@Stable
internal interface FrameMetricsResolver {
    fun resolve(
        containerSize: IntSize,
        aspectRatio: Float,
        projection: MapScreenProjection?,
        mapPlatform: MapPlatform,
    ): FrameMetrics
}

internal class DefaultFrameMetricsResolver(
    private val frameResolver: FrameResolver,
    private val distanceCalculator: DistanceCalculator,
) : FrameMetricsResolver {
    override fun resolve(
        containerSize: IntSize,
        aspectRatio: Float,
        projection: MapScreenProjection?,
        mapPlatform: MapPlatform,
    ): FrameMetrics {
        return frameResolver.resolve(
            containerSize = containerSize,
            aspectRatio = aspectRatio,
            frameWidthMetersProvider = { frame ->
                distanceCalculator.calculateDistanceMeters(
                    projection = projection,
                    frame = frame,
                    mapPlatform = mapPlatform
                ).width
            }
        )
    }
}

// --- 相机校准策略 ---

internal sealed interface CameraCalibrationAction {
    data object Pending : CameraCalibrationAction
    data class MoveCamera(val targetZoom: Float) : CameraCalibrationAction
    data class Complete(val maxZoomLimit: Float) : CameraCalibrationAction
}

@Stable
internal interface CameraCalibrationPolicy {
    fun evaluate(currentZoom: Float, currentDistanceMeters: Float): CameraCalibrationAction
}

internal class Log2CameraCalibrationPolicy(
    private val minWidthMeters: Float,
    private val epsilon: Float,
    private val minZoom: Float = 3f,
    private val maxZoom: Float = 20f,
) : CameraCalibrationPolicy {
    override fun evaluate(
        currentZoom: Float,
        currentDistanceMeters: Float
    ): CameraCalibrationAction {
        if (currentDistanceMeters <= 0f) return CameraCalibrationAction.Pending
        val deltaZoom = log2(currentDistanceMeters / minWidthMeters)
        if (abs(deltaZoom) > epsilon) {
            val targetZoom = (currentZoom + deltaZoom).coerceIn(minZoom, maxZoom)
            return CameraCalibrationAction.MoveCamera(targetZoom)
        }
        return CameraCalibrationAction.Complete(maxZoomLimit = currentZoom)
    }
}
