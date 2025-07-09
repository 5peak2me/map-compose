package com.github.speak2me.app.compose.map.markerexamples.updatingnodragmarkerwithdatamodel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.defaultCameraPosition
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.app.compose.map.singapore
import com.github.speak2me.app.compose.map.singapore2
import com.github.speak2me.app.compose.map.singapore3
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * Simplistic app data model intended for persistent storage.
 *
 * This only stores [LocationData], for demonstration purposes, but could hold an entire app's data.
 */
private class DataModel {
    /**
     * Location data
     */
    var locationData by mutableStateOf(LocationData(singapore))
}

/**
 * Data type representing a location.
 *
 * This only stores location position, for demonstration purposes,
 * but could hold other data related to the location.
 */
@Immutable
private data class LocationData(val position: LatLng)

/**
 * Demonstrates how to easily initialize and update position for a non-draggable
 * Marker from a data model.
 */
class UpdatingNoDragMarkerWithDataModelActivity : ComponentActivity() {
    private val dataModel = DataModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            // Simulate remote updates to data model
            while (true) {
                delay(3_000)

                val newPosition = when (Random.nextInt(3)) {
                    0 -> singapore
                    1 -> singapore2
                    2 -> singapore3
                    else -> singapore
                }

                dataModel.locationData = LocationData(newPosition)
            }
        }

        setContent {
            MapComposeTheme {
                AMapWithSimpleMarker(
                    locationData = dataModel.locationData,
                    modifier = Modifier.fillMaxSize()
                        .systemBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun AMapWithSimpleMarker(
    locationData: LocationData,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState { position = defaultCameraPosition }

    AMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        Marker(position = locationData.position)
    }
}

/**
 * Standard API pattern for a non-draggable Marker.
 *
 * The caller does not have to deal with MarkerState,
 * and can update Marker [position] via recomposition.
 */
@Composable
fun Marker(
    position: LatLng,
    onClick: () -> Boolean = { false },
) {
    val markerState = rememberUpdatedMarkerState(position = position)

    Marker(
        state = markerState,
        onClick = { onClick() }
    )
}