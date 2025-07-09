package com.github.speak2me.app.compose.map.demo.baidu

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.baidu.location.BDAbstractLocationListener
import com.baidu.location.BDLocation
import com.baidu.location.LocationClient
import com.baidu.location.LocationClientOption
import com.baidu.location.LocationClientOption.LocationMode
import com.baidu.mapapi.map.BitmapDescriptorFactory
import com.baidu.mapapi.map.MapStatusUpdateFactory
import com.baidu.mapapi.map.MyLocationConfiguration
import com.baidu.mapapi.map.MyLocationData
import com.baidu.mapapi.model.LatLng
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.baidu.BaiduMap
import com.github.speak2me.compose.map.baidu.MapEffect
import com.github.speak2me.compose.map.baidu.MapProperties
import com.github.speak2me.compose.map.baidu.MapType
import com.github.speak2me.compose.map.baidu.MapUiSettings
import com.github.speak2me.compose.map.baidu.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.baidu.rememberCameraPositionState

class BaiduMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT
            )
        )
        setContent {
            MapComposeTheme {
                BaiduMapScreen()
            }
        }
    }
}

private val defaultLocation = LatLng(31.847229, 117.122685)

@Composable
private fun BaiduMapScreen() {
    val context = LocalContext.current

    val client = LocationClient(context)
    val locationOption = LocationClientOption().apply {
        locationMode = LocationMode.Hight_Accuracy
        openGps = true
        coorType = "bd09ll"
//        coorType = "gcj02"
//        scanSpan = 1000
    }
    client.locOption = locationOption

//    val cameraPositionState = rememberCameraPositionState {
//        position = MapStatus.Builder().target(location).zoom(16F).build()
//    }

    val cameraPositionState = rememberCameraPositionState()

    val properties by remember {
        mutableStateOf(
            MapProperties(
                isBuildingEnabled = false,
                isIndoorEnabled = false,
                isMyLocationEnabled = true,
                isTrafficEnabled = false,
                latLngBoundsForCameraTarget = null,
                mapStyleOptions = null,
                mapType = MapType.NORMAL,
                maxZoomPreference = 21.0f,
                minZoomPreference = 3.0f,
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                indoorLevelPickerEnabled = true,
                mapToolbarEnabled = true,
                myLocationButtonEnabled = false,
                rotationGesturesEnabled = true,
                scaleControlsEnabled = true,
                scrollGesturesEnabled = true,
                scrollGesturesEnabledDuringRotateOrZoom = true,
                tiltGesturesEnabled = true,
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
            )
        )
    }

    BaiduMap(
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = {
            println("b onMapClick: $it")
        },
        onPOIClick = {
            println("b onPOIClick: ${it.position}, ${it.name}")
        }
    ) {
//        Marker(
//            state = rememberUpdatedMarkerState(defaultLocation),
//            icon = BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_location)
//        )
        @OptIn(MapsComposeExperimentalApi::class)
        MapEffect {
            it.snapshot {

            }
//            it.setMyLocationConfiguration(
//                MyLocationConfiguration(
//                    MyLocationConfiguration.LocationMode.FOLLOWING,
//                    true,
//                    BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location),
//                    0xAAFFFF88.toInt(),
//                    0xAAFFFF88.toInt()
//                ).apply {
//                    markerSize = 20F
//                }
////                MyLocationConfiguration.Builder(
////                    MyLocationConfiguration.LocationMode.FOLLOWING,
////                    true
////                ).apply {
////                    setCustomMarker(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
////                }.build()
//            )

            it.isMyLocationEnabled = true

            client.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation) {
                    println("location b: $location")
                    cameraPositionState.move(
                        MapStatusUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            16F
                        )
                    )
                    it.setMyLocationData(
                        MyLocationData.Builder()
                            .accuracy(location.radius)
                            .direction(location.direction)
                            .latitude(location.latitude)
                            .longitude(location.longitude)
                            .build()
                    )
                }
            })
            client.start()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            client.stop()
        }
    }
}
