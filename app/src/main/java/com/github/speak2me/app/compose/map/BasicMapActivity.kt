package com.github.speak2me.app.compose.map

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.Circle
import com.github.speak2me.compose.map.amap.ComposeMapColorScheme
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.MarkerComposable
import com.github.speak2me.compose.map.amap.MarkerInfoWindowComposable
import com.github.speak2me.compose.map.amap.MarkerInfoWindowContent
import com.github.speak2me.compose.map.amap.MarkerState
import com.github.speak2me.compose.map.amap.Polygon
import com.github.speak2me.compose.map.amap.Polyline
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import kotlinx.coroutines.launch

private const val TAG = "BasicMapActivity"

val singapore = LatLng(31.820586, 117.227239)
val singapore2 = LatLng(31.40, 117.77)
val singapore3 = LatLng(31.45, 117.77)
val singapore4 = LatLng(31.50, 117.77)
val singapore5 = LatLng(31.3418, 117.8461)
val singapore6 = LatLng(31.3430, 117.8844)
val singapore7 = LatLng(31.3430, 117.9116)
val singapore8 = LatLng(31.3300, 117.8624)
val singapore9 = LatLng(31.3200, 117.8541)
val singapore10 = LatLng(31.3200, 117.8765)

val defaultCameraPosition = CameraPosition.fromLatLngZoom(singapore, 11f)

//val styleSpan = StyleSpan(
//    StrokeStyle.gradientBuilder(
//        Color.Red.toArgb(),
//        Color.Green.toArgb(),
//    ).build(),
//)

class BasicMapActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isMapLoaded by remember { mutableStateOf(true) }
            // Observing and controlling the camera's state can be done with a CameraPositionState
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
            ) {
                AMapView(
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = {
                        println("isMapLoaded")
                        isMapLoaded = true
                    },
                )
                if (!isMapLoaded) {
                    AnimatedVisibility(
                        modifier = Modifier
                            .matchParentSize(),
                        visible = !isMapLoaded,
                        enter = EnterTransition.None,
                        exit = fadeOut()
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.background)
                                .wrapContentSize()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AMapView(
    modifier: Modifier = Modifier,
    cameraPositionState: CameraPositionState = rememberCameraPositionState(),
    onMapLoaded: () -> Unit = {},
    mapColorScheme: ComposeMapColorScheme = ComposeMapColorScheme.LIGHT,
    content: @Composable () -> Unit = {},
) {
    val singaporeState = rememberUpdatedMarkerState(position = singapore)
    val singapore2State = rememberUpdatedMarkerState(position = singapore2)
    val singapore3State = rememberUpdatedMarkerState(position = singapore3)
    val singapore4State = rememberUpdatedMarkerState(position = singapore4)
    val singapore5State = rememberUpdatedMarkerState(position = singapore5)

    var circleCenter by remember { mutableStateOf(singapore) }
    if (!singaporeState.isDragging) {
        circleCenter = singaporeState.position
    }

    val converter = CoordinateConverter(LocalContext.current)

    val polylinePoints = remember {
        listOf(
            converter.coord(LatLng(31.831915, 117.088863)).convert(),
            converter.coord(LatLng(31.831934, 117.093565)).convert()
        )
    }
//    val polylinePoints = remember { listOf(singapore, singapore5) }
    val polylineSpanPoints = remember { listOf(singapore, singapore6, singapore7) }
//    val styleSpanList = remember { listOf(styleSpan) }

    val polygonPoints = remember { listOf(singapore8, singapore9, singapore10) }

    var uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
    var shouldAnimateZoom by remember { mutableStateOf(true) }
    var ticker by remember { mutableIntStateOf(0) }
    var mapProperties by remember {
        mutableStateOf(MapProperties(mapType = MapType.NORMAL))
    }
    var mapVisible by remember { mutableStateOf(true) }

    var darkMode by remember { mutableStateOf(mapColorScheme) }

    if (mapVisible) {
        AMap(
            modifier = modifier,
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = onMapLoaded,
            onPOIClick = {
                Log.d(TAG, "POI clicked: ${it.name}")
            },
            mapColorScheme = darkMode
        ) {
            // Drawing on the map is accomplished with a child-based API
            val markerClick: (Marker) -> Boolean = {
                Log.d(TAG, "${it.title} was clicked")
                cameraPositionState.projection?.let { projection ->
                    Log.d(TAG, "The current projection is: $projection")
                }
                false
            }
            MarkerInfoWindowContent(
                state = singaporeState,
                title = "Zoom in has been tapped $ticker times.",
                onClick = markerClick,
                draggable = true,
            ) {
                Text(it.title ?: "Title", color = Color.Red)
            }
            MarkerInfoWindowContent(
                state = singapore2State,
                title = "Marker with custom info window.\nZoom in has been tapped $ticker times.",
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE),
                onClick = markerClick,
            ) {
                Text(it.title ?: "Title", color = Color.Blue)
            }
            Marker(
                state = singapore3State,
                title = "Marker in Singapore",
                onClick = markerClick
            )
            MarkerComposable(
                title = "Marker Composable",
                keys = arrayOf("singapore4"),
                state = singapore4State,
                onClick = markerClick,
            ) {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Red),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Compose Marker",
                        textAlign = TextAlign.Center,
                    )
                }
            }
            MarkerInfoWindowComposable(
                keys = arrayOf("singapore5"),
                state = singapore5State,
                onClick = markerClick,
                title = "Marker with custom Composable info window",
                infoContent = {
                    Text(it.title ?: "Title", color = Color.Blue)
                }
            ) {
                Box(
                    modifier = Modifier
                        .width(88.dp)
                        .height(36.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Red),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "Compose MarkerInfoWindow",
                        textAlign = TextAlign.Center,
                    )
                }
            }

            Circle(
                center = circleCenter,
                fillColor = MaterialTheme.colorScheme.secondary,
                strokeColor = MaterialTheme.colorScheme.secondaryContainer,
                radius = 1000.0,
            )

            Polyline(
                points = polylinePoints,
                tag = "Polyline A",
            )

            Polyline(
                points = polylineSpanPoints,
//                spans = styleSpanList,
                tag = "Polyline B",
            )

            Polygon(
                points = polygonPoints,
                fillColor = Color.Black.copy(alpha = 0.5f)
            )

            content()
        }
    }
    Column {
        MapTypeControls(onMapTypeClick = {
            Log.d("AMap", "Selected map type $it")
            mapProperties = mapProperties.copy(mapType = it)
        })
        Row {
            MapButton(
                text = "Reset Map",
                onClick = {
                    mapProperties = mapProperties.copy(mapType = MapType.NORMAL)
                    cameraPositionState.position = defaultCameraPosition
                    singaporeState.position = singapore
                    singaporeState.hideInfoWindow()
                }
            )
            MapButton(
                text = "Toggle Map",
                onClick = { mapVisible = !mapVisible },
                modifier = Modifier
                    .testTag("toggleMapVisibility")
            )
            MapButton(
                text = "Toggle Dark Mode",
                onClick = {
                    darkMode =
                        if (darkMode == ComposeMapColorScheme.DARK)
                            ComposeMapColorScheme.LIGHT
                        else
                            ComposeMapColorScheme.DARK
                },
                modifier = Modifier
                    .testTag("toggleDarkMode")
            )
        }
        val coroutineScope = rememberCoroutineScope()
        ZoomControls(
            shouldAnimateZoom,
            uiSettings.zoomControlsEnabled,
            onZoomOut = {
                if (shouldAnimateZoom) {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                    }
                } else {
                    cameraPositionState.move(CameraUpdateFactory.zoomOut())
                }
            },
            onZoomIn = {
                if (shouldAnimateZoom) {
                    coroutineScope.launch {
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                    }
                } else {
                    cameraPositionState.move(CameraUpdateFactory.zoomIn())
                }
                ticker++
            },
            onCameraAnimationCheckedChange = {
                shouldAnimateZoom = it
            },
            onZoomControlsCheckedChange = {
                uiSettings = uiSettings.copy(zoomControlsEnabled = it)
            }
        )
        DebugView(cameraPositionState, singaporeState)
    }
}

