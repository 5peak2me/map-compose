/*
 * Copyright © 2025 J!nl!n™ Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.speak2me.compose.map.tencent.ktx

import android.graphics.Bitmap
import android.location.Location
import com.github.speak2me.compose.map.tencent.ktx.model.circleOptions
import com.github.speak2me.compose.map.tencent.ktx.model.groundOverlayOptions
import com.github.speak2me.compose.map.tencent.ktx.model.markerOptions
import com.github.speak2me.compose.map.tencent.ktx.model.polygonOptions
import com.github.speak2me.compose.map.tencent.ktx.model.polylineOptions
import com.github.speak2me.compose.map.tencent.ktx.model.tileOverlayOptions
import com.tencent.tencentmap.mapsdk.maps.CameraUpdate
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.Circle
import com.tencent.tencentmap.mapsdk.maps.model.CircleOptions
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlay
import com.tencent.tencentmap.mapsdk.maps.model.GroundOverlayOptions
import com.tencent.tencentmap.mapsdk.maps.model.IndoorBuilding
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MapPoi
import com.tencent.tencentmap.mapsdk.maps.model.Marker
import com.tencent.tencentmap.mapsdk.maps.model.MarkerOptions
import com.tencent.tencentmap.mapsdk.maps.model.Polygon
import com.tencent.tencentmap.mapsdk.maps.model.PolygonOptions
import com.tencent.tencentmap.mapsdk.maps.model.Polyline
import com.tencent.tencentmap.mapsdk.maps.model.PolylineOptions
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlay
import com.tencent.tencentmap.mapsdk.maps.model.TileOverlayOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public sealed class CameraEvent(public open val position: CameraPosition)
public data class CameraChangeEvent(public override val position: CameraPosition) : CameraEvent(position)
public data class CameraChangeFinishEvent(public override val position: CameraPosition) : CameraEvent(position)

/**
 * Change event when a marker is dragged. See [TencentMap.setOnMarkerDragListener]
 */
public sealed class OnMarkerDragEvent {
    public abstract val marker: Marker
}

/**
 * Event emitted repeatedly while a marker is being dragged.
 */
public data class MarkerDragEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Event emitted when a marker has finished being dragged.
 */
public data class MarkerDragEndEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Event emitted when a marker starts being dragged.
 */
public data class MarkerDragStartEvent(public override val marker: Marker) : OnMarkerDragEvent()

/**
 * Change event when the indoor state changes. See [TencentMap.OnIndoorStateChangeListener]
 */
public sealed class IndoorChangeEvent

/**
 * Change event when an indoor level is activated.
 * See [TencentMap.OnIndoorStateChangeListener.onIndoorLevelActivated]
 */
public data class IndoorLevelActivatedEvent(val building: IndoorBuilding) : IndoorChangeEvent()

/**
 * A suspending function that awaits the completion of the [cameraUpdate] animation.
 *
 * @param cameraUpdate the [CameraUpdate] to apply on the map
 * @param durationMs the duration in milliseconds of the animation. Defaults to 3 seconds.
 */
public suspend inline fun TencentMap.awaitAnimateCamera(
    cameraUpdate: CameraUpdate,
    durationMs: Long = 3000
): Unit =
    suspendCancellableCoroutine { continuation ->
        animateCamera(cameraUpdate, durationMs, object : TencentMap.CancelableCallback {
            override fun onFinish() {
                continuation.resume(Unit)
            }

            override fun onCancel() {
                continuation.cancel()
            }
        })
    }

/**
 * A suspending function that awaits for the map to be loaded. Uses
 * [TencentMap.addOnMapLoadedCallback].
 */
public suspend inline fun TencentMap.awaitMapLoad(): Unit =
    suspendCoroutine { continuation ->
        addOnMapLoadedCallback {
            continuation.resume(Unit)
        }
    }

/**
 * Returns a flow that emits when the camera is idle. Using this to observe camera idle events will
 * override an existing listener (if any) to [TencentMap.setOnCameraChangeListener].
 */
public fun TencentMap.cameraIdleEvents(): Flow<CameraEvent> =
    callbackFlow {
        setOnCameraChangeListener(object : TencentMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition) {
                trySend(CameraChangeEvent(position))
            }

            override fun onCameraChangeFinished(position: CameraPosition) {
                trySend(CameraChangeFinishEvent(position))
            }
        })
        awaitClose {
            setOnCameraChangeListener(null)
        }
    }

/**
 * A suspending function that returns a bitmap snapshot of the current view of the map. Uses
 * [TencentMap.snapshot].
 *
 * @return the snapshot
 */
public suspend inline fun TencentMap.awaitSnapshot(config: Bitmap.Config = Bitmap.Config.ARGB_8888): Bitmap? =
    suspendCoroutine { continuation ->
        snapshot(
            {
                continuation.resume(it)
            }, config
        )
    }

/**
 * Returns a flow that emits when the indoor state changes. Using this to observe indoor state
 * change events will override an existing listener (if any) to
 * [TencentMap.setOnIndoorStateChangeListener]
 */
