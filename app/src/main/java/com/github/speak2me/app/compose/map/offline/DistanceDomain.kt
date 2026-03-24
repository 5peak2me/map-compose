package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import java.util.Locale
import kotlin.math.roundToInt

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

/**
 * 对外统一的距离能力：既能计算米制距离，也能格式化展示文案。
 */
interface DistanceScaleResolver : DistanceCalculator, DistanceFormatter

class DefaultDistanceScaleResolver(
    private val distanceCalculator: DistanceCalculator,
    private val distanceFormatter: DistanceFormatter,
) : DistanceScaleResolver {
    override fun calculateDistanceMeters(
        projection: MapScreenProjection?,
        frame: Rect,
        mapPlatform: MapPlatform,
    ): FrameDistanceMeters = distanceCalculator.calculateDistanceMeters(
        projection = projection,
        frame = frame,
        mapPlatform = mapPlatform
    )

    override fun format(distanceMeters: Float): String {
        return distanceFormatter.format(distanceMeters)
    }
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