@Composable
private fun MapTypeControls(
    onMapTypeClick: (MapType) -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .horizontalScroll(state = ScrollState(0)),
        horizontalArrangement = Arrangement.Center
    ) {
        MapType.entries.forEach {
            MapTypeButton(type = it) { onMapTypeClick(it) }
        }
    }
}

@Composable
private fun MapTypeButton(type: MapType, onClick: () -> Unit) =
    MapButton(text = type.toString(), onClick = onClick)

@Composable
private fun ZoomControls(
    isCameraAnimationChecked: Boolean,
    isZoomControlsEnabledChecked: Boolean,
    onZoomOut: () -> Unit,
    onZoomIn: () -> Unit,
    onCameraAnimationCheckedChange: (Boolean) -> Unit,
    onZoomControlsCheckedChange: (Boolean) -> Unit,
) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
        MapButton("-", onClick = { onZoomOut() })
        MapButton("+", onClick = { onZoomIn() })
        Column(verticalArrangement = Arrangement.Center) {
            Text(text = "Camera Animations On?")
            Switch(
                isCameraAnimationChecked,
                onCheckedChange = onCameraAnimationCheckedChange,
                modifier = Modifier.testTag("cameraAnimations"),
            )
            Text(text = "Zoom Controls On?")
            Switch(
                isZoomControlsEnabledChecked,
                onCheckedChange = onZoomControlsCheckedChange
            )
        }
    }
}

@Composable
private fun MapButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        modifier = modifier.padding(4.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onPrimary,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        onClick = onClick
    ) {
        Text(text = text, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun DebugView(
    cameraPositionState: CameraPositionState,
    markerState: MarkerState,
) {
    Column(
        Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {
        val moving =
            if (cameraPositionState.isMoving) "moving" else "not moving"
        Text(text = "Camera is $moving")
        Text(text = "Camera position is ${cameraPositionState.position}")
        Spacer(modifier = Modifier.height(4.dp))
        val dragging =
            if (markerState.isDragging) "dragging" else "not dragging"
        Text(text = "Marker is $dragging")
        Text(text = "Marker position is ${markerState.position}")
    }
}


@Preview
@Composable
fun AMapViewPreview() {
    MapComposeTheme {
        AMapView(Modifier.fillMaxSize())
    }
}
