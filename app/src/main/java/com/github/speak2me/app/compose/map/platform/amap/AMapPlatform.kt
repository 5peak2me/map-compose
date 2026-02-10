package com.github.speak2me.app.compose.map.platform.amap

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import com.amap.api.maps.AMapUtils
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.Projection
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.platform.GeoPoint
import com.github.speak2me.app.compose.map.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.platform.MapController
import com.github.speak2me.app.compose.map.platform.MapPlatform
import com.github.speak2me.app.compose.map.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.platform.MapUiConfig
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
    override fun rememberController(initialCenter: GeoPoint, initialZoom: Float): MapController {
        val cameraState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(
                LatLng(initialCenter.latitude, initialCenter.longitude),
                initialZoom
            )
        }
        return AMapController(cameraState)
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
            modifier = modifier,
            cameraPositionState = amapController.cameraState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        )
    }
}

@Stable
private class AMapController(
    val cameraState: CameraPositionState,
) : MapController {
    override val zoom: Float
        get() = cameraState.position.zoom

    override val center: GeoPoint
        get() = cameraState.position.target.toGeoPoint()

    override val projection: MapScreenProjection?
        get() = cameraState.projection?.let(::AMapScreenProjection)

    override fun moveTo(center: GeoPoint, zoom: Float) {
        val current = cameraState.position
        cameraState.move(
            CameraUpdateFactory.newCameraPosition(
                CameraPosition(
                    LatLng(center.latitude, center.longitude),
                    zoom,
                    current.tilt,
                    current.bearing
                )
            )
        )
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
