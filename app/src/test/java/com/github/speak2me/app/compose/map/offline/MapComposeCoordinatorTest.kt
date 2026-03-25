package com.github.speak2me.app.compose.map.offline

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.IntSize
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraState
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.pow

class MapComposeCoordinatorTest {

    @Test
    fun `calibration policy returns pending when distance is non-positive`() {
        val policy = Log2CameraCalibrationPolicy(
            minWidthMeters = MIN_WIDTH_METERS,
            epsilon = INIT_ZOOM_EPSILON
        )

        val action = policy.evaluate(currentZoom = 12f, currentDistanceMeters = 0f)

        assertTrue(action is CameraCalibrationAction.Pending)
    }

    @Test
    fun `calibration policy returns move with expected target zoom`() {
        val policy = Log2CameraCalibrationPolicy(
            minWidthMeters = MIN_WIDTH_METERS,
            epsilon = INIT_ZOOM_EPSILON
        )

        val action = policy.evaluate(
            currentZoom = 12f,
            currentDistanceMeters = MIN_WIDTH_METERS * 2f
        )

        assertTrue(action is CameraCalibrationAction.MoveCamera)
        action as CameraCalibrationAction.MoveCamera
        assertFloatEquals(expected = 13f, actual = action.targetZoom)
    }

    @Test
    fun `calibration policy returns complete when delta is within epsilon`() {
        val epsilon = INIT_ZOOM_EPSILON
        val policy = Log2CameraCalibrationPolicy(
            minWidthMeters = MIN_WIDTH_METERS,
            epsilon = epsilon
        )
        val stableDistance = MIN_WIDTH_METERS * 2.0.pow((epsilon / 2f).toDouble()).toFloat()

        val action = policy.evaluate(currentZoom = 10f, currentDistanceMeters = stableDistance)

        assertTrue(action is CameraCalibrationAction.Complete)
        action as CameraCalibrationAction.Complete
        assertFloatEquals(expected = 10f, actual = action.maxZoomLimit)
    }

    @Test
    fun `frame metrics resolver composes frame and distance by sequence`() {
        val initialFrame = Rect(left = 0f, top = 0f, right = 200f, bottom = 100f)
        val constrainedFrame = Rect(left = 50f, top = 25f, right = 150f, bottom = 75f)
        val frameResolver = RecordingFrameResolver(
            initialFrame = initialFrame,
            constrainedFrame = constrainedFrame
        )
        val distanceCalculator = TestDistanceCalculator(
            initialFrame = initialFrame,
            constrainedFrame = constrainedFrame,
            initialDistance = Size(width = 80_000f, height = 40_000f),
            constrainedDistance = Size(width = 40_000f, height = 20_000f)
        )
        val resolver = DefaultFrameMetricsResolver(
            frameResolver = frameResolver,
            distanceCalculator = distanceCalculator
        )

        val metrics = resolver.resolve(
            containerSize = IntSize(1080, 720),
            aspectRatio = 2f / 3f,
            projection = null,
            mapPlatform = UnusedMapPlatform
        )

        assertEquals(initialFrame, metrics.initialFrame)
        assertFloatEquals(expected = 80_000f, actual = metrics.initialFrameWidthMeters)
        assertEquals(constrainedFrame, metrics.resolveFrame)
        assertEquals(Size(width = 40_000f, height = 20_000f), metrics.sizeInMeters)
        assertFloatEquals(expected = 80_000f, actual = frameResolver.lastFrameWidthMeters ?: -1f)
    }

    private fun assertFloatEquals(expected: Float, actual: Float, delta: Float = 0.0001f) {
        assertEquals(expected, actual, delta)
    }

    private class RecordingFrameResolver(
        private val initialFrame: Rect,
        private val constrainedFrame: Rect,
    ) : FrameResolver {
        var lastFrameWidthMeters: Float? = null

        override fun resolveFrame(
            containerSize: IntSize,
            aspectRatio: Float,
            frameWidthMeters: Float?,
        ): Rect {
            return if (frameWidthMeters == null) {
                initialFrame
            } else {
                lastFrameWidthMeters = frameWidthMeters
                constrainedFrame
            }
        }
    }

    private class TestDistanceCalculator(
        private val initialFrame: Rect,
        private val constrainedFrame: Rect,
        private val initialDistance: Size,
        private val constrainedDistance: Size,
    ) : DistanceCalculator {
        override fun calculateDistanceMeters(
            projection: com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection?,
            frame: Rect,
            mapPlatform: MapPlatform,
        ): FrameGroundSizeMeters {
            return when (frame) {
                initialFrame -> initialDistance
                constrainedFrame -> constrainedDistance
                else -> Size.Zero
            }
        }
    }

    private object UnusedMapPlatform : MapPlatform {
        override fun distanceMeters(from: GeoPoint, to: GeoPoint): Float = 0f

        @Composable
        override fun rememberCameraState(initialUpdate: CameraUpdate): MapCameraState {
            error("Unused in test")
        }

        @Composable
        override fun MapView(
            modifier: Modifier,
            cameraState: MapCameraState,
            cameraConstraint: MapCameraConstraint,
            uiConfig: MapUiConfig,
        ) = Unit
    }
}
