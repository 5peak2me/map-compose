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
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraState
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.github.speak2me.app.compose.map.offline.platform.amap.AMapPlatform
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MapCompose(
    modifier: Modifier = Modifier,
    mapPlatform: MapPlatform = remember { AMapPlatform() },
    initialUpdate: CameraUpdate = CameraUpdate.Center(
        center = defaultCenter,
        zoom = 12f
    ),
    cameraState: MapCameraState = mapPlatform.rememberCameraState(initialUpdate = initialUpdate),
    maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
    onBoundsChange: ((GeoBounds) -> Unit)? = null,
    distanceScaleResolver: DistanceScaleResolver = remember(maxDistanceMeters) {
        DefaultDistanceScaleResolver(
            distanceCalculator = PlatformDistanceCalculator(),
            distanceFormatter = KilometerDistanceFormatter(
                minDistanceMeters = MIN_WIDTH_METERS,
                maxDistanceMeters = maxDistanceMeters
            )
        )
    },
    frameMetricsResolver: FrameMetricsResolver = remember(maxDistanceMeters, distanceScaleResolver) {
        DefaultFrameMetricsResolver(
            frameResolver = DefaultFrameResolver(maxDistanceMeters = maxDistanceMeters),
            distanceCalculator = distanceScaleResolver
        )
    },
    cameraCalibrationPolicy: CameraCalibrationPolicy = remember {
        Log2CameraCalibrationPolicy(
            minWidthMeters = MIN_WIDTH_METERS,
            epsilon = INIT_ZOOM_EPSILON
        )
    },
) {
    MapComposeContent(
        modifier = modifier,
        mapPlatform = mapPlatform,
        cameraState = cameraState,
        onBoundsChange = onBoundsChange,
        onCameraChange = null,
        distanceScaleResolver = distanceScaleResolver,
        frameMetricsResolver = frameMetricsResolver,
        cameraCalibrationPolicy = cameraCalibrationPolicy,
    )
}

@Deprecated(
    message = "Use overload with onBoundsChange or consume cameraState.cameraSnapshotFlow()."
)
@Composable
fun MapCompose(
    modifier: Modifier = Modifier,
    mapPlatform: MapPlatform = remember { AMapPlatform() },
    initialUpdate: CameraUpdate = CameraUpdate.Center(
        center = defaultCenter,
        zoom = 12f
    ),
    cameraState: MapCameraState = mapPlatform.rememberCameraState(initialUpdate = initialUpdate),
    maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
    onCameraChange: ((center: GeoPoint, zoom: Float, bounds: GeoBounds) -> Unit)?,
    distanceScaleResolver: DistanceScaleResolver = remember(maxDistanceMeters) {
        DefaultDistanceScaleResolver(
            distanceCalculator = PlatformDistanceCalculator(),
            distanceFormatter = KilometerDistanceFormatter(
                minDistanceMeters = MIN_WIDTH_METERS,
                maxDistanceMeters = maxDistanceMeters
            )
        )
    },
    frameMetricsResolver: FrameMetricsResolver = remember(maxDistanceMeters, distanceScaleResolver) {
        DefaultFrameMetricsResolver(
            frameResolver = DefaultFrameResolver(maxDistanceMeters = maxDistanceMeters),
            distanceCalculator = distanceScaleResolver
        )
    },
    cameraCalibrationPolicy: CameraCalibrationPolicy = remember {
        Log2CameraCalibrationPolicy(
            minWidthMeters = MIN_WIDTH_METERS,
            epsilon = INIT_ZOOM_EPSILON
        )
    },
) {
    MapComposeContent(
        modifier = modifier,
        mapPlatform = mapPlatform,
        cameraState = cameraState,
        onBoundsChange = null,
        onCameraChange = onCameraChange,
        distanceScaleResolver = distanceScaleResolver,
        frameMetricsResolver = frameMetricsResolver,
        cameraCalibrationPolicy = cameraCalibrationPolicy,
    )
}

