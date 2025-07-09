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
package com.github.speak2me.compose.map.huawei.ktx

import android.graphics.Bitmap
import android.location.Location
import androidx.annotation.IntDef
import com.github.speak2me.compose.map.huawei.ktx.model.circleOptions
import com.github.speak2me.compose.map.huawei.ktx.model.groundOverlayOptions
import com.github.speak2me.compose.map.huawei.ktx.model.markerOptions
import com.github.speak2me.compose.map.huawei.ktx.model.polygonOptions
import com.github.speak2me.compose.map.huawei.ktx.model.polylineOptions
import com.github.speak2me.compose.map.huawei.ktx.model.tileOverlayOptions
import com.huawei.hms.maps.CameraUpdate
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.HuaweiMapOptions
import com.huawei.hms.maps.model.Circle
import com.huawei.hms.maps.model.CircleOptions
import com.huawei.hms.maps.model.GroundOverlay
import com.huawei.hms.maps.model.GroundOverlayOptions
import com.huawei.hms.maps.model.IndoorBuilding
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.Marker
import com.huawei.hms.maps.model.MarkerOptions
import com.huawei.hms.maps.model.PointOfInterest
import com.huawei.hms.maps.model.Polygon
import com.huawei.hms.maps.model.PolygonOptions
import com.huawei.hms.maps.model.Polyline
import com.huawei.hms.maps.model.PolylineOptions
import com.huawei.hms.maps.model.TileOverlay
import com.huawei.hms.maps.model.TileOverlayOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

@IntDef(
    HuaweiMap.OnCameraMoveStartedListener.REASON_GESTURE,
    HuaweiMap.OnCameraMoveStartedListener.REASON_API_ANIMATION,
    HuaweiMap.OnCameraMoveStartedListener.REASON_DEVELOPER_ANIMATION
)
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.PROPERTY)
public annotation class MoveStartedReason

public sealed class CameraEvent
public object CameraIdleEvent : CameraEvent()
public object CameraMoveCanceledEvent : CameraEvent()
public object CameraMoveEvent : CameraEvent()
public data class CameraMoveStartedEvent(@MoveStartedReason val reason: Int) : CameraEvent()

/**
 * Change event when a marker is dragged. See [HuaweiMap.setOnMarkerDragListener]
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
 * Change event when the indoor state changes. See [HuaweiMap.IndoorViewListener]
 */
public sealed class IndoorChangeEvent

/**
 * Change event when an indoor building is focused.
 * See [HuaweiMap.IndoorViewListener.onIndoorLeave]
 */
public object IndoorBuildingFocusedEvent : IndoorChangeEvent()

/**
 * Change event when an indoor level is activated.
 * See [HuaweiMap.IndoorViewListener.onIndoorLeave]
 */
public data class IndoorLevelActivatedEvent(val building: IndoorBuilding) : IndoorChangeEvent()

/**
 * Returns a [Flow] of [CameraEvent] items so that camera movements can be observed. Using this to
 * observe camera events will set listeners and thus override existing listeners to
 * [HuaweiMap.setOnCameraIdleListener], [HuaweiMap.setOnCameraMoveCanceledListener],
 * [HuaweiMap.setOnCameraMoveListener] and [HuaweiMap.setOnCameraMoveStartedListener].
 */
@Deprecated(
    message = "Use cameraIdleEvents(), cameraMoveCanceledEvents(), cameraMoveEvents() or cameraMoveStartedEvents",
)
public fun HuaweiMap.cameraEvents(): Flow<CameraEvent> =
    callbackFlow {
        setOnCameraIdleListener {
            trySend(CameraIdleEvent)
        }
        setOnCameraMoveCanceledListener {
            trySend(CameraMoveCanceledEvent)
        }
        setOnCameraMoveListener {
            trySend(CameraMoveEvent)
        }
        setOnCameraMoveStartedListener {
            trySend(CameraMoveStartedEvent(it))
        }
        awaitClose {
            setOnCameraIdleListener(null)
            setOnCameraMoveCanceledListener(null)
            setOnCameraMoveListener(null)
            setOnCameraMoveStartedListener(null)
        }
    }

/**
 * A suspending function that awaits the completion of the [cameraUpdate] animation.
 *
 * @param cameraUpdate the [CameraUpdate] to apply on the map
 * @param durationMs the duration in milliseconds of the animation. Defaults to 3 seconds.
 */
