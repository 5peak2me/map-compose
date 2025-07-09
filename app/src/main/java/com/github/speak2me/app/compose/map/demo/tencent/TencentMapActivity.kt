package com.github.speak2me.app.compose.map.demo.tencent

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Satellite
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.components.Bubble
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.demo.components.MarkerInfoContent
import com.github.speak2me.app.compose.map.demo.utils.saveBitmapToFile
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.tencent.ComposeMapColorScheme
import com.github.speak2me.compose.map.tencent.MapEffect
import com.github.speak2me.compose.map.tencent.MapProperties
import com.github.speak2me.compose.map.tencent.MapType
import com.github.speak2me.compose.map.tencent.MapUiSettings
import com.github.speak2me.compose.map.tencent.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.tencent.Marker
import com.github.speak2me.compose.map.tencent.MarkerInfoWindow
import com.github.speak2me.compose.map.tencent.MarkerInfoWindowComposable
import com.github.speak2me.compose.map.tencent.Polyline
import com.github.speak2me.compose.map.tencent.TencentMap
import com.github.speak2me.compose.map.tencent.rememberCameraPositionState
import com.github.speak2me.compose.map.tencent.rememberUpdatedMarkerState
import com.tencent.gaya.foundation.api.interfaces.Removable
import com.tencent.tencentmap.mapsdk.maps.TencentMapOptions
import com.tencent.tencentmap.mapsdk.maps.model.BitmapDescriptorFactory
import com.tencent.tencentmap.mapsdk.maps.model.LatLng
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationConfig
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle
import com.tencent.tencentmap.mapsdk.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE
import kotlinx.coroutines.launch

class TencentMapActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                TencentMapScreen()
            }
        }
    }
}

private val defaultLocation = LatLng(31.849193465179447, 117.12530114551642)

@Composable
private fun TencentMapScreen() {

    val context = LocalContext.current
    val view = LocalView.current

    val cameraPositionState = rememberCameraPositionState()

    val locationSource = remember {
        TencentMapLocationSource(context, cameraPositionState)
    }

    val locations = remember {
        mutableStateListOf<LatLng>()
    }

    var properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL,
            )
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                scaleControlsEnabled = false,
                myLocationButtonEnabled = false,
//                zoomControlsEnabled = false,
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
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Satellite),
        LayerTypeItem(MapType.NEW_3D_IMMERSIVE, Icons.Filled.Terrain),
        LayerTypeItem(MapType.TRAFFIC_NAVI, Icons.Filled.Traffic),
        LayerTypeItem(MapType.TRAFFIC_NIGHT, Icons.Filled.TrackChanges),
        LayerTypeItem(MapType.NIGHT, Icons.Filled.Nightlight),
        LayerTypeItem(MapType.NAVI, Icons.Default.Navigation),
    )

    var isMapLoaded by remember { mutableStateOf(false) }
    var shouldTakeScreenshot by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.835367246976922, 117.09781923241997)
    ).apply {
//        showInfoWindow()
        println("markerState: $this")
    }

    val icon = remember {
        BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_point)
    }

    TencentMap(
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = properties,
        uiSettings = uiSettings,
        mapColorScheme = mapColorScheme,
        onMapClick = {
            println("t onMapClick: $it")
            locations.add(it)
        },
        onPOIClick = {
            println("t onPOIClick: ${it.position}, ${it.name}")
            locations.add(it.position)
        },
        onMapLoaded = {
            isMapLoaded = true
        }
    ) {
        @OptIn(MapsComposeExperimentalApi::class)
        MapEffect {
            it.myLocationConfig = MyLocationConfig.newBuilder(it.myLocationConfig)
                .setMyLocationStyle(
                    MyLocationStyle().apply {
                        icon(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
                        myLocationType(LOCATION_TYPE_LOCATION_ROTATE)
                    }
                )
                .build()
            it.uiSettings.setLogoPositionWithMargin(TencentMapOptions.LOGO_POSITION_BOTTOM_LEFT, 0, view.height / 2, 0, 0)
        }

        Marker(
            position = defaultLocation,
            drawable = R.drawable.route_ic_track_location,
            onClick = {
                locations.add(it)
            }
        )

        Marker(
            position = LatLng(31.835367246976922, 117.09781923241997),
            drawable = R.drawable.route_ic_point_start,
        )
//        MarkerInfoWindowComposable(
//            state = markerState,
//            zIndex = 10f,
//            anchor = Offset(0.5f, 0.5f),
//            infoWindowAnchor = Offset(0.5f, 1f),
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
            infoWindowAnchor = Offset(0.5f, 1f),
            onClick = {
                markerState.showInfoWindow()
                true
            },
        ) {
            MarkerInfoContent()
        }

        Locations(locations, onClick = locations::add)
        Polyline(points = locations.map { it })

        if (isMapLoaded) {
            Screenshot(shouldTakeScreenshot) {
                it?.let {
                    scope.launch {
                        context.saveBitmapToFile(it, "tencent")
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
}

@Composable
private fun Marker(position: LatLng, @DrawableRes drawable: Int, tag: Any? = null, onClick: (LatLng) -> Unit = {}) {
    Marker(
        state = rememberUpdatedMarkerState(position),
        icon = BitmapDescriptorFactory.fromResource(drawable),
        anchor = Offset(0.5f, 0.5f),
        tag = tag,
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
                tag = "t-marker",
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
            map.screenMarkers.filter { it.tag == null }.forEach(Removable::remove)
            map.snapshot({ bitmap ->
                onScreenshotTaken(bitmap)
            }, Bitmap.Config.ARGB_8888)
        }
    }
}