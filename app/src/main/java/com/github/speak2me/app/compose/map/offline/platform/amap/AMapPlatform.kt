package com.github.speak2me.app.compose.map.offline.platform.amap

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.Projection
import com.amap.api.maps.model.AMapPara.LineJoinType
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraState
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.Polygon
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sqrt
import com.amap.api.maps.CameraUpdate as AMapCameraUpdate
import com.amap.api.maps.model.CameraPosition as AMapCameraPosition
import com.github.speak2me.app.compose.map.offline.platform.CameraPosition as CoreCameraPosition

class AMapPlatform : MapPlatform {

    override fun distanceMeters(from: GeoPoint, to: GeoPoint): Float {
        return AMapUtils.calculateLineDistance(
            LatLng(from.latitude, from.longitude),
            LatLng(to.latitude, to.longitude)
        )
    }

    @Composable
    override fun rememberCameraState(initialUpdate: CameraUpdate): MapCameraState {
        val initialCenter = initialUpdate.resolveFallbackCenter()
        val initialZoom = initialUpdate.resolveFallbackZoom()
        val scope = rememberCoroutineScope()
        val cameraState = rememberCameraPositionState {
            position = AMapCameraPosition.fromLatLngZoom(
                LatLng(initialCenter.latitude, initialCenter.longitude),
                initialZoom
            )
        }
        return AMapCameraState(
            coroutineScope = scope,
            cameraState = cameraState,
            initialUpdate = initialUpdate
        )
    }

    @Composable
    override fun MapView(
        modifier: Modifier,
        cameraState: MapCameraState,
        cameraConstraint: MapCameraConstraint,
        uiConfig: MapUiConfig,
    ) {
        val amapCameraState = cameraState as? AMapCameraState
            ?: error("AMapPlatform requires AMapCameraState")

        val mapProperties = MapProperties(
            minZoomPreference = cameraConstraint.minZoom ?: 3f,
            maxZoomPreference = cameraConstraint.maxZoom ?: 20f
        )
        val mapUiSettings = MapUiSettings(
            compassEnabled = uiConfig.compassEnabled,
            indoorLevelPickerEnabled = uiConfig.indoorLevelPickerEnabled,
            mapToolbarEnabled = uiConfig.mapToolbarEnabled,
            myLocationButtonEnabled = uiConfig.myLocationButtonEnabled,
            rotationGesturesEnabled = uiConfig.rotationGesturesEnabled,
            scaleControlsEnabled = uiConfig.scaleControlsEnabled,
            tiltGesturesEnabled = uiConfig.tiltGesturesEnabled,
            zoomControlsEnabled = uiConfig.zoomControlsEnabled
        )
        val innerStrokeWidth = 12f
        val outerStrokeWidth = 12f
        val borderSpacingPx = 0f
        val innerBorderPoints = demoPolygonPoints.offsetOutwardByPixels(
            zoom = amapCameraState.cameraState.position.zoom,
//            outwardPixels = innerStrokeWidth / 3f + borderSpacingPx
            outwardPixels = innerStrokeWidth / 3f + borderSpacingPx
        )
        val outerBorderPoints = demoPolygonPoints.offsetOutwardByPixels(
            zoom = amapCameraState.cameraState.position.zoom,
            outwardPixels = innerStrokeWidth / 3f * 2 + borderSpacingPx/* + outerStrokeWidth / 2f*/
        )
        AMap(
            modifier = modifier.onSizeChanged(amapCameraState::onViewportChanged),
            cameraPositionState = amapCameraState.cameraState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        ) {
            Polygon(
                points = demoPolygonPoints,
                fillColor = Color.Gray.copy(alpha = 0.42f),
                strokeColor = Color.Transparent,
                strokeWidth = 0f,
                zIndex = 1f
            )
            Polygon(
                points = outerBorderPoints,
                fillColor = Color.Transparent,
                strokeColor = Color(0xFF4A90E2).copy(alpha = 0.55f),
                strokeJointType = LineJoinType.LineJoinRound,
                strokeWidth = outerStrokeWidth,
                zIndex = 2f
            )
            Polygon(
                points = innerBorderPoints,
                fillColor = Color.Transparent,
                strokeColor = Color.Gray.copy(alpha = 0.75f),
                strokeJointType = LineJoinType.LineJoinRound,
                strokeWidth = innerStrokeWidth,
                zIndex = 3f
            )
        }
    }
}

