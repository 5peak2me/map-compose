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
package com.github.speak2me.compose.map.amap

import android.annotation.SuppressLint
import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.amap.api.maps.AMap
//import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView as MapView
import com.amap.api.maps.model.CameraPosition

internal class MapPropertiesNode(
    val map: AMap,
    val mapView: MapView,
    cameraPositionState: CameraPositionState,
    contentDescription: String?,
    var density: Density,
    var layoutDirection: LayoutDirection,
    contentPadding: PaddingValues
) : MapNode {

    init {
        map.accelerateNetworkInChinese(true)
        applyContentPadding(mapView, contentPadding)
        // set camera position after padding for correct centering
        cameraPositionState.setMap(map)
        if (contentDescription != null) {
            mapView.setContentDescription(contentDescription)
        }
    }

    var contentDescription = contentDescription
        set(value) {
            field = value
            mapView.setContentDescription(contentDescription)
        }

    var cameraPositionState = cameraPositionState
        set(value) {
            if (value == field) return
            field.setMap(null)
            field = value
            value.setMap(map)
        }

    override fun onAttached() {
        map.setOnCameraChangeListener(object : AMap.OnCameraChangeListener {
            override fun onCameraChangeFinish(position: CameraPosition) {
                cameraPositionState.isMoving = false
                cameraPositionState.rawPosition = position
            }

            override fun onCameraChange(position: CameraPosition) {
                cameraPositionState.isMoving = true
                cameraPositionState.rawPosition = position
            }
        })
    }

    override fun onRemoved() {
        cameraPositionState.setMap(null)
    }

    override fun onCleared() {
        cameraPositionState.setMap(null)
    }
}

/**
 * Default map content padding does not pad.
 */
public val DefaultMapContentPadding: PaddingValues = PaddingValues()

/**
 * Used to keep the primary map properties up to date. This should never leave the map composition.
 */
@SuppressLint("MissingPermission")
@Suppress("NOTHING_TO_INLINE")
@Composable
internal inline fun MapUpdater(mapUpdaterState: MapUpdaterState) = with(mapUpdaterState) {
    val map = (currentComposer.applier as MapApplier).map
    val mapView = (currentComposer.applier as MapApplier).mapView
    if (mergeDescendants) {
        mapView.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
    }
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    ComposeNode<MapPropertiesNode, MapApplier>(
        factory = {
            MapPropertiesNode(
                map = map,
                mapView = mapView,
                contentDescription = contentDescription,
                cameraPositionState = cameraPositionState,
                density = density,
                layoutDirection = layoutDirection,
                contentPadding = contentPadding
            )
        }
    ) {
        // The node holds density and layoutDirection so that the updater blocks can be
        // non-capturing, allowing the compiler to turn them into singletons
        update(density) { this.density = it }
        update(layoutDirection) { this.layoutDirection = it }
        update(contentDescription) { this.contentDescription = it }
        update(contentPadding) {
            applyContentPadding(mapView, it)
        }

        set(locationSource) { map.setLocationSource(it) }
        set(mapProperties.isMapTextEnabled) { map.showMapText(it) }
        set(mapProperties.isBuildingEnabled) { map.showBuildings(it) }
        set(mapProperties.isIndoorEnabled) { map.showIndoorMap(it) }
        update(mapProperties.isMyLocationEnabled) { map.isMyLocationEnabled = it } // needed to update
        set(mapProperties.isTrafficEnabled) { map.isTrafficEnabled = it }
        set(mapProperties.latLngBoundsForCameraTarget) { map.setMapStatusLimits(it) }
        set(mapProperties.mapStyleOptions) { map.setCustomMapStyle(it) }
        set(mapProperties.mapType) { map.mapType = it.value }
        set(mapProperties.maxZoomPreference) { map.maxZoomLevel = it }
        set(mapProperties.minZoomPreference) { map.minZoomLevel = it }
        set(mapColorScheme) {
            if (it != null) {
                map.mapType = it
            }
        }

        set(mapUiSettings.compassEnabled) { map.uiSettings.isCompassEnabled = it }
        set(mapUiSettings.indoorLevelPickerEnabled) { map.uiSettings.isIndoorSwitchEnabled = it }
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        set(mapUiSettings.rotationGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
        set(mapUiSettings.scaleControlsEnabled) { map.uiSettings.isScaleControlsEnabled = it }
        set(mapUiSettings.scrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
        set(mapUiSettings.tiltGesturesEnabled) { map.uiSettings.isTiltGesturesEnabled = it }
        set(mapUiSettings.zoomControlsEnabled) { map.uiSettings.isZoomControlsEnabled = it }
        set(mapUiSettings.zoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }

        update(cameraPositionState) { this.cameraPositionState = it }
    }
}

private fun MapPropertiesNode.applyContentPadding(map: MapView, contentPadding: PaddingValues) {
    val node = this
    with (this.density) {
        map.setPadding(
            contentPadding.calculateLeftPadding(node.layoutDirection).roundToPx(),
            contentPadding.calculateTopPadding().roundToPx(),
            contentPadding.calculateRightPadding(node.layoutDirection).roundToPx(),
            contentPadding.calculateBottomPadding().roundToPx()
        )
    }
}
