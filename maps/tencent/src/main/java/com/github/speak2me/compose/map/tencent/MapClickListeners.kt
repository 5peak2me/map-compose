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
package com.github.speak2me.compose.map.tencent

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.speak2me.compose.map.tencent.ktx.toLocation
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.TencentMap.*
import com.tencent.tencentmap.mapsdk.maps.model.IndoorBuilding
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MapPoi as PointOfInterest

/**
 * Default implementation of [IndoorStateChangeListener] with no-op
 * implementations.
 */
public object DefaultIndoorStateChangeListener : IndoorStateChangeListener

/**
 * Interface definition for building indoor level state changes.
 */
public interface IndoorStateChangeListener {
    /**
     * Callback invoked when an indoor building comes to focus.
     */
    public fun onIndoorBuildingFocused() {}

    /**
     * Callback invoked when a level for a building is activated.
     * @param building the activated building
     */
    public fun onIndoorLevelActivated(building: IndoorBuilding) {}
}

/**
 * Holder class for top-level click listeners.
 */
internal class MapClickListeners {
    var indoorStateChangeListener: IndoorStateChangeListener by mutableStateOf(DefaultIndoorStateChangeListener)
    var onMapClick: ((LatLng) -> Unit)? by mutableStateOf(null)
    var onMapLongClick: ((LatLng) -> Unit)? by mutableStateOf(null)
    var onMapLoaded: (() -> Unit)? by mutableStateOf(null)
    var onMyLocationButtonClick: (() -> Boolean)? by mutableStateOf(null)
    var onMyLocationClick: ((Location) -> Unit)? by mutableStateOf(null)
    var onPOIClick: ((PointOfInterest) -> Unit)? by mutableStateOf(null)
}

/**
87777777777777777777771` * @param L TencentMap click listener type, e.g. [OnMapClickListener]
 */
internal class MapClickListenerNode<L : Any>(
    private val map: TencentMap,
    private val setter: TencentMap.(L?) -> Unit,
    private val listener: L
) : MapNode {
    override fun onAttached() = setListener(listener)
    override fun onRemoved() = setListener(null)
    override fun onCleared() = setListener(null)

    private fun setListener(listenerOrNull: L?) = map.setter(listenerOrNull)
}

@Composable
internal fun MapClickListenerUpdater() {
    // The mapClickListeners container object is not allowed to ever change
    val mapClickListeners = (currentComposer.applier as MapApplier).mapClickListeners

    with(mapClickListeners) {
        ::indoorStateChangeListener.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::setOnIndoorStateChangeListener,
                object : OnIndoorStateChangeListener {
                    override fun onIndoorBuildingFocused(): Boolean {
                        callback().onIndoorBuildingFocused()
                        return true
                    }

                    override fun onIndoorLevelActivated(building: IndoorBuilding): Boolean {
                        callback().onIndoorLevelActivated(building)
                        return true
                    }

                    override fun onIndoorBuildingDeactivated(): Boolean = true
                }
            )
        }

        ::onMapClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::setOnMapClickListener,
                OnMapClickListener { callback()?.invoke(it) }
            )
        }

        ::onMapLongClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::setOnMapLongClickListener,
                OnMapLongClickListener { callback()?.invoke(it) }
            )
        }

        ::onMapLoaded.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::addOnMapLoadedCallback,
                OnMapLoadedCallback { callback()?.invoke() }
            )
        }

//        ::onMyLocationButtonClick.let { callback ->
//            MapClickListenerComposeNode(
//                callback,
//                TencentMap::setOnMyLocationButtonClickListener,
//                OnMyLocationButtonClickListener { callback()?.invoke() ?: false }
//            )
//        }

        ::onMyLocationClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::setMyLocationClickListener,
                OnMyLocationClickListener { callback()?.invoke(it.toLocation())
                    true
                }
            )
        }

        ::onPOIClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                TencentMap::setOnMapPoiClickListener,
                OnMapPoiClickListener { callback()?.invoke(it) }
            )
        }
    }
}

/**
 * Encapsulates the ComposeNode factory lambda as a recomposition optimization.
 *
 * @param L TencentMap click listener type, e.g. [OnMapClickListener]
 * @param callback a property reference to the callback lambda, i.e.
 * invoking it returns the callback lambda
 * @param setter a reference to a TencentMap setter method, e.g. `setOnMapClickListener()`
 * @param listener must include a call to `callback()` inside the listener
 * to use the most up-to-date recomposed version of the callback lambda;
 * However, the resulting callback reference might actually be null due to races;
 * the caller must guard against this case.
 *
 */
@Composable
@NonRestartableComposable
private fun <L : Any> MapClickListenerComposeNode(
    callback: () -> Any?,
    setter: TencentMap.(L?) -> Unit,
    listener: L
) {
    val mapApplier = currentComposer.applier as MapApplier

    MapClickListenerComposeNode(callback) { MapClickListenerNode(mapApplier.map, setter, listener) }
}

@Composable
@TencentMapComposable
private fun MapClickListenerComposeNode(
    callback: () -> Any?,
    factory: () -> MapClickListenerNode<*>
) {
    // Setting a TencentMap listener may have side effects, so we unset it as needed.
    // However, the listener is reset only when the corresponding callback lambda
    // toggles between null and non-null. This is to avoid potential performance problems
    // when callbacks recompose rapidly; setting TencentMap listeners could potentially be
    // expensive due to synchronization, etc. TencentMap listeners are not designed with a
    // use case of rapid recomposition in mind.
    if (callback() != null) ComposeNode<MapClickListenerNode<*>, MapApplier>(factory) {}
}