@Stable
private class AMapCameraState(
    private val coroutineScope: CoroutineScope,
    val cameraState: CameraPositionState,
    initialUpdate: CameraUpdate,
) : MapCameraState {
    private var viewportSize: IntSize = IntSize.Zero
    private var pendingFitRequest: PendingFitRequest? = initialUpdate.toPendingFitRequestOrNull()

    override val position: CoreCameraPosition
        get() = cameraState.position.toCoreCameraPosition()

    override val isMoving: Boolean
        get() = cameraState.isMoving

    override val projection: MapScreenProjection?
        get() = cameraState.projection?.let(::AMapScreenProjection)

    override val visibleBounds: GeoBounds?
        get() = cameraState.projection?.visibleRegion?.latLngBounds?.toGeoBounds()

    override fun move(update: CameraUpdate) {
        when (update) {
            is CameraUpdate.Center -> {
                cameraState.move(update.toPlatformUpdate(cameraState.position))
            }

            is CameraUpdate.FitBounds -> {
                applyFitBounds(update.bounds, update.padding, pendingUpdate = update)
            }
            else -> return
        }
    }

    override suspend fun animate(update: CameraUpdate, durationMs: Int) {
        when (update) {
            is CameraUpdate.Center -> {
                cameraState.animate(update.toPlatformUpdate(cameraState.position), durationMs)
            }

            is CameraUpdate.FitBounds -> {
                val fitUpdate = buildFitBoundsUpdate(update.bounds, update.padding)
                if (fitUpdate == null) {
                    pendingFitRequest = PendingFitRequest(
                        update = update,
                        animated = true,
                        durationMs = durationMs
                    )
                    return
                }
                pendingFitRequest = null
                cameraState.animate(fitUpdate, durationMs)
            }
            else -> return
        }
    }

    private fun applyFitBounds(
        bounds: GeoBounds,
        paddingPx: Int,
        pendingUpdate: CameraUpdate,
    ) {
        val fitUpdate = buildFitBoundsUpdate(bounds, paddingPx)
        if (fitUpdate == null) {
            pendingFitRequest = PendingFitRequest(
                update = pendingUpdate,
                animated = false
            )
            return
        }
        pendingFitRequest = null
        cameraState.move(fitUpdate)
    }

    private fun buildFitBoundsUpdate(
        bounds: GeoBounds,
        paddingPx: Int,
    ): AMapCameraUpdate? {
        if (viewportSize.width <= 0 || viewportSize.height <= 0) return null
        return CameraUpdateFactory.newLatLngBounds(
            bounds.toLatLngBounds(),
            paddingPx
        )
    }

    fun onViewportChanged(size: IntSize) {
        viewportSize = size
        val request = pendingFitRequest ?: return
        pendingFitRequest = null
        if (request.animated) {
            coroutineScope.launch {
                animate(update = request.update, durationMs = request.durationMs)
            }
        } else {
            move(update = request.update)
        }
    }
}

private data class PendingFitRequest(
    val update: CameraUpdate,
    val animated: Boolean,
    val durationMs: Int = Int.MAX_VALUE,
)

private class AMapScreenProjection(
    private val projection: Projection,
) : MapScreenProjection {
    override fun fromScreenLocation(x: Int, y: Int): GeoPoint? {
        val latLng = projection.fromScreenLocation(Point(x, y))
        return latLng?.toGeoPoint()
    }
}

private fun LatLng.toGeoPoint(): GeoPoint = GeoPoint(
    latitude = latitude,
    longitude = longitude
)

private fun LatLngBounds.toGeoBounds(): GeoBounds = GeoBounds(
    southwest = southwest.toGeoPoint(),
    northeast = northeast.toGeoPoint()
)

