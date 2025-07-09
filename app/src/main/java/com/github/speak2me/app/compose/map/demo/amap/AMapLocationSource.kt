package com.github.speak2me.app.compose.map.demo.amap

import android.content.Context
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.LocationSource
import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.CameraPositionState

class AMapLocationSource(
    context: Context,
    private val cameraPositionState: CameraPositionState,
) : LocationSource {

    private val converter = CoordinateConverter(context)

    private val option by lazy {
        AMapLocationClientOption().apply {
            locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
//            interval = 2000
            isOnceLocation = true
            isOnceLocationLatest = true
        }
    }

    private val client by lazy {
        AMapLocationClient(context).apply {
            setLocationOption(option)
        }
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        converter.from(CoordinateConverter.CoordType.GPS)

        println("activate")
        client.startLocation()

        val location = client.lastKnownLocation
        listener.onLocationChanged(location)
        println("location a = $location")

        val latLng = converter.coord(LatLng(location.latitude, location.longitude)).convert()
        println(latLng)
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 15.5f))

//        client.setLocationListener {
//            listener.onLocationChanged(it)
//            println("location a = $it")
//            val latLng = converter.coord(LatLng(it.latitude, it.longitude)).convert()
//            println(latLng)
//            cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 15.5f))
//        }
    }

    override fun deactivate() {
        println("deactivate")
        client.stopLocation()
        client.setLocationListener(null)
        client.onDestroy()
    }
}
