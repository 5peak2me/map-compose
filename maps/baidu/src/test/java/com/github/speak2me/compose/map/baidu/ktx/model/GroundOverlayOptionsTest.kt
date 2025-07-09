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

import com.baidu.mapapi.map.BitmapDescriptor
import com.nhaarman.mockitokotlin2.mock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

internal class GroundOverlayOptionsTest {

    @Test
    fun testBuilder() {
        val descriptor: BitmapDescriptor = mock()
        val groundOverlayOptions = groundOverlayOptions {
            image(descriptor)
//            bearing(1f)
            transparency(0.5f)
            visible(true)
        }
        assertEquals(descriptor, groundOverlayOptions.image)
//        assertEquals(1f, groundOverlayOptions.bearing, 1e-6f)
        assertEquals(0.5f, groundOverlayOptions.transparency, 1e-6f)
        assertTrue(groundOverlayOptions.isVisible)
    }
}