public suspend inline fun HuaweiMap.awaitAnimateCamera(
    cameraUpdate: CameraUpdate,
    durationMs: Int = 3000
): Unit =
    suspendCancellableCoroutine { continuation ->
        animateCamera(cameraUpdate, durationMs, object : HuaweiMap.CancelableCallback {
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
 * [HuaweiMap.setOnMapLoadedCallback].
 */
public suspend inline fun HuaweiMap.awaitMapLoad(): Unit =
    suspendCoroutine { continuation ->
        setOnMapLoadedCallback {
            continuation.resume(Unit)
        }
    }

/**
 * Returns a flow that emits when the camera is idle. Using this to observe camera idle events will
 * override an existing listener (if any) to [HuaweiMap.setOnCameraChangeListener].
 */
public fun HuaweiMap.cameraIdleEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraIdleListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraIdleListener(null)
        }
    }

/**
 * Returns a flow that emits when a camera move is canceled. Using this to observe camera move
 * cancel events will override an existing listener (if any) to
 * [HuaweiMap.setOnCameraMoveCanceledListener].
 */
public fun HuaweiMap.cameraMoveCanceledEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraMoveCanceledListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraMoveCanceledListener(null)
        }
    }

/**
 * Returns a flow that emits when the camera moves. Using this to observe camera move events will
 * override an existing listener (if any) to [HuaweiMap.setOnCameraMoveListener].
 */
public fun HuaweiMap.cameraMoveEvents(): Flow<Unit> =
    callbackFlow {
        setOnCameraMoveListener {
            trySend(Unit)
        }
        awaitClose {
            setOnCameraMoveListener(null)
        }
    }

/**
 * A suspending function that returns a bitmap snapshot of the current view of the map. Uses
 * [HuaweiMap.snapshot].
 *
 * @param bitmap an optional preallocated bitmap
 * @return the snapshot
 */
public suspend inline fun HuaweiMap.awaitSnapshot(bitmap: Bitmap? = null): Bitmap? =
    suspendCoroutine { continuation ->
        snapshot({ continuation.resume(it) }, bitmap)
    }

/**
 * Returns a flow that emits when a camera move started. Using this to observe camera move start
 * events will override an existing listener (if any) to [HuaweiMap.setOnCameraMoveStartedListener].
 */
public fun HuaweiMap.cameraMoveStartedEvents(): Flow<Int> =
    callbackFlow {
        setOnCameraMoveStartedListener {
            trySend(it)
        }
        awaitClose {
            setOnCameraMoveStartedListener(null)
        }
    }

/**
 * Returns a flow that emits when a circle is clicked. Using this to observe circle clicks events
 * will override an existing listener (if any) to [HuaweiMap.setOnCircleClickListener].
 */
