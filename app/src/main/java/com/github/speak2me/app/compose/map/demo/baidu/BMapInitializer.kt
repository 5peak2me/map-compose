package com.github.speak2me.app.compose.map.demo.baidu

import android.content.Context
import com.baidu.location.LocationClient
import com.baidu.mapapi.CoordType
import com.baidu.mapapi.SDKInitializer

internal object BMapInitializer {

    fun initialize(context: Context) {
        SDKInitializer.setAgreePrivacy(context, true)
        SDKInitializer.initialize(context)
        SDKInitializer.setCoordType(CoordType.BD09LL)
        LocationClient.setAgreePrivacy(true)
    }

}