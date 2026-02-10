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

import androidx.compose.runtime.Stable
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.CustomMapStyleOptions as MapStyleOptions
import java.util.Objects

/**
 * Equivalent to [MapProperties] with default values.
 */
public val DefaultMapProperties: MapProperties = MapProperties()

/**
 * Data class for properties that can be modified on the map.
 *
 * Note: This is intentionally a class and not a data class for binary
 * compatibility on future changes.
 * See: https://jakewharton.com/public-api-challenges-in-kotlin/
 */
@Stable
public class MapProperties(
    public val isMapTextEnabled: Boolean = true,
    public val isBuildingEnabled: Boolean = false,
    public val isIndoorEnabled: Boolean = false,
    public val isMyLocationEnabled: Boolean = false,
    public val isTrafficEnabled: Boolean = false,
    public val latLngBoundsForCameraTarget: LatLngBounds? = null,
    public val mapStyleOptions: MapStyleOptions? = null,
    public val mapType: MapType = MapType.NORMAL,
    public val maxZoomPreference: Float = 20.0f,
    public val minZoomPreference: Float = 3.0f,
) {
    override fun toString(): String = "MapProperties(" +
        "isMapTextEnabled=$isMapTextEnabled, " +
        "isBuildingEnabled=$isBuildingEnabled, isIndoorEnabled=$isIndoorEnabled, " +
        "isMyLocationEnabled=$isMyLocationEnabled, isTrafficEnabled=$isTrafficEnabled, " +
        "latLngBoundsForCameraTarget=$latLngBoundsForCameraTarget, mapStyleOptions=$mapStyleOptions, " +
        "mapType=$mapType, maxZoomPreference=$maxZoomPreference, " +
        "minZoomPreference=$minZoomPreference)"

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        isMapTextEnabled == other.isMapTextEnabled &&
        isBuildingEnabled == other.isBuildingEnabled &&
        isIndoorEnabled == other.isIndoorEnabled &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        latLngBoundsForCameraTarget == other.latLngBoundsForCameraTarget &&
        mapStyleOptions == other.mapStyleOptions &&
        mapType == other.mapType &&
        maxZoomPreference == other.maxZoomPreference &&
        minZoomPreference == other.minZoomPreference

    override fun hashCode(): Int = Objects.hash(
        isMapTextEnabled,
        isBuildingEnabled,
        isIndoorEnabled,
        isMyLocationEnabled,
        isTrafficEnabled,
        latLngBoundsForCameraTarget,
        mapStyleOptions,
        mapType,
        maxZoomPreference,
        minZoomPreference
    )

    public fun copy(
        isMapTextEnabled: Boolean = this.isMapTextEnabled,
        isBuildingEnabled: Boolean = this.isBuildingEnabled,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        latLngBoundsForCameraTarget: LatLngBounds? = this.latLngBoundsForCameraTarget,
        mapStyleOptions: MapStyleOptions? = this.mapStyleOptions,
        mapType: MapType = this.mapType,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
    ): MapProperties = MapProperties(
        isMapTextEnabled = isMapTextEnabled,
        isBuildingEnabled = isBuildingEnabled,
        isIndoorEnabled = isIndoorEnabled,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        latLngBoundsForCameraTarget = latLngBoundsForCameraTarget,
        mapStyleOptions = mapStyleOptions,
        mapType = mapType,
        maxZoomPreference = maxZoomPreference,
        minZoomPreference = minZoomPreference,
    )
}
