package com.github.speak2me.app.compose.map.demo.google

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FireHydrantAlt
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.demo.components.MarkerInfoContent
import com.github.speak2me.app.compose.map.demo.utils.saveBitmapToFile
import com.github.speak2me.app.compose.map.demo.utils.useLocationPermission
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerInfoWindowComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.launch
import com.github.speak2me.app.compose.map.demo.utils.drawBitmapWithBackground
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.wgs84ToGcj02
import com.google.maps.android.compose.AdvancedMarker

class GoogleMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleMapScreen()
        }
    }

}

private val defaultLocation = LatLng(31.849193465179447, 117.12530114551642)

@Composable
private fun GoogleMapScreen() {
    var isMyLocationEnabled by remember { mutableStateOf(false) }

    useLocationPermission(
        onPermissionGranted = {
            isMyLocationEnabled = true
            println("permission granted")
        }
    )
    GoogleMapScreen(isMyLocationEnabled)
}

@SuppressLint("MissingPermission")
@Composable
private fun GoogleMapScreen(isMyLocationEnabled: Boolean) {

    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState()

    val locationSource = remember {
        GoogleMapLocationSource(context, cameraPositionState)
    }

    var properties by remember(isMyLocationEnabled) {
        mutableStateOf(
            MapProperties(isMyLocationEnabled = isMyLocationEnabled)
        )
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
//                compassEnabled = false,
                mapToolbarEnabled = false,
//                myLocationButtonEnabled = false,
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
        LayerTypeItem(MapType.NONE, Icons.Filled.HourglassEmpty),
        LayerTypeItem(MapType.NORMAL, Icons.Filled.Map),
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Public),
        LayerTypeItem(MapType.TERRAIN, Icons.Filled.Terrain),
        LayerTypeItem(MapType.HYBRID, Icons.Filled.FireHydrantAlt),
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

    GoogleMap(
        cameraPositionState = cameraPositionState,
        locationSource = locationSource,
        properties = properties,
        uiSettings = uiSettings,
        mapColorScheme = mapColorScheme,
        onMapClick = {
            println("g onMapClick: $it")
            locations.add(it)
        },
        onPOIClick = {
            println("g onPOIClick: ${it.latLng}, ${it.name}")
            locations.add(it.latLng)
        },
        onMapLoaded = {
            isMapLoaded = true
        }
    ) {
        @OptIn(MapsComposeExperimentalApi::class)
        MapEffect {
            it.isMyLocationEnabled = false
            it.setOnMarkerClickListener {
                println(it)
                false
            }
        }

        Current(current, cameraPositionState)

        Marker(
            position = defaultLocation,
            drawable = R.drawable.route_ic_track_location,
            onClick = {
                locations.add(it)
            }
        )
        Marker(
            position = LatLng(31.835367246976922, 117.09781923241997),
            drawable = R.drawable.route_ic_map_marker,
        )
        MarkerInfoWindowComposable(
            state = markerState,
            zIndex = 10f,
            anchor = Offset(0.5f, 0.5f),
            infoWindowAnchor = Offset(1f, 0f),
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

//        MarkerInfoWindow(
//            state = markerState,
//            icon = BitmapDescriptorFactory.fromResource(R.drawable.route_ic_track_point),
//            zIndex = 10f,
//            anchor = Offset(0.5f, 0.5f),
//            onClick = {
//                markerState.showInfoWindow()
//                true
//            },
//        ) {
//            MarkerInfoContent()
//        }

        Locations(locations)

        val pattern = listOf(
            Dash(30f),
            Gap(10f),
            Dot()
        )

        Polyline(points = locations.map { it }, pattern = pattern, geodesic = true)

        if (isMapLoaded) {
            Screenshot(shouldTakeScreenshot) { bitmap ->
                bitmap?.let {
                    scope.launch {
                        context.saveBitmapToFile(bitmap, "google")
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
        ScaleBar(cameraPositionState = cameraPositionState)
    }
}

@Composable
private fun Marker(position: LatLng, @DrawableRes drawable: Int, onClick: (LatLng) -> Boolean = { false }) {

    val context = LocalContext.current
    val icon = context.getDrawable(drawable)
    val bitmap = drawBitmapWithBackground(0x90ed7612.toInt(), icon?.toBitmap()!!)
    AdvancedMarker(
        state = rememberUpdatedMarkerState(position),
        icon = BitmapDescriptorFactory.fromBitmap(bitmap),
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
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(location, 14.5f))
    }
}

@Composable
private fun Locations(locations: List<LatLng>, onClick: (LatLng) -> Boolean = { true }) {
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
                onClick = onClick
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
