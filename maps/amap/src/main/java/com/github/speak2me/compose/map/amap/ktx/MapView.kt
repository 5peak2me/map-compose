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
package com.github.speak2me.compose.map.amap.ktx

import android.view.View
import android.view.View.OnAttachStateChangeListener
import com.amap.api.maps.AMap
//import com.amap.api.maps.MapView
import com.amap.api.maps.TextureMapView as MapView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A suspending function that provides an instance of [AMap] from this [MapView]. This is
 * an alternative to [AMap.setOnMapLoadedListener] by using coroutines to obtain the [AMap].
 *
 * @return the [AMap] instance
 */
public suspend inline fun MapView.awaitMap(): AMap =
    suspendCoroutine { continuation ->
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                continuation.resume(map)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }
