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
package com.github.speak2me.compose.map.amap.ktx

import android.graphics.Bitmap
import android.location.Location
import com.amap.api.maps.AMap
import com.amap.api.maps.AMap.OnCameraChangeListener
import com.amap.api.maps.AMap.OnMapScreenShotListener
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.Circle
import com.amap.api.maps.model.CircleOptions
import com.amap.api.maps.model.GroundOverlay
import com.amap.api.maps.model.GroundOverlayOptions
import com.amap.api.maps.model.IndoorBuildingInfo
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import com.amap.api.maps.model.Poi as PointOfInterest
import com.amap.api.maps.model.Polygon
import com.amap.api.maps.model.PolygonOptions
import com.amap.api.maps.model.Polyline
import com.amap.api.maps.model.PolylineOptions
import com.amap.api.maps.model.TileOverlay
import com.amap.api.maps.model.TileOverlayOptions
import com.github.speak2me.compose.map.amap.ktx.model.circleOptions
import com.github.speak2me.compose.map.amap.ktx.model.groundOverlayOptions
import com.github.speak2me.compose.map.amap.ktx.model.tileOverlayOptions
import com.github.speak2me.compose.map.amap.ktx.model.markerOptions
import com.github.speak2me.compose.map.amap.ktx.model.polygonOptions
import com.github.speak2me.compose.map.amap.ktx.model.polylineOptions
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
 * Change event when a marker is dragged. See [AMap.setOnMarkerDragListener]
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
 * Change event when the indoor state changes. See [AMap.OnIndoorBuildingActiveListener]
 */
public sealed class IndoorChangeEvent

/**
 * Change event when an indoor level is activated.
 * See [AMap.OnIndoorBuildingActiveListener.OnIndoorBuilding]
 */
public data class IndoorLevelActivatedEvent(val building: IndoorBuildingInfo) : IndoorChangeEvent()

/**
 * A suspending function that awaits the completion of the [cameraUpdate] animation.
 *
 * @param cameraUpdate the [CameraUpdate] to apply on the map
 * @param durationMs the duration in milliseconds of the animation. Defaults to 3 seconds.
 */
public suspend inline fun AMap.awaitAnimateCamera(
    cameraUpdate: CameraUpdate,
    durationMs: Long = 3000,
): Unit = suspendCancellableCoroutine { continuation ->
    animateCamera(
        cameraUpdate,
        durationMs,
        object : AMap.CancelableCallback {
            override fun onFinish() {
                continuation.resume(Unit)
            }

            override fun onCancel() {
                continuation.cancel()
            }
        },
    )
}

/**
 * A suspending function that awaits for the map to be loaded. Uses
 * [AMap.setOnMapLoadedListener].
 */
public suspend inline fun AMap.awaitMapLoad(): Unit = suspendCoroutine { continuation ->
    setOnMapLoadedListener {
        continuation.resume(Unit)
    }
}

/**
 * Returns a flow that emits when the camera is idle. Using this to observe camera idle events will
 * override an existing listener (if any) to [AMap.addOnCameraChangeListener].
 */
public fun AMap.cameraIdleEvents(): Flow<CameraEvent> = callbackFlow {
    setOnCameraChangeListener(object : OnCameraChangeListener {
        override fun onCameraChange(position: CameraPosition) {
            trySend(CameraChangeEvent(position))
        }

        override fun onCameraChangeFinish(position: CameraPosition) {
            trySend(CameraChangeFinishEvent(position))
        }
    })
    awaitClose {
        setOnCameraChangeListener(null)
    }
}

/**
 * A suspending function that returns a bitmap snapshot of the current view of the map. Uses
 * [AMap.getMapScreenShot].
 *
 * @return the snapshot
 */
public suspend inline fun AMap.awaitSnapshot(): Bitmap? = suspendCoroutine { continuation ->
    getMapScreenShot(object : OnMapScreenShotListener {
        override fun onMapScreenShot(bitmap: Bitmap?) {
            continuation.resume(bitmap)
        }

        override fun onMapScreenShot(bitmap: Bitmap?, status: Int) {
        }
    })
}

public inline fun AMap.snapshot(crossinline callback: (Bitmap?) -> Unit): Unit =
    getMapScreenShot(object : OnMapScreenShotListener {
        override fun onMapScreenShot(bitmap: Bitmap?) {
            callback.invoke(bitmap)
        }

        override fun onMapScreenShot(bitmap: Bitmap?, status: Int) = Unit
    })

/**
 * Returns a flow that emits when the indoor state changes. Using this to observe indoor state
 * change events will override an existing listener (if any) to
 * [AMap.setOnIndoorBuildingActiveListener]
 */
