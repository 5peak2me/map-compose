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

import androidx.compose.runtime.Immutable
import com.amap.api.maps.AMap

/**
 * Enumerates the different types of map tiles.
 */
@Immutable
public enum class MapType(public val value: Int) {
    NORMAL(AMap.MAP_TYPE_NORMAL),
    SATELLITE(AMap.MAP_TYPE_SATELLITE),
    NIGHT(AMap.MAP_TYPE_NIGHT),
    NAVI(AMap.MAP_TYPE_NAVI),
    BUS(AMap.MAP_TYPE_BUS),
    NAVI_NIGHT(AMap.MAP_TYPE_NAVI_NIGHT);
}
