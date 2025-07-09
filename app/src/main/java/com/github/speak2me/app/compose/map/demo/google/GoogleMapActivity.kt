package com.github.speak2me.app.compose.map.demo.google

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FireHydrantAlt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MicNone
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Terrain
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.demo.components.LayerTypeItem
import com.github.speak2me.app.compose.map.demo.components.MapPanel
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState

class GoogleMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GoogleMapScreen()
        }
    }

}

private val defaultLocation = LatLng(31.847229, 117.122685)

@SuppressLint("MissingPermission")
@Composable
private fun GoogleMapScreen() {

    val context = LocalContext.current

    val cameraPositionState = rememberCameraPositionState()

    val myLocationSource = remember {
        GoogleMapLocationSource(context, cameraPositionState)
    }

    val locations = remember {
        mutableStateListOf<LatLng>()
    }

    var properties by remember {
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
//                compassEnabled = false,
                indoorLevelPickerEnabled = true,
                mapToolbarEnabled = false,
//                myLocationButtonEnabled = false,
                rotationGesturesEnabled = true,
                scrollGesturesEnabled = true,
                scrollGesturesEnabledDuringRotateOrZoom = true,
                tiltGesturesEnabled = true,
//                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
            )
        )
    }

    var mapColorScheme by remember {
        mutableStateOf(ComposeMapColorScheme.LIGHT)
    }

    // Define your layer types here
    val layerTypes = listOf(
        LayerTypeItem(MapType.NONE, Icons.Filled.MicNone, "NONE"),
        LayerTypeItem(MapType.NORMAL, Icons.Filled.Map, "Normal"),
        LayerTypeItem(MapType.SATELLITE, Icons.Filled.Public, "Satellite"),
        LayerTypeItem(MapType.TERRAIN, Icons.Filled.Terrain, "TERRAIN"),
        LayerTypeItem(MapType.HYBRID, Icons.Filled.FireHydrantAlt, "HYBRID"),
        // Add more layer types as needed, e.g., TERRAIN, HYBRID
        // LayerTypeItem(MapType.TERRAIN, Icons.Filled.Terrain, "Terrain"), // Example
    )

    GoogleMap(
        cameraPositionState = cameraPositionState,
        locationSource = myLocationSource,
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
        }
    ) {
        Marker(
            state = rememberUpdatedMarkerState("", defaultLocation),
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

        Polyline(points = locations.map {
//            val (gcjLat, gcjLon) = CoordinateTransform.gcj02ToWgs84(it.latitude, it.longitude)
//            LatLng(gcjLat, gcjLon)
            it
        }, geodesic = true)

//        @OptIn(MapsComposeExperimentalApi::class)
//        MapEffect {
//            it.snapshot {
//
//            }
//        }
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

@Composable
@Deprecated(
    message = "Use 'rememberUpdatedMarkerState' instead - It may be confusing to think " +
            "that the state is automatically updated as the position changes, " +
            "so it will be changed or removed.",
    replaceWith = ReplaceWith(
        expression = """
            val markerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
                MarkerState(position)
            }
        """
    )
)
public fun rememberUpdatedMarkerState(
    key: String? = null,
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = rememberSaveable(key = key, saver = MarkerState.Saver) {
    MarkerState(position)
}

/**
 * This function updates the state value according to the update of the input parameter,
 * like 'rememberUpdatedState'.
 *
 * This cannot be used to preserve state across configuration changes.
 */
@Composable
public fun rememberUpdatedMarkerState(
    position: LatLng = LatLng(0.0, 0.0)
): MarkerState = remember {
    MarkerState(position = position)
}.also { it.position = position }
