package com.github.speak2me.app.compose.map.offline

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraState
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.github.speak2me.app.compose.map.offline.platform.amap.AMapPlatform
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
internal fun MapCompose(
    modifier: Modifier = Modifier,
    mapPlatform: MapPlatform = remember { AMapPlatform() },
    initialUpdate: CameraUpdate = CameraUpdate.Center(
        center = defaultCenter,
        zoom = 12f
    ),
    cameraState: MapCameraState = mapPlatform.rememberCameraState(initialUpdate = initialUpdate),
    minDistanceMeters: Float = MIN_WIDTH_METERS,
    maxDistanceMeters: Float = DEFAULT_MAX_DISTANCE_METERS,
    distanceScaleResolver: DistanceScaleResolver = remember(maxDistanceMeters) {
        DefaultDistanceScaleResolver(
            distanceCalculator = PlatformDistanceCalculator(),
            distanceFormatter = KilometerDistanceFormatter(
                minDistanceMeters = minDistanceMeters,
                maxDistanceMeters = maxDistanceMeters
            )
        )
    },
    frameMetricsResolver: FrameMetricsResolver = remember(
        maxDistanceMeters,
        distanceScaleResolver
    ) {
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
    onSelectionChange: ((SelectionFrame) -> Unit)? = null,
) {
    MapComposeContent(
        modifier = modifier,
        mapPlatform = mapPlatform,
        cameraState = cameraState,
        onSelectionChange = onSelectionChange,
        distanceScaleResolver = distanceScaleResolver,
        frameMetricsResolver = frameMetricsResolver,
        cameraCalibrationPolicy = cameraCalibrationPolicy,
    )
}

@Composable
private fun MapComposeContent(
    modifier: Modifier,
    cameraState: MapCameraState,
    mapPlatform: MapPlatform,
    onSelectionChange: ((SelectionFrame) -> Unit)?,
    distanceScaleResolver: DistanceScaleResolver,
    frameMetricsResolver: FrameMetricsResolver,
    cameraCalibrationPolicy: CameraCalibrationPolicy,
) {
    val uiConfig = remember { MapUiConfig() }
    val currentOnSelectionChange by rememberUpdatedState(onSelectionChange)
    val density = LocalDensity.current

    BoxWithConstraints(modifier = modifier) {
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
        val frame = frameMetrics.resolveFrame

        val currentFrame by rememberUpdatedState(frame)
        val currentDistanceMeters by rememberUpdatedState(frameMetrics.sizeInMeters)
        var lastEmittedSelection by remember { mutableStateOf<SelectionFrame?>(null) }

        LaunchedEffect(cameraState) {
            var pendingSelection: SelectionFrame? = null

            @Suppress("OPT_IN_USAGE")
            cameraState.cameraSnapshotFlow().debounce(100L).collectLatest { snapshot ->
                if (snapshot.isMoving || currentOnSelectionChange == null) {
                    pendingSelection = null
                    return@collectLatest
                }

                val croppedBounds = cameraState.projection?.let { projection ->
                    currentFrame.toGeoBounds(projection)
                }
                val selectionFrame = croppedBounds?.let { bounds ->
                    SelectionFrame(bounds = bounds, size = currentDistanceMeters)
                } ?: return@collectLatest
                if (selectionFrame.size == Size.Zero) return@collectLatest
                if (selectionFrame.isApproximatelySame(lastEmittedSelection)) return@collectLatest

                pendingSelection = selectionFrame
//                    delay(SELECTION_EMIT_DEBOUNCE_MS)
                val latestSelection = pendingSelection ?: return@collectLatest
                if (latestSelection.isApproximatelySame(lastEmittedSelection)) return@collectLatest
                lastEmittedSelection = latestSelection
                currentOnSelectionChange?.invoke(latestSelection)
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
            MapComposeOverlay(
                frame = frame,
                widthText = distanceScaleResolver.format(frameMetrics.sizeInMeters.width),
                heightText = distanceScaleResolver.format(frameMetrics.sizeInMeters.height)
            )
        }
    }
}

private sealed interface MapComposeCalibrationState {
    data object Pending : MapComposeCalibrationState
    data class Calibrated(val maxZoomLimit: Float) : MapComposeCalibrationState
}

/**
 * [区域在线绘制](https://lbs.amap.com/demo/javascript-api-v2/example/overlayers/rectangle-draw-and-edit)
 */
private fun Rect.toGeoBounds(projection: MapScreenProjection): GeoBounds? {
    val nw = projection.fromScreenLocation(left.roundToInt(), top.roundToInt()) ?: return null
    val ne =
        projection.fromScreenLocation(right.roundToInt(), top.roundToInt()) ?: return null
    val sw =
        projection.fromScreenLocation(left.roundToInt(), bottom.roundToInt()) ?: return null
    val se =
        projection.fromScreenLocation(right.roundToInt(), bottom.roundToInt()) ?: return null

    val points = listOf(nw, ne, sw, se)
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

private fun SelectionFrame.isApproximatelySame(
    other: SelectionFrame?,
    boundsEpsilon: Double = 1e-6,
    sizeEpsilon: Float = 0.5f,
): Boolean {
    if (other == null) return false
    return bounds.isApproximatelySame(other.bounds, boundsEpsilon) &&
            size.isApproximatelySame(other.size, sizeEpsilon)
}

private fun FrameGroundSizeMeters.isApproximatelySame(
    other: FrameGroundSizeMeters,
    epsilon: Float,
): Boolean {
    return abs(width - other.width) <= epsilon &&
            abs(height - other.height) <= epsilon
}
