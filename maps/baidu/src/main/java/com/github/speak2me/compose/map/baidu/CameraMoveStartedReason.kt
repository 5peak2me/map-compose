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

import androidx.compose.runtime.Immutable
import com.baidu.mapapi.map.BaiduMap

/**
 * Enumerates the different reasons why the map camera started to move.
 *
 * Based on enum values from https://lbsyun.baidu.com/faq/api?title=androidsdk/guide/interaction/event#constants.
 *
 * [NO_MOVEMENT_YET] is used as the initial state before any map movement has been observed.
 *
 * [UNKNOWN] is used to represent when an unsupported integer value is provided to [fromInt] - this
 * may be a new constant value from the Maps SDK that isn't supported by maps-compose yet, in which
 * case this library should be updated to include a new enum value for that constant.
 */
@Immutable
public enum class CameraMoveStartedReason(public val value: Int) {
    UNKNOWN(-2),
    NO_MOVEMENT_YET(-1),
    GESTURE(BaiduMap.OnMapStatusChangeListener.REASON_GESTURE),
    API_ANIMATION(BaiduMap.OnMapStatusChangeListener.REASON_API_ANIMATION),
    DEVELOPER_ANIMATION(BaiduMap.OnMapStatusChangeListener.REASON_DEVELOPER_ANIMATION);

    public companion object {
        /**
         * Converts from the Maps SDK [BaiduMap.OnMapStatusChangeListener]
         * constants to [CameraMoveStartedReason], or returns [UNKNOWN] if there is no such
         * [CameraMoveStartedReason] for the given [value].
         *
         * See https://lbsyun.baidu.com/faq/api?title=androidsdk/guide/interaction/event#constants.
         */
        public fun fromInt(value: Int): CameraMoveStartedReason {
            return entries.firstOrNull { it.value == value } ?: return UNKNOWN
        }
    }
}
