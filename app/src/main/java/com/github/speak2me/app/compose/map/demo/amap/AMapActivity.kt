package com.github.speak2me.app.compose.map.demo.amap

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusAlert
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.amap.api.maps.AMapOptions
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.MyLocationStyle
import com.github.speak2me.app.compose.map.DisappearingScaleBar
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.demo.components.MarkerInfoContent
import com.github.speak2me.app.compose.map.demo.utils.merge
import com.github.speak2me.app.compose.map.demo.utils.saveBitmapToFile
import com.github.speak2me.app.compose.map.demo.utils.useLocationPermission
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.ComposeMapColorScheme
import com.github.speak2me.compose.map.amap.MapEffect
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.MarkerInfoWindowComposable
import com.github.speak2me.compose.map.amap.Polyline
import com.github.speak2me.compose.map.amap.ktx.snapshot
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

class AMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                AMapScreen()
            }
        }

//        val client = AMapLocationClient(this)
//        client.setLocationListener {
//            println("location111 = $it")
//        }
//        client.startLocation()
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

private val defaultLocation = LatLng(31.849193465179447, 117.12530114551642)

@Composable
private fun AMapScreen() {
    var isMyLocationEnabled by remember { mutableStateOf(false) }

    useLocationPermission(
        onPermissionGranted = {
            isMyLocationEnabled = true
            println("permission granted")
        }
    )
    AMapScreen(isMyLocationEnabled)
}

@Composable
private fun AMapScreen(isMyLocationEnabled: Boolean) {

    val context = LocalContext.current

    val converter = CoordinateConverter(context)
    converter.from(CoordinateConverter.CoordType.BAIDU)

    val builder = LatLngBounds.builder()
    val points = tracks.map {
        converter.coord(it).convert().apply(builder::include)
    }
    // 判断是是横向还是纵向，设置 padding
    val bounds = builder.build()

    val cameraPositionState = rememberCameraPositionState()

    val locationSource = remember {
        AMapLocationSource(context, cameraPositionState)
    }

    var properties by remember(isMyLocationEnabled) {
        mutableStateOf(
            MapProperties(isMyLocationEnabled = isMyLocationEnabled)
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                myLocationButtonEnabled = true,
                scaleControlsEnabled = true,
                tiltGesturesEnabled = false,
//                zoomControlsEnabled = false,
            )
        )
    }

    var mapColorScheme by remember {
        mutableStateOf(ComposeMapColorScheme.LIGHT)
    }

    // Define your layer types here
    val layerTypes = listOf(
        LayerTypeItem(MapType.NORMAL, Icons.Filled.Map),
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Public),
        LayerTypeItem(MapType.NAVI, Icons.Filled.Navigation),
        LayerTypeItem(MapType.BUS, Icons.Filled.BusAlert),
        LayerTypeItem(MapType.NAVI_NIGHT, Icons.Filled.Nightlight),
    )

    var isMapLoaded by remember { mutableStateOf(false) }
    var shouldTakeScreenshot by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val locations = remember {
        mutableStateListOf<LatLng>()
    }

    val current by locationSource.current.collectAsStateWithLifecycle()

    val markerState = rememberUpdatedMarkerState(
        position = LatLng(31.835367246976922, 117.09781923241997)
    ).apply {
//        showInfoWindow()
        println("markerState: $this")
    }

    val icon = remember {
        BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_point)
    }

    AMap(
        modifier = Modifier.statusBarsPadding(),
        cameraPositionState = cameraPositionState,
        properties = properties,
        locationSource = locationSource,
        uiSettings = uiSettings,
        mapColorScheme = mapColorScheme,
        onMapClick = {
            println("a onMapClick: $it")
            locations.add(it)
        },
        onPOIClick = {
            println("a onPOIClick: $it")
            locations.add(it.coordinate)
        },
        onMapLoaded = {
            isMapLoaded = true
        }
    ) {
        @OptIn(MapsComposeExperimentalApi::class)
        MapEffect(Unit) {
            // https://lbs.amap.com/api/android-sdk/guide/create-map/mylocation#s1
//            it.isMyLocationEnabled = true
            it.myLocationStyle = MyLocationStyle().apply {
//                myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.route_ic_my_location))
                showMyLocation(false)
//                myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATE)
//                strokeWidth(0f)
//                strokeColor(Color.TRANSPARENT)
//                radiusFillColor(Color.TRANSPARENT)
            }
            it.uiSettings.setLogoMarginRate(AMapOptions.LOGO_MARGIN_BOTTOM, 0.5f)
        }

        Current(current, cameraPositionState)

        Marker(
            state = rememberUpdatedMarkerState(defaultLocation),
            icon = BitmapDescriptorFactory.fromBitmap(context.getDrawable(R.drawable.route_ic_track_location)!!.merge(context.getDrawable(R.drawable.icon_bridge)!!)),
            onClick = {
                locations.add(it.position)
                true
            }
        )
        Marker(
            position = LatLng(31.835367246976922, 117.09781923241997),
            drawable = R.drawable.route_ic_point_start,
        )
        MarkerInfoWindowComposable(
            state = markerState,
            zIndex = 10f,
            anchor = Offset(0.5f, 0.5f),
            infoWindowAnchor = Offset(0.5f, 1f),
            onClick = {
                markerState.showInfoWindow()
                true
            },
            infoContent = {
                MarkerInfoContent()
            }
        ) {
            Image(
                painter = painterResource(id = R.drawable.route_ic_track_point),
                contentDescription = "Marker",
            )
        }

        LaunchedEffect(markerState) {
            markerState.showInfoWindow()
        }

//        MarkerInfoWindowComposable(
//            state = markerState,
//            anchor = Offset(0.5f, 0.5f),
//        ) {
//            Column(horizontalAlignment = Alignment.CenterHorizontally) {
//                MarkerInfoContent()
//                Image(
//                    painter = painterResource(id = R.drawable.route_ic_track_point),
//                    contentDescription = "Marker",
//                )
//            }
//        }
//        MarkerInfoWindow(
//            state = markerState,
//            icon = icon,
//            zIndex = 10f,
//            anchor = Offset(0.5f, 0.5f),
//            onClick = {
//                markerState.showInfoWindow()
//                true
//            },
//        ) {
//            MarkerInfoContent()
//        }

        Locations(locations, onClick = locations::add)

        Polyline(points = locations.map { it })
        Polyline(points = points)
//
        if (isMapLoaded) {
            Screenshot(shouldTakeScreenshot) { bitmap ->
                bitmap?.let {
                    scope.launch {
                        context.saveBitmapToFile(bitmap, "amap")
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
        Text("${cameraPositionState.position.zoom} - ${cameraPositionState.map?.scalePerPixel}")
        DisappearingScaleBar(
            cameraPositionState = cameraPositionState,
            modifier = Modifier
        )
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
            false
        }
    )
}

@Composable
private fun Current(location: LatLng?, cameraPositionState: CameraPositionState) {
    location ?: return
    Marker(
        position = location,
        drawable = R.drawable.route_ic_my_location,
    )
    LaunchedEffect(location) {
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, 15f))
    }
}

@Composable
private fun Locations(locations: List<LatLng>, onClick: (LatLng) -> Unit) {
    locations.forEachIndexed { index, location ->
        val drawable by remember {
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
                drawable = drawable,
//                onClick = onClick
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
