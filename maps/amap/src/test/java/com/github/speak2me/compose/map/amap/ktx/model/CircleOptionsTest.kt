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
package com.github.speak2me.compose.map.amap.ktx.model

import com.amap.api.maps.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class CircleOptionsTest {

    @Test
    fun testBuilder() {
        val circleOptions = circleOptions {
            center(LatLng(0.0, 0.0))
            fillColor(0)
            radius(1.23)
            strokeColor(1)
            strokeWidth(2f)
            visible(true)
            zIndex(1f)
        }
        assertEquals(LatLng(0.0, 0.0), circleOptions.center)
        assertEquals(0, circleOptions.fillColor)
        assertEquals(1.23, circleOptions.radius, 1e-6)
        assertEquals(1, circleOptions.strokeColor)
        assertEquals(2f, circleOptions.strokeWidth)
        assertTrue(circleOptions.isVisible)
        assertEquals(1f, circleOptions.zIndex, 1e-6f)
    }
}