public fun AMap.indoorStateChangeEvents(): Flow<IndoorChangeEvent> = callbackFlow {
    setOnIndoorBuildingActiveListener {
        trySend(IndoorLevelActivatedEvent(building = it))
    }
    awaitClose {
        setOnIndoorBuildingActiveListener(null)
    }
}

/**
 * Returns a flow that emits when a marker's info window is clicked. Using this to observe
 * info window clicks will override an existing listener (if any) to
 * [AMap.setOnInfoWindowClickListener]
 */
public fun AMap.infoWindowClickEvents(): Flow<Marker> = callbackFlow {
    setOnInfoWindowClickListener {
        trySend(it)
    }
    awaitClose {
        setOnInfoWindowClickListener(null)
    }
}

/**
 * Returns a flow that emits when the map is clicked. Using this to observe map click events will
 * override an existing listener (if any) to [AMap.setOnMapClickListener]
 */
public fun AMap.mapClickEvents(): Flow<LatLng> = callbackFlow {
    setOnMapClickListener {
        trySend(it)
    }
    awaitClose {
        setOnMapClickListener(null)
    }
}

/**
 * Returns a flow that emits when the map is long clicked. Using this to observe map click events
 * will override an existing listener (if any) to [AMap.setOnMapLongClickListener]
 */
public fun AMap.mapLongClickEvents(): Flow<LatLng> = callbackFlow {
    setOnMapLongClickListener {
        trySend(it)
    }
    awaitClose {
        setOnMapLongClickListener(null)
    }
}

/**
 * Returns a flow that emits when a marker on the map is clicked. Using this to observe marker click
 * events will override an existing listener (if any) to [AMap.addOnMarkerClickListener]
 */
public fun AMap.markerClickEvents(): Flow<Marker> = callbackFlow {
    setOnMarkerClickListener {
        trySend(it).isSuccess
    }
    awaitClose {
        setOnMarkerClickListener(null)
    }
}

/**
 * Returns a flow that emits when a marker is dragged. Using this to observer marker drag events
 * will override existing listeners (if any) to [AMap.setOnMarkerDragListener]
 */
public fun AMap.markerDragEvents(): Flow<OnMarkerDragEvent> = callbackFlow {
    setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
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
 * [AMap.setOnMyLocationChangeListener]
 */
public fun AMap.myLocationChangeEvents(): Flow<Location> = callbackFlow {
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
 * [AMap.setOnPOIClickListener]
 */
public fun AMap.poiClickEvents(): Flow<PointOfInterest> = callbackFlow {
    setOnPOIClickListener {
        trySend(it)
    }
    awaitClose {
        setOnPOIClickListener(null)
    }
}

/**
 * Returns a flow that emits when a Polyline is clicked. Using this to observe Polyline click events
 * will override an existing listener (if any) to [AMap.setOnPolylineClickListener]
 */
public fun AMap.polylineClickEvents(): Flow<Polyline> = callbackFlow {
    setOnPolylineClickListener {
        trySend(it)
    }
    awaitClose {
        setOnPolylineClickListener(null)
    }
}

/**
 * Builds a new [AMapOptions] using the provided [optionsActions].
 *
 * @return the constructed [AMapOptions]
 */
public inline fun buildAMapOptions(optionsActions: AMapOptions.() -> Unit): AMapOptions =
    AMapOptions().apply(
        optionsActions,
    )

/**
 * Adds a [Circle] to this [AMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun AMap.addCircle(optionsActions: CircleOptions.() -> Unit): Circle = this.addCircle(
    circleOptions(optionsActions),
)

/**
 * Adds a [GroundOverlay] to this [AMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun AMap.addGroundOverlay(optionsActions: GroundOverlayOptions.() -> Unit): GroundOverlay? =
    this.addGroundOverlay(
        groundOverlayOptions(optionsActions),
    )

/**
 * Adds a [Marker] to this [AMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Marker]
 */
public inline fun AMap.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker? =
    this.addMarker(
        markerOptions(optionsActions),
    )

/**
 * Adds a [Polygon] to this [AMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polygon]
 */
public inline fun AMap.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon =
    this.addPolygon(
        polygonOptions(optionsActions),
    )

/**
 * Adds a [Polyline] to this [AMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun AMap.addPolyline(optionsActions: PolylineOptions.() -> Unit): Polyline =
    this.addPolyline(
        polylineOptions(optionsActions),
    )

/**
 * Adds a [TileOverlay] to this [AMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun AMap.addTileOverlay(optionsActions: TileOverlayOptions.() -> Unit): TileOverlay? =
    this.addTileOverlay(
        tileOverlayOptions(optionsActions),
    )
