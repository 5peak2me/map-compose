package com.github.speak2me.app.compose.map

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.amap.api.maps.model.Marker
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState

private const val TAG = "AccessibilityActivity"


class AccessibilityActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val singaporeState = rememberUpdatedMarkerState(position = singapore)
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }
            val uiSettings by remember { mutableStateOf(MapUiSettings(compassEnabled = false)) }
            val mapProperties by remember {
                mutableStateOf(MapProperties(mapType = MapType.NORMAL))
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding(),
            ) {
                AMap (
                    // mergeDescendants will remove accessibility from the entire map and content inside.
                    mergeDescendants = true,
                    // alternatively, contentDescription will deactivate it for the maps, but not markers.
                    contentDescription = "",
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
                        // contentDescription overrides title for TalkBack
                        contentDescription = "Description of the marker",
                        state = singaporeState,
                        title = "Marker in Singapore",
                        onClick = markerClick
                    )
                }
            }
        }
    }
}

