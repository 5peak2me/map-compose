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
package com.github.speak2me.compose.map.huawei

import android.location.Location
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.NonRestartableComposable
import androidx.compose.runtime.currentComposer
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.huawei.hms.maps.HuaweiMap
import com.huawei.hms.maps.HuaweiMap.*
import com.huawei.hms.maps.model.IndoorBuilding
import com.huawei.hms.maps.model.LatLng
import com.huawei.hms.maps.model.PointOfInterest

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
 * @param L HuaweiMap click listener type, e.g. [OnMapClickListener]
 */
internal class MapClickListenerNode<L : Any>(
    private val map: HuaweiMap,
    private val setter: HuaweiMap.(L?) -> Unit,
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
                HuaweiMap::setIndoorViewListener,
                object : IndoorViewListener {
                    override fun onIndoorLeave() =
                        callback().onIndoorBuildingFocused()

                    override fun onIndoorFocus(building: IndoorBuilding)  =
                        callback().onIndoorLevelActivated(building)
                }
            )
        }

        ::onMapClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnMapClickListener,
                OnMapClickListener { callback()?.invoke(it) }
            )
        }

        ::onMapLongClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnMapLongClickListener,
                OnMapLongClickListener { callback()?.invoke(it) }
            )
        }

        ::onMapLoaded.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnMapLoadedCallback,
                OnMapLoadedCallback { callback()?.invoke() }
            )
        }

        ::onMyLocationButtonClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnMyLocationButtonClickListener,
                OnMyLocationButtonClickListener { callback()?.invoke() ?: false }
            )
        }

        ::onMyLocationClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnMyLocationClickListener,
                OnMyLocationClickListener { callback()?.invoke(it) }
            )
        }

        ::onPOIClick.let { callback ->
            MapClickListenerComposeNode(
                callback,
                HuaweiMap::setOnPoiClickListener,
                OnPoiClickListener { callback()?.invoke(it) }
            )
        }
    }
}

/**
 * Encapsulates the ComposeNode factory lambda as a recomposition optimization.
 *
 * @param L HuaweiMap click listener type, e.g. [OnMapClickListener]
 * @param callback a property reference to the callback lambda, i.e.
 * invoking it returns the callback lambda
 * @param setter a reference to a HuaweiMap setter method, e.g. `setOnMapClickListener()`
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
    setter: HuaweiMap.(L?) -> Unit,
    listener: L
) {
    val mapApplier = currentComposer.applier as MapApplier

    MapClickListenerComposeNode(callback) { MapClickListenerNode(mapApplier.map, setter, listener) }
}

@Composable
@HuaweiMapComposable
private fun MapClickListenerComposeNode(
    callback: () -> Any?,
    factory: () -> MapClickListenerNode<*>
) {
    // Setting a HuaweiMap listener may have side effects, so we unset it as needed.
    // However, the listener is reset only when the corresponding callback lambda
    // toggles between null and non-null. This is to avoid potential performance problems
    // when callbacks recompose rapidly; setting HuaweiMap listeners could potentially be
    // expensive due to synchronization, etc. HuaweiMap listeners are not designed with a
    // use case of rapid recomposition in mind.
    if (callback() != null) ComposeNode<MapClickListenerNode<*>, MapApplier>(factory) {}
}
