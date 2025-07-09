package com.github.speak2me.app.compose.map.route.plan.components

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.LocationSource

internal class LocationSourceImpl(
    private val context: Context,
    private val onLocationChanged: ((android.location.Location) -> Unit)? = null,
) : LocationSource {

    private val option by lazy {
        AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
            isSensorEnable = true // 启用传感器获取方向
            isOnceLocation = false // 持续定位以获取方向变化
            isWifiScan = true
            isLocationCacheEnable = false // 不使用缓存定位
            interval = 2000 // 定位间隔2秒
        }
    }

    private val client by lazy {
        AMapLocationClient(context).apply {
            setLocationOption(option)
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        println("activate")
        client.startLocation()
        client.setLocationListener { aMapLocation ->
            // 创建包含方向信息的Location对象
            val location = android.location.Location("AMap").apply {
                latitude = aMapLocation.latitude
                longitude = aMapLocation.longitude
                accuracy = aMapLocation.accuracy
                bearing = aMapLocation.bearing // 设置方向角度
                time = aMapLocation.time
            }
            listener.onLocationChanged(location)
            // 通知外部监听器
            onLocationChanged?.invoke(location)
        }
    }

    override fun deactivate() {
        println("deactivate")
        client.startLocation()
        client.setLocationListener(null)
    }
}
