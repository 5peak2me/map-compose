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
package com.github.speak2me.compose.map.baidu.ktx

import android.view.View
import android.view.View.OnAttachStateChangeListener
import com.baidu.mapapi.map.BaiduMap
import com.baidu.mapapi.map.MapView
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * A suspending function that provides an instance of [BaiduMap] from this [MapView]. This is
 * an alternative to [View.addOnAttachStateChangeListener] by using coroutines to obtain the [BaiduMap].
 *
 * @return the [BaiduMap] instance
 */
public suspend inline fun MapView.awaitMap(): BaiduMap =
    suspendCoroutine { continuation ->
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                continuation.resume(map)
            }

            override fun onViewDetachedFromWindow(v: View) = Unit
        })
    }