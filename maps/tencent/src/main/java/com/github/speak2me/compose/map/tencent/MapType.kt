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

import androidx.compose.runtime.Immutable
import com.tencent.tencentmap.mapsdk.maps.TencentMap

/**
 * Enumerates the different types of map tiles.
 */
@Immutable
public enum class MapType(public val value: Int) {
    NONE(TencentMap.MAP_TYPE_NONE),
    NORMAL(TencentMap.MAP_TYPE_NORMAL),
    SATELLITE(TencentMap.MAP_TYPE_SATELLITE),
    NEW_3D_IMMERSIVE(TencentMap.MAP_TYPE_NEW_3D_IMMERSIVE),
    TRAFFIC_NAVI(TencentMap.MAP_TYPE_TRAFFIC_NAVI),
    TRAFFIC_NIGHT(TencentMap.MAP_TYPE_TRAFFIC_NIGHT),
    NIGHT(TencentMap.MAP_TYPE_NIGHT),
    NAVI(TencentMap.MAP_TYPE_NAVI);
}
