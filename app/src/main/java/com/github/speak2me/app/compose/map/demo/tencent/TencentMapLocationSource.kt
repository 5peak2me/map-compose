package com.github.speak2me.app.compose.map.demo.tencent

import android.content.Context
import android.location.Location
import android.os.Looper
import com.github.speak2me.compose.map.tencent.CameraPositionState
import com.tencent.map.geolocation.TencentLocation
import com.tencent.map.geolocation.TencentLocationListener
import com.tencent.map.geolocation.TencentLocationManager
import com.tencent.map.geolocation.TencentLocationManager.COORDINATE_TYPE_GCJ02
import com.tencent.map.geolocation.TencentLocationRequest
import com.tencent.tencentmap.mapsdk.maps.CameraUpdateFactory
import com.tencent.tencentmap.mapsdk.maps.LocationSource
import com.tencent.tencentmap.mapsdk.maps.model.LatLng

/**
 * [单次定位](https://lbs.qq.com/mobile/androidLocationSDK/androidGeoGuide/androidGeoSingle)
 */
class TencentMapLocationSource(
    context: Context,
    private val cameraPositionState: CameraPositionState,
) : LocationSource {

    private lateinit var callback: TencentLocationListener

    private val client by lazy {
        TencentLocationManager.getInstance(context)
    }

    private val request by lazy {
        TencentLocationRequest.create()
//            .setInterval(5000)
            // https://lbs.qq.com/mobile/androidLocationSDK/androidGeoGuide/androidGeoRequestLevel
            .setRequestLevel(TencentLocationRequest.REQUEST_LEVEL_ADMIN_AREA)
//            .setAllowGPS(true)
//            .setAllowDirection(false)
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        println("t: activate")

        callback = object : TencentLocationListener {
            override fun onLocationChanged(location: TencentLocation, error: Int, reason: String) {
                println("location t = $location")
                listener?.onLocationChanged(location.toSysLocation())
                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location.toLatLng(), 14.5f))
            }

            override fun onStatusUpdate(name: String, status: Int, desc: String) = Unit
        }

        client.setDebuggable(true)
        client.requestSingleFreshLocation(request, callback, Looper.myLooper())
        client.coordinateType = COORDINATE_TYPE_GCJ02
    }

    override fun deactivate() {
        println("t: deactivate")
        client.removeUpdates(callback)
    }
}

private fun TencentLocation.toLatLng(): LatLng = LatLng(
    latitude,
    longitude
)

private fun TencentLocation.toSysLocation(): Location = Location("tencent-map-compose").apply {
    latitude = this@toSysLocation.latitude
    longitude = this@toSysLocation.longitude
    altitude = this@toSysLocation.altitude
}