public fun TencentMap.indoorStateChangeEvents(): Flow<IndoorChangeEvent> =
    callbackFlow {
        setOnIndoorStateChangeListener(object : TencentMap.OnIndoorStateChangeListener {
            override fun onIndoorBuildingFocused(): Boolean = true

            override fun onIndoorLevelActivated(building: IndoorBuilding): Boolean {
                trySend(IndoorLevelActivatedEvent(building = building))
                return true
            }

            override fun onIndoorBuildingDeactivated(): Boolean = true
        })
        awaitClose {
            setOnIndoorStateChangeListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is clicked. Using this to observe
 * info window clicks will override an existing listener (if any) to
 * [TencentMap.setOnInfoWindowClickListener]
 */
public fun TencentMap.infoWindowClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowClickListener(object : TencentMap.OnInfoWindowClickListener {
            override fun onInfoWindowClick(marker: Marker) {
                trySend(marker)
            }

            override fun onInfoWindowClickLocation(p0: Int, p1: Int, p2: Int, p3: Int) {

            }

        })
        awaitClose {
            setOnInfoWindowClickListener(null)
        }
    }


/**
 * Returns a flow that emits when the map is clicked. Using this to observe map click events will
 * override an existing listener (if any) to [TencentMap.setOnMapClickListener]
 */
public fun TencentMap.mapClickEvents(): Flow<LatLng> =
    callbackFlow {
        setOnMapClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMapClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the map is long clicked. Using this to observe map click events
 * will override an existing listener (if any) to [TencentMap.setOnMapLongClickListener]
 */
public fun TencentMap.mapLongClickEvents(): Flow<LatLng> =
    callbackFlow {
        setOnMapLongClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMapLongClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker on the map is clicked. Using this to observe marker click
 * events will override an existing listener (if any) to [TencentMap.setOnMarkerClickListener]
 */
public fun TencentMap.markerClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnMarkerClickListener {
            trySend(it).isSuccess
        }
        awaitClose {
            setOnMarkerClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker is dragged. Using this to observer marker drag events
 * will override existing listeners (if any) to [TencentMap.setOnMarkerDragListener]
 */
public fun TencentMap.markerDragEvents(): Flow<OnMarkerDragEvent> =
    callbackFlow {
        setOnMarkerDragListener(object : TencentMap.OnMarkerDragListener {
            override fun onMarkerDragStart(marker: Marker) {
                trySend(MarkerDragStartEvent(marker = marker))
            }

            override fun onMarkerDrag(marker: Marker) {
                trySend(MarkerDragEvent(marker = marker))
            }

            override fun onMarkerDragEnd(marker: Marker) {
                trySend(MarkerDragEndEvent(marker = marker))
            }
        })
        awaitClose {
            setOnMarkerDragListener(null)
        }
    }

/**
 * Returns a flow that emits when the my location blue dot is clicked. Using this to observe my
 * location blue dot click events will override an existing listener (if any) to
 * [TencentMap.setOnMyLocationChangeListener]
 */
public fun TencentMap.myLocationChangeEvents(): Flow<Location> =
    callbackFlow {
        setOnMyLocationChangeListener {
            trySend(it)
        }
        awaitClose {
            setOnMyLocationChangeListener(null)
        }
    }

/**
 * Returns a flow that emits when a PointOfInterest is clicked. Using this to observe
 * PointOfInterest click events will override an existing listener (if any) to
 * [TencentMap.setOnMapClickListener]
 */
public fun TencentMap.poiClickEvents(): Flow<MapPoi> =
    callbackFlow {
        setOnMapPoiClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMapPoiClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a Polyline is clicked. Using this to observe Polyline click events
 * will override an existing listener (if any) to [TencentMap.setOnPolylineClickListener]
 */
public fun TencentMap.polylineClickEvents(): Flow<Polyline> =
    callbackFlow {
        setOnPolylineClickListener { polyline, _ ->
            trySend(polyline)
            true
        }
        awaitClose {
            setOnPolylineClickListener(null)
        }
    }

/**
 * Builds a new [TencentMapOptions] using the provided [optionsActions].
 *
 * @return the constructed [TencentMapOptions]
 */
public inline fun buildTencentMapOptions(optionsActions: TencentMapOptions.() -> Unit): TencentMapOptions =
    TencentMapOptions().apply(
        optionsActions
    )

/**
 * Adds a [Circle] to this [TencentMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun TencentMap.addCircle(optionsActions: CircleOptions.() -> Unit): Circle =
    this.addCircle(
        circleOptions(optionsActions)
    )

/**
 * Adds a [GroundOverlay] to this [TencentMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [GroundOverlay]
 */
public inline fun TencentMap.addGroundOverlay(optionsActions: GroundOverlayOptions.() -> Unit): GroundOverlay? =
    this.addGroundOverlay(
        groundOverlayOptions(optionsActions)
    )

/**
 * Adds a [Marker] to this [TencentMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Marker]
 */
public inline fun TencentMap.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker =
    this.addMarker(
        markerOptions(optionsActions)
    )

/**
 * Adds a [Polygon] to this [TencentMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polygon]
 */
public inline fun TencentMap.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon =
    this.addPolygon(
        polygonOptions(optionsActions)
    )

/**
 * Adds a [Polyline] to this [TencentMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun TencentMap.addPolyline(optionsActions: PolylineOptions.() -> Unit): Polyline =
    this.addPolyline(
        polylineOptions(optionsActions)
    )

/**
 * Adds a [TileOverlay] to this [TencentMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [TileOverlay]
 */
public inline fun TencentMap.addTileOverlay(optionsActions: TileOverlayOptions.() -> Unit): TileOverlay? =
    this.addTileOverlay(
        tileOverlayOptions(optionsActions)
    )

/**
 * @see [setOnInfoWindowClickListener](https://lbs.qq.com/mobile/androidMapSDK/developerGuide/infoWindow#4)
 */
public inline fun TencentMap.setOnInfoWindowClickListener(crossinline callback: (marker: Marker) -> Unit) {
    setOnInfoWindowClickListener(object : TencentMap.OnInfoWindowClickListener {
        override fun onInfoWindowClick(marker: Marker) {
            callback(marker)
        }

        override fun onInfoWindowClickLocation(width: Int, height: Int, x: Int, y: Int) = Unit
    })
}
