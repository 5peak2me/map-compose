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
package com.github.speak2me.compose.map.baidu.ktx.model

import com.baidu.mapapi.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PolylineOptionsTest {
    private val tracks = listOf(LatLng(1.0, 2.0), LatLng(2.0, 3.0))
    @Test
    fun testBuilder() {
        val polylineOptions = polylineOptions {
            points(tracks)
            color(0)
            isGeodesic(true)
            width(1)
        }
        assertEquals(tracks, polylineOptions.points)
        assertEquals(0, polylineOptions.color)
//        assertTrue(polylineOptions.isGeodesic)
        assertEquals(1, polylineOptions.width)
    }
}
