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
package com.github.speak2me.compose.map.baidu.ktx

import com.baidu.mapapi.model.LatLng
import org.junit.Assert.*
import org.junit.Test

internal class LatLngTest {
    private val earthRadius = 6371009.0

    @Test
    fun `test that latLng can be destructured`() {
        val latLng = LatLng(2.0, 3.0)
        val (lat, lng) = latLng
        assertEquals(2.0, lat, 1e-6)
        assertEquals(3.0, lng, 1e-6)
    }

    @Test
    fun `compute spherical distance`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        assertEquals(Math.PI * earthRadius, up.sphericalDistance(down), 1e-6)
    }

    @Test
    fun `validate spherical polygon area`() {
        val up = LatLng(90.0, 0.0)
        val down = LatLng(-90.0, 0.0)
        val right = LatLng(0.0, 90.0)
        val polygon = listOf(up, down, right, up)
        assertEquals(1.2751647824926386E14, polygon.sphericalPolygonArea(), 1e-6)
    }

}