private fun GeoBounds.toLatLngBounds(): LatLngBounds {
    return LatLngBounds.Builder()
        .include(LatLng(southwest.latitude, southwest.longitude))
        .include(LatLng(northeast.latitude, northeast.longitude))
        .build()
}

private fun CameraUpdate.resolveFallbackCenter(): GeoPoint = when (this) {
    is CameraUpdate.Center -> center
    is CameraUpdate.FitBounds -> GeoPoint(
        latitude = (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0,
        longitude = (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
    )
    else -> GeoPoint(latitude = 0.0, longitude = 0.0)
}

private fun CameraUpdate.resolveFallbackZoom(): Float = when (this) {
    is CameraUpdate.Center -> zoom
    is CameraUpdate.FitBounds -> 12f
    else -> 12f
}

private fun CameraUpdate.toPendingFitRequestOrNull(): PendingFitRequest? = when (this) {
    is CameraUpdate.FitBounds -> PendingFitRequest(update = this, animated = false)
    else -> null
}

private fun CameraUpdate.Center.toPlatformUpdate(
    currentPosition: AMapCameraPosition,
): AMapCameraUpdate {
    return CameraUpdateFactory.newCameraPosition(
        AMapCameraPosition(
            LatLng(center.latitude, center.longitude),
            zoom,
            currentPosition.tilt,
            currentPosition.bearing
        )
    )
}

private fun AMapCameraPosition.toCoreCameraPosition(): CoreCameraPosition = CoreCameraPosition(
    center = target.toGeoPoint(),
    zoom = zoom,
    tilt = tilt,
    bearing = bearing
)

private val demoPolygonSeedPoints = listOf(
    LatLng(31.8700, 117.0800),
    LatLng(31.8900, 117.1450),
    LatLng(31.8350, 117.1850),
    LatLng(31.8050, 117.0950),
)

private val demoPolygonPoints = demoPolygonSeedPoints.expandFromCentroid(scale = 2.75)

private fun List<LatLng>.expandFromCentroid(scale: Double): List<LatLng> {
    if (isEmpty()) return this
    val centerLatitude = sumOf(LatLng::latitude) / size
    val centerLongitude = sumOf(LatLng::longitude) / size
    return map { point ->
        LatLng(
            centerLatitude + (point.latitude - centerLatitude) * scale,
            centerLongitude + (point.longitude - centerLongitude) * scale
        )
    }
}

private fun List<LatLng>.offsetOutwardByPixels(
    zoom: Float,
    outwardPixels: Float,
): List<LatLng> {
    if (size < 3 || outwardPixels <= 0f) return this
    val centroid = centroid()
    val metersPerPixel = metersPerPixel(
        latitude = centroid.latitude,
        zoom = zoom
    )
    val outwardMeters = outwardPixels * metersPerPixel
    if (outwardMeters <= 0.0) return this

    val metersPerDegreeLat = 111_320.0
    val metersPerDegreeLon = (111_320.0 * cos(Math.toRadians(centroid.latitude)))
        .coerceAtLeast(1e-6)

    return map { point ->
        val dxMeters = (point.longitude - centroid.longitude) * metersPerDegreeLon
        val dyMeters = (point.latitude - centroid.latitude) * metersPerDegreeLat
        val distance = sqrt(dxMeters * dxMeters + dyMeters * dyMeters)
        if (distance <= 1e-6) {
            point
        } else {
            val scale = (distance + outwardMeters) / distance
            LatLng(
                centroid.latitude + (dyMeters * scale) / metersPerDegreeLat,
                centroid.longitude + (dxMeters * scale) / metersPerDegreeLon
            )
        }
    }
}

private fun List<LatLng>.centroid(): LatLng {
    if (isEmpty()) return LatLng(0.0, 0.0)
    return LatLng(sumOf(LatLng::latitude) / size, sumOf(LatLng::longitude) / size)
}

private fun metersPerPixel(latitude: Double, zoom: Float): Double {
    val safeZoom = zoom.coerceAtLeast(0f)
    return (156543.03392 * cos(Math.toRadians(latitude))) / 2.0.pow(safeZoom.toDouble())
}