@Composable
private fun MapComposeContent(
    modifier: Modifier,
    mapPlatform: MapPlatform,
    cameraState: MapCameraState,
    onBoundsChange: ((GeoBounds) -> Unit)?,
    onCameraChange: ((center: GeoPoint, zoom: Float, bounds: GeoBounds) -> Unit)?,
    distanceScaleResolver: DistanceScaleResolver,
    frameMetricsResolver: FrameMetricsResolver,
    cameraCalibrationPolicy: CameraCalibrationPolicy,
) {
    val uiConfig = remember { MapUiConfig() }

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val containerSize = rememberContainerSize(maxWidth, maxHeight, density)
        val aspectRatio = rememberAspectRatio(containerSize)
        var calibrationState by remember(containerSize) {
            mutableStateOf<MapComposeCalibrationState>(MapComposeCalibrationState.Pending)
        }

        val frameMetrics = remember(
            containerSize,
            aspectRatio,
            cameraState.zoom,
            cameraState.projection,
            frameMetricsResolver,
            mapPlatform
        ) {
            frameMetricsResolver.resolve(
                containerSize = containerSize,
                aspectRatio = aspectRatio,
                projection = cameraState.projection,
                mapPlatform = mapPlatform
            )
        }
        val frame = frameMetrics.frame
        val widthMeters = frameMetrics.distanceMeters.width
        val heightMeters = frameMetrics.distanceMeters.height

        LaunchedEffect(cameraState, frame, onBoundsChange, onCameraChange) {
            val boundsCallback = onBoundsChange
            val cameraCallback = onCameraChange
            if (boundsCallback == null && cameraCallback == null) return@LaunchedEffect
            var lastEmittedBounds: GeoBounds? = null
            cameraState.cameraSnapshotFlow().collect { snapshot ->
                if (boundsCallback != null && !snapshot.isMoving) {
                    val croppedBounds = cameraState.projection?.let { projection ->
                        frame.toGeoBounds(projection)
                    }
                    if (croppedBounds != null && !croppedBounds.isApproximatelySame(lastEmittedBounds)) {
                        lastEmittedBounds = croppedBounds
                        boundsCallback.invoke(croppedBounds)
                    }
                }
                cameraCallback?.invoke(
                    snapshot.position.center,
                    snapshot.position.zoom,
                    snapshot.visibleBounds
                )
            }
        }

        LaunchedEffect(
            containerSize,
            cameraState.zoom,
            frameMetrics.initialFrameWidthMeters,
            calibrationState,
            cameraCalibrationPolicy
        ) {
            if (containerSize.width <= 0) return@LaunchedEffect
            if (calibrationState is MapComposeCalibrationState.Calibrated) return@LaunchedEffect
            when (
                val action = cameraCalibrationPolicy.evaluate(
                    currentZoom = cameraState.zoom,
                    currentDistanceMeters = frameMetrics.initialFrameWidthMeters
                )
            ) {
                CameraCalibrationAction.Pending -> Unit
                is CameraCalibrationAction.MoveCamera -> {
                    cameraState.moveTo(center = cameraState.center, zoom = action.targetZoom)
                }
                is CameraCalibrationAction.Complete -> {
                    calibrationState = MapComposeCalibrationState.Calibrated(
                        maxZoomLimit = action.maxZoomLimit
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            mapPlatform.MapView(
                modifier = Modifier.fillMaxSize(),
                cameraState = cameraState,
                cameraConstraint = MapCameraConstraint(
                    minZoom = 3f,
                    maxZoom = (calibrationState as? MapComposeCalibrationState.Calibrated)?.maxZoomLimit
                ),
                uiConfig = uiConfig
            )
            SelectionFrameOverlay(frame = frame)
            DistanceScaleOverlay(
                frame = frame,
                distanceScaleResolver.format(widthMeters),
                distanceScaleResolver.format(heightMeters)
            )
        }
    }
}

private sealed interface MapComposeCalibrationState {
    data object Pending : MapComposeCalibrationState
    data class Calibrated(val maxZoomLimit: Float) : MapComposeCalibrationState
}

private fun Rect.toGeoBounds(projection: MapScreenProjection): GeoBounds? {
    val topLeft = projection.fromScreenLocation(left.roundToInt(), top.roundToInt()) ?: return null
    val topRight = projection.fromScreenLocation(right.roundToInt(), top.roundToInt()) ?: return null
    val bottomLeft = projection.fromScreenLocation(left.roundToInt(), bottom.roundToInt()) ?: return null
    val bottomRight = projection.fromScreenLocation(right.roundToInt(), bottom.roundToInt()) ?: return null

    val points = listOf(topLeft, topRight, bottomLeft, bottomRight)
    val minLatitude = points.minOf { it.latitude }
    val maxLatitude = points.maxOf { it.latitude }
    val minLongitude = points.minOf { it.longitude }
    val maxLongitude = points.maxOf { it.longitude }

    return GeoBounds(
        southwest = GeoPoint(latitude = minLatitude, longitude = minLongitude),
        northeast = GeoPoint(latitude = maxLatitude, longitude = maxLongitude)
    )
}

private fun GeoBounds.isApproximatelySame(
    other: GeoBounds?,
    epsilon: Double = 1e-6,
): Boolean {
    if (other == null) return false
    return southwest.isApproximatelySame(other.southwest, epsilon) &&
        northeast.isApproximatelySame(other.northeast, epsilon)
}

private fun GeoPoint.isApproximatelySame(other: GeoPoint, epsilon: Double): Boolean {
    return abs(latitude - other.latitude) <= epsilon &&
        abs(longitude - other.longitude) <= epsilon
}
