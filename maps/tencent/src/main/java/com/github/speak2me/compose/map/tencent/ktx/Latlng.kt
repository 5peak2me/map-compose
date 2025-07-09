/*
 * Copyright © 2020 J!nl!n™ Inc. All rights reserved.
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
@file:Suppress("NOTHING_TO_INLINE")

package com.github.speak2me.compose.map.tencent.ktx

import com.tencent.map.geolocation.TencentLocationUtils
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

public operator fun LatLng.minus(distanceOf: LatLng): Double =
    TencentLocationUtils.distanceBetween(latitude, longitude, distanceOf.latitude, distanceOf.longitude)

/**
 * Returns the [LatLng.latitude] of this [LatLng].
 *
 * e.g.
 * ```
 * val (lat, _) = latLng
 * ```
 */
public inline operator fun LatLng.component1(): Double = this.latitude

/**
 * Returns the [LatLng.longitude] of this [LatLng].
 *
 * e.g.
 * ```
 * val (_, lng) = latLng
 * ```
 */
public inline operator fun LatLng.component2(): Double = this.longitude

/**
 * Computes the spherical distance between this LatLng and [to].
 *
 * @param to the LatLng to compute the distance to
 * @return the distance between this and [to] in meters
 */
public inline fun LatLng.sphericalDistance(to: LatLng): Double =
    TencentLocationUtils.distanceBetween(latitude, longitude, to.latitude, to.longitude)
