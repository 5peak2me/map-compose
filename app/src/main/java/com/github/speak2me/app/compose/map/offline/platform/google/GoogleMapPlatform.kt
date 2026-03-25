package com.github.speak2me.app.compose.map.offline.platform.google

import android.graphics.Point
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import com.github.speak2me.app.compose.map.offline.platform.CameraUpdate
import com.github.speak2me.app.compose.map.offline.platform.GeoBounds
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraConstraint
import com.github.speak2me.app.compose.map.offline.platform.MapCameraState
import com.github.speak2me.app.compose.map.offline.platform.MapPlatform
import com.github.speak2me.app.compose.map.offline.platform.MapScreenProjection
import com.github.speak2me.app.compose.map.offline.platform.MapUiConfig
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.Projection
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.github.speak2me.app.compose.map.offline.platform.CameraPosition as CoreCameraPosition
import com.google.android.gms.maps.CameraUpdate as GoogleCameraUpdate
import com.google.android.gms.maps.model.CameraPosition as GoogleCameraPosition

class GoogleMapPlatform : MapPlatform {

    override fun distanceMeters(from: GeoPoint, to: GeoPoint): Float {
        return SphericalUtil.computeDistanceBetween(
            LatLng(from.latitude, from.longitude),
            LatLng(to.latitude, to.longitude)
        ).toFloat()
    }

    @Composable
    override fun rememberCameraState(initialUpdate: CameraUpdate): MapCameraState {
        val initialCenter = initialUpdate.resolveFallbackCenter()
        val initialZoom = initialUpdate.resolveFallbackZoom()
        val scope = rememberCoroutineScope()
        val cameraState = rememberCameraPositionState {
            position = GoogleCameraPosition.fromLatLngZoom(
                LatLng(initialCenter.latitude, initialCenter.longitude),
                initialZoom
            )
        }
        return GoogleMapCameraState(
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
        val googleCameraState = cameraState as? GoogleMapCameraState
            ?: error("GoogleMapPlatform requires GoogleMapCameraState")

        val mapProperties = MapProperties(
            minZoomPreference = cameraConstraint.minZoom ?: 3f,
            maxZoomPreference = cameraConstraint.maxZoom ?: 21f
        )
        val mapUiSettings = MapUiSettings(
            compassEnabled = uiConfig.compassEnabled,
            indoorLevelPickerEnabled = uiConfig.indoorLevelPickerEnabled,
            mapToolbarEnabled = uiConfig.mapToolbarEnabled,
            myLocationButtonEnabled = uiConfig.myLocationButtonEnabled,
            rotationGesturesEnabled = uiConfig.rotationGesturesEnabled,
            tiltGesturesEnabled = uiConfig.tiltGesturesEnabled,
            zoomControlsEnabled = uiConfig.zoomControlsEnabled
        )

        GoogleMap(
            modifier = modifier.onSizeChanged(googleCameraState::onViewportChanged),
            cameraPositionState = googleCameraState.cameraState,
            properties = mapProperties,
            uiSettings = mapUiSettings
        )
    }
}

@Stable
private class GoogleMapCameraState(
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
        get() = cameraState.projection?.let(::GoogleMapScreenProjection)

    override val visibleBounds: GeoBounds?
        get() = cameraState.projection?.visibleRegion?.latLngBounds?.toGeoBounds()

    override fun move(update: CameraUpdate) {
        when (update) {
            is CameraUpdate.Center -> {
                cameraState.move(update.toPlatformUpdate(cameraState.position))
            }

            is CameraUpdate.FitBounds -> {
                applyFitBounds(update.bounds, update.paddingPx, pendingUpdate = update)
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
                val fitUpdate = buildFitBoundsUpdate(update.bounds, update.paddingPx)
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
    ): GoogleCameraUpdate? {
        if (viewportSize.width <= 0 || viewportSize.height <= 0) return null
        return CameraUpdateFactory.newLatLngBounds(
            bounds.toLatLngBounds(),
            viewportSize.width,
            viewportSize.height,
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

private class GoogleMapScreenProjection(
    private val projection: Projection,
) : MapScreenProjection {
    override fun fromScreenLocation(x: Int, y: Int): GeoPoint? {
        return projection.fromScreenLocation(Point(x, y))?.toGeoPoint()
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
    currentPosition: GoogleCameraPosition,
): GoogleCameraUpdate {
    return CameraUpdateFactory.newCameraPosition(
        GoogleCameraPosition(
            LatLng(center.latitude, center.longitude),
            zoom,
            currentPosition.tilt,
            currentPosition.bearing
        )
    )
}

private fun GoogleCameraPosition.toCoreCameraPosition(): CoreCameraPosition = CoreCameraPosition(
    center = target.toGeoPoint(),
    zoom = zoom,
    tilt = tilt,
    bearing = bearing
)
