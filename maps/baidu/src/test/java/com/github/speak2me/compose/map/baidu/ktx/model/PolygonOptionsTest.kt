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

import android.graphics.Color
import com.baidu.mapapi.map.Stroke
import com.baidu.mapapi.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Test

internal class PolygonOptionsTest {
    private val tracks = listOf(LatLng(1.0, 2.0), LatLng(2.0, 3.0), LatLng(3.0, 4.0))
    @Test
    fun testBuilder() {
        val polygonOptions =
            polygonOptions {
                stroke(Stroke(1, Color.BLACK))
                points(tracks)
            }
        assertEquals(1, polygonOptions.stroke.strokeWidth)
        assertEquals(Color.BLACK, polygonOptions.stroke.color)
        assertEquals(tracks, polygonOptions.points)
    }
}
