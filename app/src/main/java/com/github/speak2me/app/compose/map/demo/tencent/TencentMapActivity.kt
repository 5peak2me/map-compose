package com.github.speak2me.app.compose.map.demo.tencent

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.tencent.MapEffect
import com.github.speak2me.compose.map.tencent.MapProperties
import com.github.speak2me.compose.map.tencent.MapType
import com.github.speak2me.compose.map.tencent.MapUiSettings
import com.github.speak2me.compose.map.tencent.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.tencent.Marker
import com.github.speak2me.compose.map.tencent.TencentMap
import com.github.speak2me.compose.map.tencent.rememberCameraPositionState
import com.github.speak2me.compose.map.tencent.rememberUpdatedMarkerState
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationConfig
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE

class TencentMapActivity : ComponentActivity() {
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
                TencentMapScreen()
            }
        }
    }
}

private val defaultLocation = LatLng(31.847229, 117.122685)

@Composable
private fun TencentMapScreen() {

    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState()

    val locationSource = remember {
        TencentMapLocationSource(context, cameraPositionState)
    }

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

    TencentMap(
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = properties,
        uiSettings = uiSettings,
        onMapClick = {
            println("t onMapClick: $it")
        },
        onPOIClick = {
            println("t onPOIClick: ${it.position}, ${it.name}")
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
            it.myLocationConfig = MyLocationConfig.newBuilder(it.myLocationConfig)
                .setMyLocationStyle(
                    MyLocationStyle().apply {
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
                        myLocationType(LOCATION_TYPE_LOCATION_ROTATE)
                    }
                )
                .build()
        }
    }
}
