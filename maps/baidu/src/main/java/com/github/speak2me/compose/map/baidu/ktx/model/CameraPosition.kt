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

import com.baidu.mapapi.map.MapStatus

/**
 * Builds a new [MapStatus] using the provided [optionsActions]. Using this removes the need
 * to construct a [MapStatus.Builder] object.
 *
 * @return the constructed [MapStatus]
 */
public inline fun cameraPosition(optionsActions: MapStatus.Builder.() -> Unit): MapStatus =
    MapStatus.Builder().apply(optionsActions).build()
