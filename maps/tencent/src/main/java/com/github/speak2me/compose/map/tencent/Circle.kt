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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.github.speak2me.compose.map.tencent.ktx.addCircle
import com.github.speak2me.compose.map.tencent.ktx.model.circleOptions
import com.tencent.tencentmap.mapsdk.maps.model.Circle
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

internal class CircleNode(
    val circle: Circle,
    var onCircleClick: (Circle) -> Unit
) : MapNode {
    override fun onRemoved() {
        circle.remove()
    }
}

/**
 * A composable for a circle on the map.
 *
 * @param center the [LatLng] to use for the center of this circle
 * @param clickable boolean indicating if the circle is clickable or not
 * @param fillColor the fill color of the circle
 * @param radius the radius of the circle in meters.
 * @param strokeColor the stroke color of the circle
 * @param strokePattern a sequence of [Int] to be repeated along the circle's outline (null
 * represents a solid line)
 * @param tag optional tag to be associated with the circle
 * @param strokeWidth the width of the circle's outline in screen pixels
 * @param visible the visibility of the circle
 * @param zIndex the z-index of the circle
 * @param onClick a lambda invoked when the circle is clicked
 */
@Composable
@TencentMapComposable
public fun Circle(
    center: LatLng,
    clickable: Boolean = false,
    fillColor: Color = Color.Black,
    radius: Double = 10.0,
    strokeColor: Color = Color.Black,
    strokePattern: List<Int>? = null,
    strokeWidth: Float = 10f,
    tag: Any? = null,
    visible: Boolean = true,
    zIndex: Float = 0f,
    onClick: (Circle) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as? MapApplier
    ComposeNode<CircleNode, MapApplier>(
        factory = {
            val circle = mapApplier?.map?.addCircle {
                center(center)
                clickable(clickable)
                fillColor(fillColor.toArgb())
                radius(radius)
                strokeColor(strokeColor.toArgb())
                pattern(strokePattern)
                strokeWidth(strokeWidth)
                visible(visible)
                zIndex(zIndex.toInt())
            } ?: error("Error adding circle")
            circle.tag = tag
            CircleNode(circle, onClick)
        },
        update = {
            update(onClick) { this.onCircleClick = it }

            update(center) { this.circle.center = it }
            update(clickable) { this.circle.isClickable = it }
            update(fillColor) { this.circle.fillColor = it.toArgb() }
            update(radius) { this.circle.radius = it }
            update(strokeColor) { this.circle.strokeColor = it.toArgb() }
            update(strokePattern) { this.circle.setOptions(circleOptions { pattern(it) }) }
            update(strokeWidth) { this.circle.strokeWidth = it }
            update(tag) { this.circle.tag = it }
            update(visible) { this.circle.isVisible = it }
            update(zIndex) { this.circle.zIndex = it.toInt()}
        }
    )
}
