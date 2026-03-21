package com.github.speak2me.app.compose.map.offline.platform.amap

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntSize
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.Projection
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.GeoPolygon
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapController
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.rememberCameraPositionState

class AMapPlatform : MapPlatform {

    override fun distanceMeters(from: GeoPoint, to: GeoPoint): Float {
        return AMapUtils.calculateLineDistance(
            LatLng(from.latitude, from.longitude),
            LatLng(to.latitude, to.longitude)
        )
    }

    @Composable
    override fun rememberController(initialUpdate: CameraUpdate): MapController {
        val initialCenter = initialUpdate.resolveFallbackCenter()
        val initialZoom = initialUpdate.resolveFallbackZoom()
        val cameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(initialCenter.latitude, initialCenter.longitude),
                initialZoom
            )
        }
        return AMapController(
            cameraState = cameraState,
            initialUpdate = initialUpdate
        )
    }

    @Composable
    override fun MapView(
        modifier: Modifier,
        controller: MapController,
        cameraConstraint: MapCameraConstraint,
        uiConfig: MapUiConfig,
    ) {
        val amapController = controller as? AMapController
            ?: error("AMapPlatform requires AMapController")

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
        AMap(
            modifier = modifier.onSizeChanged(amapController::onViewportChanged),
            cameraPositionState = amapController.cameraState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        )
    }
}

@Stable
private class AMapController(
    val cameraState: CameraPositionState,
    initialUpdate: CameraUpdate,
) : MapController {
    private var viewportSize: IntSize = IntSize.Zero
    private var pendingFitUpdate: CameraUpdate? = when (initialUpdate) {
        is CameraUpdate.FitBounds -> initialUpdate
        is CameraUpdate.FitPolygon -> initialUpdate
        is CameraUpdate.CenterZoom -> null
    }

    override val zoom: Float
        get() = cameraState.position.zoom

    override val center: GeoPoint
        get() = cameraState.position.target.toGeoPoint()

    override val projection: MapScreenProjection?
        get() = cameraState.projection?.let(::AMapScreenProjection)

    override fun move(update: CameraUpdate, animated: Boolean) {
        when (update) {
            is CameraUpdate.CenterZoom -> {
                val current = cameraState.position
                cameraState.move(
                    CameraUpdateFactory.newCameraPosition(
                        CameraPosition(
                            LatLng(update.center.latitude, update.center.longitude),
                            update.zoom,
                            current.tilt,
                            current.bearing
                        )
                    )
                )
            }

            is CameraUpdate.FitBounds -> {
                applyFitBounds(update.bounds, update.paddingPx, pendingUpdate = update)
            }

            is CameraUpdate.FitPolygon -> {
                val bounds = update.polygon.toGeoBoundsOrNull() ?: return
                applyFitBounds(bounds, update.paddingPx, pendingUpdate = update)
            }
        }
    }

    private fun applyFitBounds(
        bounds: GeoBounds,
        paddingPx: Int,
        pendingUpdate: CameraUpdate,
    ) {
        if (viewportSize.width <= 0 || viewportSize.height <= 0) {
            pendingFitUpdate = pendingUpdate
            return
        }
        pendingFitUpdate = null
        cameraState.move(
            CameraUpdateFactory.newLatLngBounds(
                bounds.toLatLngBounds(),
                paddingPx
            )
        )
    }

    fun onViewportChanged(size: IntSize) {
        viewportSize = size
        val request = pendingFitUpdate ?: return
        move(update = request, animated = false)
    }
}

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

private fun GeoBounds.toLatLngBounds(): LatLngBounds {
    return LatLngBounds.Builder()
        .include(LatLng(southwest.latitude, southwest.longitude))
        .include(LatLng(northeast.latitude, northeast.longitude))
        .build()
}

private fun GeoPolygon.toGeoBoundsOrNull(): GeoBounds? {
    if (points.isEmpty()) return null
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    var minLng = Double.POSITIVE_INFINITY
    var maxLng = Double.NEGATIVE_INFINITY
    points.forEach { point ->
        minLat = minOf(minLat, point.latitude)
        maxLat = maxOf(maxLat, point.latitude)
        minLng = minOf(minLng, point.longitude)
        maxLng = maxOf(maxLng, point.longitude)
    }
    return GeoBounds(
        southwest = GeoPoint(latitude = minLat, longitude = minLng),
        northeast = GeoPoint(latitude = maxLat, longitude = maxLng)
    )
}

private fun CameraUpdate.resolveFallbackCenter(): GeoPoint = when (this) {
    is CameraUpdate.CenterZoom -> center
    is CameraUpdate.FitBounds -> GeoPoint(
        latitude = (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0,
        longitude = (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
    )
    is CameraUpdate.FitPolygon -> polygon.toGeoBoundsOrNull()?.let { bounds ->
        GeoPoint(
            latitude = (bounds.southwest.latitude + bounds.northeast.latitude) / 2.0,
            longitude = (bounds.southwest.longitude + bounds.northeast.longitude) / 2.0
        )
    } ?: GeoPoint(latitude = 0.0, longitude = 0.0)
}

private fun CameraUpdate.resolveFallbackZoom(): Float = when (this) {
    is CameraUpdate.CenterZoom -> zoom
    is CameraUpdate.FitBounds -> 12f
    is CameraUpdate.FitPolygon -> 12f
}
