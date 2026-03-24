package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import kotlin.math.abs
import kotlin.math.log2

data class FrameMetrics(
    val initialFrame: Rect,
    val initialFrameWidthMeters: Float,
    val frame: Rect,
    val distanceMeters: FrameDistanceMeters,
)

interface FrameMetricsResolver {
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
        fun Rect.distanceMeters(): FrameDistanceMeters = distanceCalculator.calculateDistanceMeters(
            projection = projection,
            frame = this,
            mapPlatform = mapPlatform
        )

        val initialFrame = frameResolver.resolveFrame(
            containerSize = containerSize,
            aspectRatio = aspectRatio
        )
        val initialFrameDistance = initialFrame.distanceMeters()
        val initialFrameWidthMeters = initialFrameDistance.width
        val frame = frameResolver.resolveFrame(
            containerSize = containerSize,
            aspectRatio = aspectRatio,
            frameWidthMeters = initialFrameWidthMeters
        )
        return FrameMetrics(
            initialFrame = initialFrame,
            initialFrameWidthMeters = initialFrameWidthMeters,
            frame = frame,
            distanceMeters = frame.distanceMeters()
        )
    }
}

sealed interface CameraCalibrationAction {
    data object Pending : CameraCalibrationAction
    data class MoveCamera(val targetZoom: Float) : CameraCalibrationAction
    data class Complete(val maxZoomLimit: Float) : CameraCalibrationAction
}

interface CameraCalibrationPolicy {
    fun evaluate(currentZoom: Float, currentDistanceMeters: Float): CameraCalibrationAction
}

class Log2CameraCalibrationPolicy(
    private val minWidthMeters: Float,
    private val epsilon: Float,
    private val minZoom: Float = 3f,
    private val maxZoom: Float = 20f,
) : CameraCalibrationPolicy {
    override fun evaluate(currentZoom: Float, currentDistanceMeters: Float): CameraCalibrationAction {
        if (currentDistanceMeters <= 0f) return CameraCalibrationAction.Pending
        val deltaZoom = log2(currentDistanceMeters / minWidthMeters)
        if (abs(deltaZoom) > epsilon) {
            val targetZoom = (currentZoom + deltaZoom).coerceIn(minZoom, maxZoom)
            return CameraCalibrationAction.MoveCamera(targetZoom)
        }
        return CameraCalibrationAction.Complete(maxZoomLimit = currentZoom)
    }
}
