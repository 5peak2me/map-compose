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

import android.view.View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.github.speak2me.compose.map.tencent.ktx.model.toComposeCameraPosition
import com.tencent.tencentmap.mapsdk.maps.TencentMap
import com.tencent.tencentmap.mapsdk.maps.model.CameraPosition
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationConfig

internal class MapPropertiesNode(
    val map: TencentMap,
    cameraPositionState: CameraPositionState,
    contentDescription: String?,
    var density: Density,
    var layoutDirection: LayoutDirection,
    contentPadding: PaddingValues
) : MapNode {

    init {
        applyContentPadding(map, contentPadding)
        // set camera position after padding for correct centering
        cameraPositionState.setMap(map)
//        if (contentDescription != null) {
//            map.setContentDescription(contentDescription)
//        }
    }

    var contentDescription = contentDescription
        set(value) {
            field = value
//            map.setContentDescription(contentDescription)
        }

    var cameraPositionState = cameraPositionState
        set(value) {
            if (value == field) return
            field.setMap(null)
            field = value
            value.setMap(map)
        }

    override fun onAttached() {
//        map.setOnCameraIdleListener {
//            cameraPositionState.isMoving = false
//            // setOnCameraMoveListener is only invoked when the camera position
//            // is changed via .animate(). To handle updating state when .move()
//            // is used, it's necessary to set the camera's position here as well
//            cameraPositionState.rawPosition = map.cameraPosition
//        }
//        map.setOnCameraMoveCanceledListener {
//            cameraPositionState.isMoving = false
//        }
//        map.setOnCameraMoveStartedListener {
//            cameraPositionState.cameraMoveStartedReason = CameraMoveStartedReason.fromInt(it)
//            cameraPositionState.isMoving = true
//        }
//        map.setOnCameraMoveListener {
//            cameraPositionState.rawPosition = map.cameraPosition
//        }
        map.setOnCameraChangeListener(object : TencentMap.OnCameraChangeListener {
            override fun onCameraChange(position: CameraPosition) {
                cameraPositionState.cameraMoveStartedReason = CameraMoveStartedReason.fromInt(position.triggers.firstOrNull { position.changedReason == it.name }?.ordinal ?: 0)
                cameraPositionState.isMoving = true
                cameraPositionState.rawPosition = position.toComposeCameraPosition()
            }

            override fun onCameraChangeFinished(position: CameraPosition) {
                cameraPositionState.isMoving = false
                cameraPositionState.rawPosition = position.toComposeCameraPosition()
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
@Suppress("NOTHING_TO_INLINE", "DEPRECATION")
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
            applyContentPadding(map, it)
        }

        set(locationSource) { map.setLocationSource(it) }
        set(mapProperties.isBuildingEnabled) { map.showBuilding(it) }
        set(mapProperties.isIndoorEnabled) { map.setIndoorEnabled(it) }
        set(mapProperties.isMyLocationEnabled) {
            map.myLocationConfig = MyLocationConfig.newBuilder(map.myLocationConfig)
                .setMyLocationEnabled(it)
                .build()
        }
        set(mapProperties.isTrafficEnabled) { map.isTrafficEnabled = it }
//        set(mapProperties.latLngBoundsForCameraTarget) { map.setLatLngBoundsForCameraTarget(it) }
        set(mapProperties.mapStyleOptions) { it?.let(map::setMapStyle) }
        set(mapProperties.mapType) { map.mapType = it.value }
        set(mapProperties.maxZoomPreference) { map.setMaxZoomLevel(it.toInt()) }
        set(mapProperties.minZoomPreference) {  map.setMinZoomLevel(it.toInt()) }
        set(mapColorScheme) {
            if (it != null) {
                map.mapType = it
            }
        }

        set(mapUiSettings.compassEnabled) { map.uiSettings.isCompassEnabled = it }
        set(mapUiSettings.indoorLevelPickerEnabled) { map.uiSettings.isIndoorLevelPickerEnabled = it }
//        set(mapUiSettings.mapToolbarEnabled) { map.uiSettings.isMapToolbarEnabled = it }
        set(mapUiSettings.myLocationButtonEnabled) { map.uiSettings.isMyLocationButtonEnabled = it }
        set(mapUiSettings.rotationGesturesEnabled) { map.uiSettings.isRotateGesturesEnabled = it }
        set(mapUiSettings.scaleControlsEnabled) {
            map.uiSettings.isScaleViewEnabled = it
            map.uiSettings.setScaleViewFadeEnable(it)
        }
        set(mapUiSettings.scrollGesturesEnabled) { map.uiSettings.isScrollGesturesEnabled = it }
//        set(mapUiSettings.scrollGesturesEnabledDuringRotateOrZoom) { map.uiSettings.isScrollGesturesEnabledDuringRotateOrZoom = it }
        set(mapUiSettings.tiltGesturesEnabled) { map.uiSettings.isTiltGesturesEnabled = it }
        set(mapUiSettings.zoomControlsEnabled) { map.uiSettings.isZoomControlsEnabled = it }
        set(mapUiSettings.zoomGesturesEnabled) { map.uiSettings.isZoomGesturesEnabled = it }

        update(cameraPositionState) { this.cameraPositionState = it }
    }
}

private fun MapPropertiesNode.applyContentPadding(map: TencentMap, contentPadding: PaddingValues) {
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
