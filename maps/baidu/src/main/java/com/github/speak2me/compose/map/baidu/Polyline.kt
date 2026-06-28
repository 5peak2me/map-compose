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

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposeNode
import androidx.compose.runtime.currentComposer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.baidu.mapapi.map.Polyline
import com.baidu.mapapi.map.PolylineOptions.LineCapType
import com.baidu.mapapi.map.PolylineOptions.LineJoinType
import com.baidu.mapapi.model.LatLng
import com.github.speak2me.compose.map.baidu.ktx.addPolyline

internal class PolylineNode(
    val polyline: Polyline,
    var onPolylineClick: (Polyline) -> Unit
) : MapNode {
    override fun onRemoved() {
        polyline.remove()
    }
}

/**
 * A composable for a polyline on the map.
 *
 * @param points the points comprising the polyline
 * @param clickable boolean indicating if the polyline is clickable or not
 * @param color the color of the polyline
 * @param endCap a cap at the end vertex of the polyline
 * @param geodesic specifies whether to draw the polyline as a geodesic
 * @param jointType the joint type for all vertices of the polyline except the start and end
 * vertices
 * @param pattern the pattern for the polyline
 * @param startCap the cap at the start vertex of the polyline
 * @param visible the visibility of the polyline
 * @param width the width of the polyline in screen pixels
 * @param zIndex the z-index of the polyline
 * @param onClick a lambda invoked when the polyline is clicked
 */
@Composable
@BaiduMapComposable
public fun Polyline(
    points: List<LatLng>,
    clickable: Boolean = false,
    color: Color = Color.Black,
//    endCap: Cap = ButtCap(),
    lineCapType: LineCapType = LineCapType.LineCapButt,
    geodesic: Boolean = false,
//    jointType: Int = JointType.DEFAULT,
    lineJoinType: LineJoinType = LineJoinType.LineJoinMiter,
//    pattern: List<PatternItem>? = null,
//    startCap: Cap = ButtCap(),
    tag: Any? = null,
    visible: Boolean = true,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {}
) {
    PolylineImpl(
        points = points,
        clickable = clickable,
        color = color,
//        endCap = endCap,
        lineCapType = lineCapType,
        geodesic = geodesic,
//        jointType = jointType,
        lineJoinType = lineJoinType,
//        pattern = pattern,
//        startCap = startCap,
        tag = tag,
        visible = visible,
        width = width,
        zIndex = zIndex,
        onClick = onClick,
    )
}

/**
 * A composable for a polyline on the map that supports a StyleSpan.
 *
 * @param points the points comprising the polyline
 * @param spans style spans for the polyline
 * @param clickable boolean indicating if the polyline is clickable or not
 * @param endCap a cap at the end vertex of the polyline
 * @param geodesic specifies whether to draw the polyline as a geodesic
 * @param jointType the joint type for all vertices of the polyline except the start and end
 * vertices
 * @param pattern the pattern for the polyline
 * @param startCap the cap at the start vertex of the polyline
 * @param visible the visibility of the polyline
 * @param width the width of the polyline in screen pixels
 * @param zIndex the z-index of the polyline
 * @param onClick a lambda invoked when the polyline is clicked
 */
@Composable
@BaiduMapComposable
public fun Polyline(
    points: List<LatLng>,
//    spans: List<StyleSpan>,
    clickable: Boolean = false,
//    endCap: Cap = ButtCap(),
    lineCapType: LineCapType = LineCapType.LineCapButt,
    geodesic: Boolean = false,
//    jointType: Int = JointType.DEFAULT,
    lineJoinType: LineJoinType = LineJoinType.LineJoinMiter,
//    pattern: List<PatternItem>? = null,
//    startCap: Cap = ButtCap(),
    tag: Any? = null,
    visible: Boolean = true,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {},
) {
    PolylineImpl(
        points = points,
//        spans = spans,
        clickable = clickable,
//        endCap = endCap,
        lineCapType = lineCapType,
        geodesic = geodesic,
//        jointType = jointType,
        lineJoinType = lineJoinType,
//        pattern = pattern,
//        startCap = startCap,
//        tag = tag,
        visible = visible,
        width = width,
        zIndex = zIndex,
        onClick = onClick,
    )
}

/**
 * Internal implementation for an advanced polyline on a Google map.
 *
 * @param points the points comprising the polyline
 * @param spans style spans for the polyline
 * @param clickable boolean indicating if the polyline is clickable or not
 * @param color the color of the polyline
 * @param endCap a cap at the end vertex of the polyline
 * @param geodesic specifies whether to draw the polyline as a geodesic
 * @param jointType the joint type for all vertices of the polyline except the start and end
 * vertices
 * @param pattern the pattern for the polyline
 * @param startCap the cap at the start vertex of the polyline
 * @param visible the visibility of the polyline
 * @param width the width of the polyline in screen pixels
 * @param zIndex the z-index of the polyline
 * @param onClick a lambda invoked when the polyline is clicked
 */
@Composable
@BaiduMapComposable
private fun PolylineImpl(
    points: List<LatLng>,
//    spans: List<StyleSpan> = emptyList(),
    clickable: Boolean = false,
    color: Color = Color.Black,
//    endCap: Cap = ButtCap(),
    lineCapType: LineCapType = LineCapType.LineCapButt,
    isDottedLine: Boolean = false,
    geodesic: Boolean = false,
//    jointType: Int = JointType.DEFAULT,
    lineJoinType: LineJoinType = LineJoinType.LineJoinMiter,
//    pattern: List<PatternItem>? = null,
//    startCap: Cap = ButtCap(),
    tag: Any? = null,
    visible: Boolean = true,
    width: Float = 10f,
    zIndex: Float = 0f,
    onClick: (Polyline) -> Unit = {},
) {
    val mapApplier = currentComposer.applier as MapApplier?
    ComposeNode<PolylineNode, MapApplier>(
        factory = {
            val polyline = mapApplier?.map?.addPolyline {
                points(points)
//                addAllSpans(spans)
                clickable(clickable)
                color(color.toArgb())
//                endCap(endCap)
                lineCapType(lineCapType)
                isGeodesic(geodesic)
//                jointType(jointType)
                lineJoinType(lineJoinType)
//                pattern(pattern)
//                startCap(startCap)
                visible(visible)
                width(width)
                zIndex(zIndex.toInt())
            } ?: error("Error adding Polyline")
//            polyline.tag = tag
            PolylineNode(polyline, onClick)
        },
        update = {
            update(onClick) { this.onPolylineClick = it }

            update(points) { this.polyline.points = it }
//            update(spans) { this.polyline.spans = it }
            update(clickable) { this.polyline.isClickable = it }
            update(color) { this.polyline.color = it.toArgb() }
//            update(endCap) { this.polyline.endCap = it }
            update(geodesic) { this.polyline.isGeodesic = it }
            update(isDottedLine) { this.polyline.isDottedLine = it }
            update(lineCapType) { this.polyline.lineCapType = it }
            update(lineJoinType) { this.polyline.lineJoinType = it }
//            update(pattern) { this.polyline.pattern = it }
//            update(startCap) { this.polyline.startCap = it }
//            update(tag) { this.polyline.tag = it }
            update(visible) { this.polyline.isVisible = it }
            update(width) { this.polyline.setWidth(it) }
            update(zIndex) { this.polyline.zIndex = it.toInt() }
        }
    )
}
