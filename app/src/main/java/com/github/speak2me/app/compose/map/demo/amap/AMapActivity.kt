package com.github.speak2me.app.compose.map.demo.amap

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_FOLLOW
import com.amap.api.maps.model.MyLocationStyle.LOCATION_TYPE_LOCATE
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.ComposeMapColorScheme
import com.github.speak2me.compose.map.amap.MapEffect
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.Polyline
import com.github.speak2me.compose.map.amap.ktx.snapshot
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState

class AMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                AMapScreen()
            }
        }
    }
}

val tracks = listOf(
    LatLng(31.847065, 117.121938),
    LatLng(31.841822, 117.121925),
    LatLng(31.836580, 117.121911),
    LatLng(31.836611, 117.124990),
    LatLng(31.836663, 117.128020),
    LatLng(31.836697, 117.131150),
    LatLng(31.837509, 117.131180),
    LatLng(31.838382, 117.131205),
    LatLng(31.838601, 117.131286),
    LatLng(31.838780, 117.131369),
    LatLng(31.839701, 117.132869),
    LatLng(31.840465, 117.134275),
    LatLng(31.841145, 117.134809),
    LatLng(31.841729, 117.135208),
    LatLng(31.843256, 117.135533),
    LatLng(31.844608, 117.135811),
    LatLng(31.845717, 117.135469),
    LatLng(31.846714, 117.135125),
    LatLng(31.847478, 117.134507),
    LatLng(31.848142, 117.133837),
    LatLng(31.848588, 117.133431),
    LatLng(31.848914, 117.133042),
    LatLng(31.849146, 117.132353),
    LatLng(31.849312, 117.131698),
    LatLng(31.849455, 117.131070),
    LatLng(31.849593, 117.130437),
    LatLng(31.849647, 117.130018),
    LatLng(31.849687, 117.129669),
    LatLng(31.849623, 117.129008),
    LatLng(31.849499, 117.128353),
    LatLng(31.849243, 117.127627),
    LatLng(31.849031, 117.126900),
    LatLng(31.848750, 117.126392),
    LatLng(31.848470, 117.125913),
    LatLng(31.848078, 117.125270),
    LatLng(31.847721, 117.124625),
    LatLng(31.847367, 117.124159),
    LatLng(31.847042, 117.123693),
    LatLng(31.847053, 117.122816),
    LatLng(31.847065, 117.121938)
)

private val defaultLocation = LatLng(31.847229, 117.122685)

@OptIn(MapsComposeExperimentalApi::class)
@Composable
private fun AMapScreen() {

    val context = LocalContext.current

    val converter = CoordinateConverter(context)
    converter.from(CoordinateConverter.CoordType.BAIDU)

    val builder = LatLngBounds.builder()
    val points = tracks.map {
        converter.coord(it).convert().apply(builder::include)
    }

    val cameraPositionState = rememberCameraPositionState()

    val locationSource = remember {
        AMapLocationSource(context, cameraPositionState)
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
                compassEnabled = true,
                myLocationButtonEnabled = true,
                scaleControlsEnabled = false,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = false,
            )
        )
    }

    var mapColorScheme by remember {
        mutableStateOf(ComposeMapColorScheme.LIGHT)
    }

    // Define your layer types here
    val layerTypes = listOf(
        LayerTypeItem(MapType.NORMAL, Icons.Filled.Map, "Normal"),
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Public, "Satellite"),
        LayerTypeItem(MapType.NAVI, Icons.Filled.Navigation, "Navigation"),
        LayerTypeItem(MapType.BUS, Icons.Filled.BusAlert, "Bus"),
        // Add more layer types as needed, e.g., TERRAIN, HYBRID
        // LayerTypeItem(MapType.TERRAIN, Icons.Filled.Terrain, "Terrain"), // Example
    )

    AMap(
//        modifier = Modifier.navigationBarsPadding(),
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = properties,
        uiSettings = uiSettings,
        mapColorScheme = mapColorScheme,
        onMapClick = {
            println("a onMapClick: $it")
            locations.add(it)
        },
        onPOIClick = {
            println("a onPOIClick: $it")
            locations.add(it.coordinate)
        }
    ) {
        Marker(
            state = rememberUpdatedMarkerState(defaultLocation),
            icon = BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_location)
        )

        locations.forEachIndexed { index, location ->
            key(location) {
                Marker(
                    state = rememberUpdatedMarkerState(location),
                    anchor = Offset(0.5f, 0.5f),
                    icon = BitmapDescriptorFactory.fromResource(R.drawable.route_ic_point_start),
                    onClick = { true }
                )
            }
        }

        Polyline(points = locations.map { converter.from(CoordinateConverter.CoordType.GOOGLE).coord(it).convert() })
        Polyline(points = points)

        MapEffect {
//            it.snapshot {
//
//            }
            it.isMyLocationEnabled = true
            it.myLocationStyle = MyLocationStyle().apply {
                myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
                showMyLocation(true)
                myLocationType(LOCATION_TYPE_LOCATE)
                strokeWidth(0f)
                radiusFillColor(Color.TRANSPARENT)
            }
            it.uiSettings.setLogoMarginRate(AMapOptions.LOGO_MARGIN_BOTTOM, 0.5f)
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
    )
}
