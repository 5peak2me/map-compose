package com.github.speak2me.app.compose.map.demo.amap

import android.content.Context
import android.location.Location
import android.text.format.DateFormat
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.CameraPositionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

internal class AMapLocationSource(context: Context, val cameraPositionState: CameraPositionState) : LocationSource {

    internal val current: StateFlow<LatLng?>
        field = MutableStateFlow(null)

    private var listener: LocationSource.OnLocationChangedListener? = null

    private val option by lazy {
        AMapLocationClientOption.setLocationProtocol(AMapLocationClientOption.AMapLocationProtocol.HTTPS)
        // https://a.amap.com/lbs/static/unzip/Android_Location_Doc/index.html?com/amap/api/location/class-use/AMapLocationClientOption.html
        AMapLocationClientOption().apply {
            /**
             * 设置定位场景，目前支持三种场景（签到、出行、运动，默认无场景）
             */
//            locationPurpose = AMapLocationClientOption.AMapLocationPurpose.Sport
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
//            interval = 2000
            isOnceLocation = true
            // 获取最近3s内精度最高的一次定位结果：
            // 设置setOnceLocationLatest(boolean b)接口为true，启动定位时SDK会返回最近3s内精度最高的一次定位结果。如果设置其为true，setOnceLocation(boolean b)接口也会被设置为true，反之不会，默认为false。
//            isOnceLocationLatest = true
            isLocationCacheEnable = false // 默认 true
            isSensorEnable = true // 默认 false
//            lastLocationLifeCycle = 30_000 // 默认 30_000
//            isWifiScan = true // 默认 true
//            isNeedAddress = true // 默认 true
//            isLBSLocationEnable = true // 默认 true
//            isSysNetworkLocEnable = true // 默认 true
//            lastLocationLifeCycle = 30_000 // 默认 30_000
//            httpTimeOut = 10_000 // 默认 30_000
//            isGpsFirst = true // 默认 false
//            gpsFirstTimeout = 15_000 // 默认 30_000, min: 5_000, max: 30_000
        }
    }

    private val client by lazy {
        AMapLocationClient(context).apply {
            setLocationOption(option)
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        println("a: activate")
        this.listener = listener
        startLocation()
    }

    private fun startLocation() {
        client.startLocation()
        var location = client.lastKnownLocation
//        if (location == null) {
            client.setLocationListener {
                location = it
                handle(it)
            }
            client.startLocation()
//        } else {
//            handle(location)
//        }
    }

    private fun handle(location: AMapLocation) {
        current.tryEmit(location.toLatLng())
        listener?.onLocationChanged(location)
        val datetime = DateFormat.format("yyyy-MM-dd HH:mm:ss", location.time)
        println("location a = ${datetime}: city = ${location.city}, $location")
//        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), 15f))
    }

    override fun deactivate() {
        println("a: deactivate")
        client.stopLocation()
        client.setLocationListener(null)
        client.onDestroy()
    }

    private fun Location.toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

}
