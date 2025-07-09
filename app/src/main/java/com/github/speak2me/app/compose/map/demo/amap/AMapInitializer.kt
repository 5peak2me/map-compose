package com.github.speak2me.app.compose.map.demo.amap

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.maps.MapsInitializer

internal object AMapInitializer {

    fun initialize(context: Context) {
        MapsInitializer.updatePrivacyAgree(context, true)
        MapsInitializer.updatePrivacyShow(context, true, true)
        MapsInitializer.initialize(context)
        
        // 初始化高德定位SDK
        try {
            AMapLocationClient.updatePrivacyAgree(context, true)
            AMapLocationClient.updatePrivacyShow(context, true, true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}