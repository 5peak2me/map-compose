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
package com.github.speak2me.compose.map.baidu

import com.baidu.mapapi.model.LatLngBounds
import java.util.Objects
import com.baidu.mapapi.map.MapCustomStyleOptions as MapStyleOptions

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
public class MapProperties(
    public val isBuildingEnabled: Boolean = false,
    public val isIndoorEnabled: Boolean = false,
    public val isMyLocationEnabled: Boolean = false,
    public val isTrafficEnabled: Boolean = false,
    public val latLngBoundsForCameraTarget: LatLngBounds? = null,
    public val mapStyleOptions: MapStyleOptions? = null,
    public val mapType: MapType = MapType.NORMAL,
    public val logoPosition: LogoPosition = LogoPosition.LEFT_BOTTOM,
    public val maxZoomPreference: Float = 22.0f,
    public val minZoomPreference: Float = 4.0f,
) {
    override fun toString(): String = "MapProperties(" +
        "isBuildingEnabled=$isBuildingEnabled, isIndoorEnabled=$isIndoorEnabled, " +
        "isMyLocationEnabled=$isMyLocationEnabled, isTrafficEnabled=$isTrafficEnabled, " +
        "latLngBoundsForCameraTarget=$latLngBoundsForCameraTarget, mapStyleOptions=$mapStyleOptions, " +
        "mapType=$mapType, maxZoomPreference=$maxZoomPreference, " +
        "minZoomPreference=$minZoomPreference)"

    override fun equals(other: Any?): Boolean = other is MapProperties &&
        isBuildingEnabled == other.isBuildingEnabled &&
        isIndoorEnabled == other.isIndoorEnabled &&
        isMyLocationEnabled == other.isMyLocationEnabled &&
        isTrafficEnabled == other.isTrafficEnabled &&
        latLngBoundsForCameraTarget == other.latLngBoundsForCameraTarget &&
        mapStyleOptions == other.mapStyleOptions &&
        mapType == other.mapType &&
        logoPosition == other.logoPosition &&
        maxZoomPreference == other.maxZoomPreference &&
        minZoomPreference == other.minZoomPreference

    override fun hashCode(): Int = Objects.hash(
        isBuildingEnabled,
        isIndoorEnabled,
        isMyLocationEnabled,
        isTrafficEnabled,
        latLngBoundsForCameraTarget,
        mapStyleOptions,
        mapType,
        logoPosition,
        maxZoomPreference,
        minZoomPreference
    )

    public fun copy(
        isBuildingEnabled: Boolean = this.isBuildingEnabled,
        isIndoorEnabled: Boolean = this.isIndoorEnabled,
        isMyLocationEnabled: Boolean = this.isMyLocationEnabled,
        isTrafficEnabled: Boolean = this.isTrafficEnabled,
        latLngBoundsForCameraTarget: LatLngBounds? = this.latLngBoundsForCameraTarget,
        mapStyleOptions: MapStyleOptions? = this.mapStyleOptions,
        mapType: MapType = this.mapType,
        logoPosition: LogoPosition = this.logoPosition,
        maxZoomPreference: Float = this.maxZoomPreference,
        minZoomPreference: Float = this.minZoomPreference,
    ): MapProperties = MapProperties(
        isBuildingEnabled = isBuildingEnabled,
        isIndoorEnabled = isIndoorEnabled,
        isMyLocationEnabled = isMyLocationEnabled,
        isTrafficEnabled = isTrafficEnabled,
        latLngBoundsForCameraTarget = latLngBoundsForCameraTarget,
        mapStyleOptions = mapStyleOptions,
        mapType = mapType,
        logoPosition = logoPosition,
        maxZoomPreference = maxZoomPreference,
        minZoomPreference = minZoomPreference,
    )
}
