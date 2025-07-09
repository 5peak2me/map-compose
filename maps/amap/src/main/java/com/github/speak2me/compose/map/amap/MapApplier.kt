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

import androidx.compose.runtime.AbstractApplier
import com.amap.api.maps.AMap
//import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView as MapView
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.Polyline

internal interface MapNode {
    fun onAttached() {}
    fun onRemoved() {}
    fun onCleared() {}
}

private object MapNodeRoot : MapNode

// [mapClickListeners] must be a singleton for the [map] and is therefore stored here:
// [AMap.setOnIndoorBuildingActiveListener()] will not actually set a new non-null listener if
// called more than once; if [mapClickListeners] were passed through the Compose function hierarchy
// we would need to consider the case of it changing, which would require special treatment
// for that particular listener; yet MapClickListeners never actually changes.
internal class MapApplier(
    val map: AMap,
    internal val mapView: MapView,
    val mapClickListeners: MapClickListeners,
) : AbstractApplier<MapNode>(MapNodeRoot) {

    private val decorations = mutableListOf<MapNode>()

    init {
        attachClickListeners()
    }

    override fun onClear() {
        map.clear()
        decorations.forEach { it.onCleared() }
        decorations.clear()
    }

    override fun insertBottomUp(index: Int, instance: MapNode) {
        decorations.add(index, instance)
        instance.onAttached()
    }

    override fun insertTopDown(index: Int, instance: MapNode) {
        // insertBottomUp is preferred
    }

    override fun move(from: Int, to: Int, count: Int) {
        decorations.move(from, to, count)
    }

    override fun remove(index: Int, count: Int) {
        repeat(count) {
            decorations[index + it].onRemoved()
        }
        decorations.remove(index, count)
    }

    internal fun attachClickListeners() {
        map.setOnPolylineClickListener { polyline ->
            decorations.findInputCallback<PolylineNode, Polyline, Unit>(
                nodeMatchPredicate = { it.polyline == polyline },
                nodeInputCallback = { onPolylineClick },
                marker = polyline,
                inputHandlerCallback = { onPolylineClick }
            )
        }

        // Marker
        map.setOnMarkerClickListener { marker ->
            decorations.findInputCallback<MarkerNode, Marker, Boolean>(
                nodeMatchPredicate = { it.marker == marker },
                marker = marker,
                nodeInputCallback = { onMarkerClick },
                inputHandlerCallback = { onMarkerClick }
            )
        }
        map.setOnInfoWindowClickListener { marker ->
            decorations.findInputCallback<MarkerNode, Marker, Unit>(
                nodeMatchPredicate = { it.marker == marker },
                marker = marker,
                nodeInputCallback = { onInfoWindowClick },
                inputHandlerCallback = { onInfoWindowClick }
            )
        }
        map.setOnMarkerDragListener(object : AMap.OnMarkerDragListener {
            // We update MarkerState isDragging & position properties in a specific well-defined
            // order: MarkerState.position is never updated by us unless
            // MarkerState.isDragging == true. This avoids using Snapshots, which can fail to apply;
            // they would not be meaningful here, because we are not the actual source of truth.

            override fun onMarkerDragStart(marker: Marker) {
                decorations.findInputCallback<MarkerNode, Marker, Unit>(
                    nodeMatchPredicate = { it.marker == marker },
                    marker = marker,
                    nodeInputCallback = {
                        {
                            val position = it.position

                            markerState.isDragging = true
                            // update position after enabling isDragging
                            markerState.position = position

                            @Suppress("DEPRECATION")
                            markerState.dragState = DragState.START
                        }
                    },
                    inputHandlerCallback = { onMarkerDragStart }
                )
            }

            override fun onMarkerDrag(marker: Marker) {
                decorations.findInputCallback<MarkerNode, Marker, Unit>(
                    nodeMatchPredicate = { it.marker == marker },
                    nodeInputCallback = {
                        {
                            val position = it.position

                            markerState.isDragging = true // just in case, should be set already
                            // update position after enabling isDragging
                            markerState.position = position

                            @Suppress("DEPRECATION")
                            markerState.dragState = DragState.DRAG
                        }
                    },
                    marker = marker,
                    inputHandlerCallback = { onMarkerDrag }
                )
            }

            override fun onMarkerDragEnd(marker: Marker) {
                decorations.findInputCallback<MarkerNode, Marker, Unit>(
                    nodeMatchPredicate = { it.marker == marker },
                    marker = marker,
                    nodeInputCallback = {
                        {
                            val position = it.position

                            markerState.isDragging = true // just in case, should be set already
                            // update position after enabling isDragging
                            markerState.position = position
                            // disable isDragging after updating position
                            markerState.isDragging = false

                            @Suppress("DEPRECATION")
                            markerState.dragState = DragState.END
                        }
                    },
                    inputHandlerCallback = { onMarkerDragEnd }
                )
            }
        })
        map.setInfoWindowAdapter(
            ComposeInfoWindowAdapter(
                mapView,
                markerNodeFinder = { marker ->
                    decorations.firstOrNull { it is MarkerNode && it.marker == marker }
                            as MarkerNode?
                }
            )
        )
    }
}

/**
 * General pattern for handling input. This finds the node that belongs to the clicked item, and executes the callback.
 *
 * If there is none, don't handle.
 */
private inline fun <reified NodeT : MapNode, I, O> Iterable<MapNode>.findInputCallback(
    nodeMatchPredicate: (NodeT) -> Boolean,
    nodeInputCallback: NodeT.() -> ((I) -> O)?,
    marker: I,
    inputHandlerCallback: InputHandlerNode.() -> ((I) -> O)?,
): Boolean {
    var callback: ((I) -> O)?
    for (item in this) {
        if (item is NodeT && nodeMatchPredicate(item)) {
            // Found a matching node
            if (nodeInputCallback(item)?.invoke(marker) == true) {
                return true
            }
        } else if (item is InputHandlerNode) {
            // Found an input handler, but keep looking for matching nodes
            callback = inputHandlerCallback(item)
            if (callback?.invoke(marker) == true) {
                return true
            }
        }
    }
    return false
}
