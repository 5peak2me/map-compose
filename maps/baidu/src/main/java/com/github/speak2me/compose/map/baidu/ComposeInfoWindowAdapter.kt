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

import android.view.View
import androidx.compose.ui.platform.ComposeView
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.InfoWindow
import com.baidu.mapapi.map.InfoWindowAdapter
import com.baidu.mapapi.map.MapView
import com.baidu.mapapi.map.Marker

/**
 * An InfoWindowAdapter that returns a [ComposeView] for drawing a marker's
 * info window.
 *
 * Note: As of version 18.0.2 of the Maps SDK, info windows are drawn by
 * creating a bitmap of the [View]s returned in the [BaiduMap.InfoWindowAdapter]
 * interface methods. The returned views are never attached to a window,
 * instead, they are drawn to a bitmap canvas. This breaks the assumption
 * [ComposeView] makes where it must eventually be attached to a window. As a
 * workaround, the contained window is temporarily attached to the MapView so
 * that the contents of the ComposeViews are rendered.
 *
 * Eventually when info windows are no longer implemented this way, this
 * implementation should be updated.
 */
internal class ComposeInfoWindowAdapter(
    private val mapView: MapView,
    private val markerNodeFinder: (Marker) -> MarkerNode?
) : InfoWindowAdapter {

    override fun getInfoWindowView(marker: Marker): View? {
        val markerNode = markerNodeFinder(marker) ?: return null
        val content = markerNode.infoContent
        if (content == null) {
            return null
        }
        val view = ComposeView(mapView.context).apply {
            setContent { content(marker) }
        }
        mapView.renderComposeViewOnce(view, parentContext = markerNode.compositionContext)
        return view
    }

    override fun getInfoWindowViewYOffset(): Int = 0

    override fun getInfoWindow(marker: Marker): InfoWindow? {
        val markerNode = markerNodeFinder(marker) ?: return null
        val infoWindow = markerNode.infoWindow
        if (infoWindow == null) {
            return null
        }
        val view = ComposeView(mapView.context).apply {
            setContent { infoWindow(marker) }
        }
        mapView.renderComposeViewOnce(view, parentContext = markerNode.compositionContext)
        return marker.infoWindow
    }

}
