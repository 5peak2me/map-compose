package com.github.speak2me.app.compose.map.demo.google

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.wgs84ToGcj02
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState

class GoogleMapLocationSource(
    context: Context,
    private val cameraPositionState: CameraPositionState
) : LocationSource {

    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                println("location g = $location")

                val (gcjLat, gcjLon) = wgs84ToGcj02(location.latitude, location.longitude)
                val transformed = LatLng(gcjLat, gcjLon)
                println("location g = $transformed")
                listener.onLocationChanged(transformed.toLocation())
                cameraPositionState.move(
                    CameraUpdateFactory.newLatLngZoom(transformed, 15f)
                )
            }
        }
    }

    override fun deactivate() {

    }

    private fun LatLng.toLocation(): Location {
        return com.github.speak2me.app.compose.map.route.plan.ktx.Location(latitude, latitude)
    }
}
