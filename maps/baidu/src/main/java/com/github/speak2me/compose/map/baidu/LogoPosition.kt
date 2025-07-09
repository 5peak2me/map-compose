package com.github.speak2me.compose.map.baidu

import androidx.compose.runtime.Immutable

@Immutable
public enum class LogoPosition(public val value: Int) {
    LEFT_BOTTOM(com.baidu.mapapi.map.LogoPosition.logoPostionleftBottom.ordinal),
    LEFT_TOP(com.baidu.mapapi.map.LogoPosition.logoPostionleftTop.ordinal),
    CENTER_BOTTOM(com.baidu.mapapi.map.LogoPosition.logoPostionCenterBottom.ordinal),
    CENTER_TOP(com.baidu.mapapi.map.LogoPosition.logoPostionCenterTop.ordinal),
    RIGHT_BOTTOM(com.baidu.mapapi.map.LogoPosition.logoPostionRightBottom.ordinal),
    RIGHT_TOP(com.baidu.mapapi.map.LogoPosition.logoPostionRightTop.ordinal),
}