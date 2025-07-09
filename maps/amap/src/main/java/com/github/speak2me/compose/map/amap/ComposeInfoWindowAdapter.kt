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

import android.graphics.Color
import android.view.View
import android.widget.Space
import androidx.compose.ui.platform.ComposeView
import com.amap.api.maps.AMap
//import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView as MapView
import com.amap.api.maps.model.Marker

/**
 * An InfoWindowAdapter that returns a [ComposeView] for drawing a marker's
 * info window.
 *
 * Note: As of version 18.0.2 of the Maps SDK, info windows are drawn by
 * creating a bitmap of the [View]s returned in the [AMap.InfoWindowAdapter]
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
) : AMap.InfoWindowAdapter {

    /**
     * 此方法和 getInfoWindow（Marker marker）方法的实质是一样的，唯一的区别是：此方法不能修改整个
     * InfoWindow 的背景和边框，无论自定义的样式是什么样，SDK 都会在最外层添加一个默认的边框。
     */
    override fun getInfoContents(marker: Marker): View? {
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
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

    /**
     * [当实现此方法并返回有效值时（返回值不为空，则视为有效）,SDK 将不会使用默认的样式，而采用此方法
      返回的样式（即 View）。默认会将Marker 的 title 和 snippet 显示到 InfoWindow 中。](https://lbs.amap.com/api/android-sdk/guide/draw-on-map/draw-marker#s1)
     * - 注意：如果此方法返回的 View 没有设置 InfoWindow 背景图，SDK 会默认添加一个背景图。
     */
    override fun getInfoWindow(marker: Marker): View? {
        val markerNode = markerNodeFinder(marker) ?: return Space(mapView.context)
        val infoWindow = markerNode.infoWindow
        if (infoWindow == null) {
            return null
        }
        val view = ComposeView(mapView.context).apply {
            setContent { infoWindow(marker) }
            setBackgroundColor(Color.TRANSPARENT)
        }
        mapView.renderComposeViewOnce(view, parentContext = markerNode.compositionContext)
        return view
    }

}
