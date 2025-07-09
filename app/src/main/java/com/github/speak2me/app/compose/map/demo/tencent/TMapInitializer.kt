package com.github.speak2me.app.compose.map.demo.tencent

import android.content.Context
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.tencentmap.mapsdk.maps.TencentMapInitializer

internal object TMapInitializer {

    fun initialize(context: Context) {
        TencentMapInitializer.setAgreePrivacy(context, true)
        TencentMapInitializer.start(context)
        TencentLocationManager.setUserAgreePrivacy(true)
    }

}