public fun HuaweiMap.circleClickEvents(): Flow<Circle> =
    callbackFlow {
        setOnCircleClickListener {
            trySend(it)
        }
        awaitClose {
            setOnCircleClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a ground overlay is clicked. Using this to observe ground overlay
 * clicks events will override an existing listener (if any) to
 * [HuaweiMap.setOnGroundOverlayClickListener].
 */
public fun HuaweiMap.groundOverlayClicks(): Flow<GroundOverlay> =
    callbackFlow {
        setOnGroundOverlayClickListener {
            trySend(it)
        }
        awaitClose {
            setOnGroundOverlayClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the indoor state changes. Using this to observe indoor state
 * change events will override an existing listener (if any) to
 * [HuaweiMap.setIndoorViewListener]
 */
public fun HuaweiMap.indoorStateChangeEvents(): Flow<IndoorChangeEvent> =
    callbackFlow {
        setIndoorViewListener(object : HuaweiMap.IndoorViewListener {
            override fun onIndoorLeave() {
                trySend(IndoorBuildingFocusedEvent)
            }

            override fun onIndoorFocus(indoorBuilding: IndoorBuilding) {
                trySend(IndoorLevelActivatedEvent(building = indoorBuilding))
            }
        })
        awaitClose {
            setIndoorViewListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is clicked. Using this to observe
 * info window clicks will override an existing listener (if any) to
 * [HuaweiMap.setOnInfoWindowClickListener]
 */
public fun HuaweiMap.infoWindowClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowClickListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is closed. Using this to observe info
 * window closes will override an existing listener (if any) to
 * [HuaweiMap.setOnInfoWindowCloseListener]
 */
public fun HuaweiMap.infoWindowCloseEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowCloseListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowCloseListener(null)
        }
    }

/**
 * Returns a flow that emits when a marker's info window is long pressed. Using this to observe info
 * window long presses will override an existing listener (if any) to
 * [HuaweiMap.setOnInfoWindowLongClickListener]
 */
public fun HuaweiMap.infoWindowLongClickEvents(): Flow<Marker> =
    callbackFlow {
        setOnInfoWindowLongClickListener {
            trySend(it)
        }
        awaitClose {
            setOnInfoWindowLongClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the map is clicked. Using this to observe map click events will
 * override an existing listener (if any) to [HuaweiMap.setOnMapClickListener]
 */
public fun HuaweiMap.mapClickEvents(): Flow<LatLng> =
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
 * will override an existing listener (if any) to [HuaweiMap.setOnMapLongClickListener]
 */
public fun HuaweiMap.mapLongClickEvents(): Flow<LatLng> =
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
 * events will override an existing listener (if any) to [HuaweiMap.setOnMarkerClickListener]
 */
public fun HuaweiMap.markerClickEvents(): Flow<Marker> =
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
 * will override existing listeners (if any) to [HuaweiMap.setOnMarkerDragListener]
 */
public fun HuaweiMap.markerDragEvents(): Flow<OnMarkerDragEvent> =
    callbackFlow {
        setOnMarkerDragListener(object : HuaweiMap.OnMarkerDragListener {
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
 * Returns a flow that emits when the my location button is clicked. Using this to observe my
 * location button click events will override an existing listener (if any) to
 * [HuaweiMap.setOnMyLocationButtonClickListener]
 */
public fun HuaweiMap.myLocationButtonClickEvents(): Flow<Unit> =
    callbackFlow {
        setOnMyLocationButtonClickListener {
            trySend(Unit).isSuccess
        }
        awaitClose {
            setOnMyLocationButtonClickListener(null)
        }
    }

/**
 * Returns a flow that emits when the my location blue dot is clicked. Using this to observe my
 * location blue dot click events will override an existing listener (if any) to
 * [HuaweiMap.setOnMyLocationClickListener]
 */
public fun HuaweiMap.myLocationChangeEvents(): Flow<Location> =
    callbackFlow {
        setOnMyLocationClickListener {
            trySend(it)
        }
        awaitClose {
            setOnMyLocationClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a PointOfInterest is clicked. Using this to observe
 * PointOfInterest click events will override an existing listener (if any) to
 * [HuaweiMap.setOnPoiClickListener]
 */
public fun HuaweiMap.poiClickEvents(): Flow<PointOfInterest> =
    callbackFlow {
        setOnPoiClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPoiClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a Polygon is clicked. Using this to observe Polygon click events
 * will override an existing listener (if any) to [HuaweiMap.setOnPolygonClickListener]
 */
public fun HuaweiMap.polygonClickEvents(): Flow<Polygon> =
    callbackFlow {
        setOnPolygonClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPolygonClickListener(null)
        }
    }

/**
 * Returns a flow that emits when a Polyline is clicked. Using this to observe Polyline click events
 * will override an existing listener (if any) to [HuaweiMap.setOnPolylineClickListener]
 */
public fun HuaweiMap.polylineClickEvents(): Flow<Polyline> =
    callbackFlow {
        setOnPolylineClickListener {
            trySend(it)
        }
        awaitClose {
            setOnPolylineClickListener(null)
        }
    }

/**
 * Builds a new [HuaweiMapOptions] using the provided [optionsActions].
 *
 * @return the constructed [HuaweiMapOptions]
 */
public inline fun buildHuaweiMapOptions(optionsActions: HuaweiMapOptions.() -> Unit): HuaweiMapOptions =
    HuaweiMapOptions().apply(
        optionsActions
    )

/**
 * Adds a [Circle] to this [HuaweiMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Circle]
 */
public inline fun HuaweiMap.addCircle(optionsActions: CircleOptions.() -> Unit): Circle =
    this.addCircle(
        circleOptions(optionsActions)
    )

/**
 * Adds a [GroundOverlay] to this [HuaweiMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [GroundOverlay]
 */
public inline fun HuaweiMap.addGroundOverlay(optionsActions: GroundOverlayOptions.() -> Unit): GroundOverlay? =
    this.addGroundOverlay(
        groundOverlayOptions(optionsActions)
    )

/**
 * Adds a [Marker] to this [HuaweiMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Marker]
 */
public inline fun HuaweiMap.addMarker(optionsActions: MarkerOptions.() -> Unit): Marker =
    this.addMarker(
        markerOptions(optionsActions)
    )

/**
 * Adds a [Polygon] to this [HuaweiMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polygon]
 */
public inline fun HuaweiMap.addPolygon(optionsActions: PolygonOptions.() -> Unit): Polygon =
    this.addPolygon(
        polygonOptions(optionsActions)
    )

/**
 * Adds a [Polyline] to this [HuaweiMap] using the function literal with receiver [optionsActions].
 *
 * @return the added [Polyline]
 */
public inline fun HuaweiMap.addPolyline(optionsActions: PolylineOptions.() -> Unit): Polyline =
    this.addPolyline(
        polylineOptions(optionsActions)
    )

/**
 * Adds a [TileOverlay] to this [HuaweiMap] using the function literal with receiver
 * [optionsActions].
 *
 * @return the added [TileOverlay]
 */
public inline fun HuaweiMap.addTileOverlay(optionsActions: TileOverlayOptions.() -> Unit): TileOverlay? =
    this.addTileOverlay(
        tileOverlayOptions(optionsActions)
    )
