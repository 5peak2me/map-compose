package com.github.speak2me.app.compose.map.demo.google

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.text.format.DateFormat
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.wgs84ToGcj02
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.LocationSource
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GoogleMapLocationSource(
    context: Context,
    private val cameraPositionState: CameraPositionState
) : LocationSource {

    internal val current: StateFlow<LatLng?>
        field = MutableStateFlow(null)

    private val client by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    @SuppressLint("MissingPermission")
    override fun activate(listener: LocationSource.OnLocationChangedListener) {
        println("g: activate")
//        val cancellationTokenSource = CancellationTokenSource()
//        client.getCurrentLocation(
//            Priority.PRIORITY_HIGH_ACCURACY,
//            cancellationTokenSource.token
//        ).addOnSuccessListener { location ->
//            if (location != null) {
//                println("current location g = $location")
//
//                val (gcjLat, gcjLon) = wgs84ToGcj02(location.latitude, location.longitude)
//                val transformed = LatLng(gcjLat, gcjLon)
//                println("transformed location g = $transformed")
//                listener.onLocationChanged(transformed.toLocation())
//                cameraPositionState.move(
//                    CameraUpdateFactory.newLatLngZoom(transformed, 15f)
//                )
//            }
//        }.addOnFailureListener { exception ->
//            println("Failed to get current location: ${exception.message}")
//        }
        client.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val datetime = DateFormat.format("yyyy-MM-dd HH:mm:ss", location.time)
                println("location g = ${datetime}: $location")

                val (gcjLat, gcjLon) = wgs84ToGcj02(location.latitude, location.longitude)
                val transformed = LatLng(gcjLat, gcjLon)

                current.tryEmit(transformed)

                println("location g = $transformed")
                listener.onLocationChanged(transformed.toLocation())
//                cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(transformed, 15f))
            }
        }.addOnFailureListener { exception ->
            println("Failed to get current location: ${exception.stackTraceToString()}")
        }
    }

    override fun deactivate() {
        println("g: deactivate")
    }

    private fun LatLng.toLocation(): Location {
        return com.github.speak2me.app.compose.map.route.plan.ktx.Location(latitude, longitude)
    }
}
