package com.github.speak2me.app.compose.map.demo.baidu

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.demo.components.MarkerInfoContent
import com.github.speak2me.app.compose.map.demo.utils.saveBitmapToFile
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.gcj02ToBd09
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.baidu.BaiduMap
import com.github.speak2me.compose.map.baidu.ComposeMapColorScheme
import com.github.speak2me.compose.map.baidu.LogoPosition
import com.github.speak2me.compose.map.baidu.MapEffect
import com.github.speak2me.compose.map.baidu.MapProperties
import com.github.speak2me.compose.map.baidu.MapType
import com.github.speak2me.compose.map.baidu.MapUiSettings
import com.github.speak2me.compose.map.baidu.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.baidu.Marker
import com.github.speak2me.compose.map.baidu.MarkerInfoWindow
import com.github.speak2me.compose.map.baidu.MarkerInfoWindowComposable
import com.github.speak2me.compose.map.baidu.Polyline
import com.github.speak2me.compose.map.baidu.rememberCameraPositionState
import com.github.speak2me.compose.map.baidu.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

class BaiduMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                BaiduMapScreen()
            }
        }
    }
}

private val defaultLocation = LatLng(31.849193465179447, 117.12530114551642)

@Composable
private fun BaiduMapScreen() {
    val context = LocalContext.current
    val view = LocalView.current

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

    val locations = remember {
        mutableStateListOf<LatLng>()
    }

    var properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL,
                logoPosition = LogoPosition.LEFT_BOTTOM
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
//                myLocationButtonEnabled = false,
                zoomControlsEnabled = false,
            )
        )
    }

    var mapColorScheme by remember {
        mutableStateOf(ComposeMapColorScheme.LIGHT)
    }

    // Define your layer types here
    val layerTypes = listOf(
        LayerTypeItem(MapType.NONE, Icons.Filled.HourglassEmpty),
        LayerTypeItem(MapType.NORMAL, Icons.Filled.Map),
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Public),
    )

    var isMapLoaded by remember { mutableStateOf(false) }
    var shouldTakeScreenshot by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()


    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.847867, 117.128157)
    ).apply {
//        showInfoWindow()
        println("markerState: $this")
    }

    val icon = remember {
        BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_point)
    }

    BaiduMap(
        cameraPositionState = cameraPositionState,
        properties = properties,
        uiSettings = uiSettings,
        mapColorScheme = mapColorScheme,
        onMapClick = {
            println("b onMapClick: $it")
            locations.add(it)
        },
        onPOIClick = {
            println("b onPOIClick: ${it.position}, ${it.name}")
            locations.add(it.position)
        },
        onMapLoaded = {
            isMapLoaded = true
        },
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        @OptIn(MapsComposeExperimentalApi::class)
        MapEffect(shouldTakeScreenshot) {
            it.setMyLocationConfiguration(
//                MyLocationConfiguration(
//                    MyLocationConfiguration.LocationMode.FOLLOWING,
//                    true,
//                    BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location),
//                    0xAAFFFF88.toInt(),
//                    0xAAFFFF88.toInt()
//                ).apply {
//                    setMarkerSize(0.5f)
//                    setArrowSize(0f)
//                }

                MyLocationConfiguration.Builder(
                    MyLocationConfiguration.LocationMode.FOLLOWING,
                    true
                ).apply {
                    setCustomMarker(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
                    setMarkerSize(0.5f)
                    setArrowSize(0f)
                }.build()
            )

            it.isMyLocationEnabled = true

            client.registerLocationListener(object : BDAbstractLocationListener() {
                override fun onReceiveLocation(location: BDLocation) {
                    println("location b: $location")
                    cameraPositionState.move(
                        MapStatusUpdateFactory.newLatLngZoom(
                            LatLng(location.latitude, location.longitude),
                            16.5F
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

        Marker(
            position = gcj02ToBd09(defaultLocation.latitude, defaultLocation.longitude).asLatLng(),
            drawable = R.drawable.route_ic_track_location,
            onClick = {
                locations.add(it)
            }
        )

        Marker(
            position = LatLng(31.847867, 117.128157),
            drawable = R.drawable.route_ic_point_start,
        )
//        MarkerInfoWindowComposable(
//            state = markerState,
//            zIndex = 10f,
//            flat = true,
//            anchor = Offset(0.5f, 0.5f),
////            infoWindowAnchor = Offset(0.5f, 0.5f),
//            onClick = {
//                markerState.showInfoWindow()
//                true
//            },
//            infoContent = {
//                MarkerInfoContent()
//            }
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.route_ic_track_point),
//                contentDescription = "Marker",
//            )
//        }
        MarkerInfoWindow(
            state = markerState,
            icon = icon,
            zIndex = 10f,
            anchor = Offset(0.5f, 0.5f),
            onClick = {
                markerState.showInfoWindow()
                true
            },
        ) {
            MarkerInfoContent()
        }

        Locations(locations, onClick = locations::add)
        if (locations.size > 1) {
            Polyline(points = locations.map { it })
        }

        if (isMapLoaded) {
            Screenshot(shouldTakeScreenshot) { bitmap ->
                bitmap?.let {
                    scope.launch {
                        context.saveBitmapToFile(bitmap, "baidu")
                    }
                }
                shouldTakeScreenshot = false
            }
        }

    }

    MapPanel(
        onThemeChange = {
            mapColorScheme = if (mapColorScheme == ComposeMapColorScheme.LIGHT) {
                ComposeMapColorScheme.DARK
            } else {
                ComposeMapColorScheme.LIGHT
            }
        },
        currentMapType = properties.mapType, // Pass the current mapType
        layerTypes = layerTypes,
        onMapTypeChange = { newMapType ->
            properties = properties.copy(mapType = newMapType)
        },
        onScreenshot = {
            shouldTakeScreenshot = true
        }
    ) {

    }

    DisposableEffect(Unit) {
        onDispose {
            client.stop()
        }
    }
}

@Composable
private fun Marker(position: LatLng, @DrawableRes drawable: Int, onClick: (LatLng) -> Unit = {}) {
    Marker(
        state = rememberUpdatedMarkerState(position),
        icon = BitmapDescriptorFactory.fromResource(drawable),
        anchor = Offset(0.5f, 0.5f),
        onClick = {
            onClick(it.position)
            true
        }
    )
}

@Composable
private fun Locations(locations: List<LatLng>, onClick: (LatLng) -> Unit) {
    locations.forEachIndexed { index, location ->
        val descriptor by remember {
            derivedStateOf {
                when (index) {
                    0 -> R.drawable.route_ic_point_start
                    else -> R.drawable.route_ic_point_end
                }
            }
        }
        key(location) {
            Marker(
                position = location,
                drawable = descriptor,
                onClick = { onClick(it); true }
            )
        }
    }
}

@Composable
private fun Screenshot(shouldTakeScreenshot: Boolean, onScreenshotTaken: (Bitmap?) -> Unit) {
    @OptIn(MapsComposeExperimentalApi::class)
    MapEffect(shouldTakeScreenshot) { map ->
        if (shouldTakeScreenshot) {
            map.snapshot { bitmap ->
                onScreenshotTaken(bitmap)
            }
        }
    }
}

internal fun Pair<Double, Double>.asLatLng(): LatLng {
    return LatLng(this.first, this.second)
}
