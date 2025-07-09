package com.github.speak2me.app.compose.map

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.amap.api.maps.model.Marker
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import kotlin.random.Random

private const val TAG = "RecompositionActivity"

/**
 * This is a sample activity showcasing how the recomposition works. The location is changed
 * every time we click on the button, and the marker gets updated (removed and added in a new
 * location)
 */
class RecompositionActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }
            Box(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding(),
            ) {
                MapComposeTheme {
                    AMapView(
                        modifier = Modifier.matchParentSize(),
                        cameraPositionState = cameraPositionState
                    )
                }
            }
        }
    }

    @Composable
    fun AMapView(
        modifier: Modifier = Modifier,
        cameraPositionState: CameraPositionState = rememberCameraPositionState(),
        content: @Composable () -> Unit = {},
    ) {
        val markerState = rememberUpdatedMarkerState(position = singapore)

        val uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
        val mapProperties by remember {
            mutableStateOf(MapProperties(mapType = MapType.NORMAL))
        }

        val mapVisible by remember { mutableStateOf(true) }
        if (mapVisible) {
            AMap(
                modifier = modifier,
                cameraPositionState = cameraPositionState,
                properties = mapProperties,
                uiSettings = uiSettings,
                onPOIClick = {
                    Log.d(TAG, "POI clicked: ${it.name}")
                }
            ) {
                val markerClick: (Marker) -> Boolean = {
                    Log.d(TAG, "${it.title} was clicked")
                    cameraPositionState.projection?.let { projection ->
                        Log.d(TAG, "The current projection is: $projection")
                    }
                    false
                }

                Marker(
                    state = markerState,
                    title = "Marker in Singapore",
                    onClick = markerClick
                )

                content()
            }
            Column {
                Button(onClick = {
                    val randomValue = Random.nextInt(3)
                    markerState.position = when (randomValue) {
                        0 -> singapore
                        1 -> singapore2
                        2 -> singapore3
                        else -> singapore
                    }
                }) {
                    Text("Change Location")
                }
            }
        }
    }
}