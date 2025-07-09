package com.github.speak2me.app.compose.map.markerexamples.markerscollection

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.ui.Modifier
import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.MarkerState
import com.github.speak2me.app.compose.map.defaultCameraPosition
import com.github.speak2me.app.compose.map.markerexamples.updatingnodragmarkerwithdatamodel.Marker
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap

/**
 * Simplistic app data model intended for persistent storage.
 *
 * This only stores [LocationData], for demonstration purposes, but could hold an entire app's data.
 */
private class DataModel {
    /**
     * Location data.
     */
    val locationDataMap = mutableStateMapOf<LocationKey, LocationData>()
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
 * Unique, stable key for location
 */
private class LocationKey

private typealias KeyedLocationData = Pair<LocationKey, LocationData>

/**
 * Demonstrates how to sync a data model with a changing collection of
 * location markers using keys.
 *
 * The user can add a location marker to the model by clicking the map and delete a location from
 * the model by clicking a marker.
 *
 * This example reuses the simple non-draggable Marker approach from the
 * `UpdatingNoDragMarkerWithDataModelActivity` example, which encapsulates
 * [MarkerState] to provide a cleaner API surface.
 */
class MarkersCollectionActivity : ComponentActivity() {
    private val dataModel = DataModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                Screen(
                    dataModel = dataModel,
                    modifier = Modifier.fillMaxSize()
                        .systemBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun Screen(
    dataModel: DataModel,
    modifier: Modifier = Modifier
) = AMapWithLocations(
    modifier = modifier,
    keyedLocationData = dataModel.locationDataMap.toList(),
    onAddLocation = { locationData ->
        dataModel.locationDataMap += LocationKey() to locationData
    },
    onDeleteLocation = { key ->
        dataModel.locationDataMap -= key
    }
)

/**
 * A AMap with locations represented by markers
 *
 * @param keyedLocationData model data for location markers with unique keys.
 * Uses a [Collection] type to keep it independent of our data model.
 * @param onAddLocation location addition events for updating data model
 * @param onDeleteLocation location deletion events for updating data model
 */
@Composable
private fun AMapWithLocations(
    keyedLocationData: Collection<KeyedLocationData>,
    modifier: Modifier = Modifier,
    onAddLocation: (LocationData) -> Unit,
    onDeleteLocation: (LocationKey) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState { position = defaultCameraPosition }

    AMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
        onMapClick = { position -> onAddLocation(LocationData(position)) }
    ) {
        Locations(
            keyedLocationData = keyedLocationData,
            onLocationClick = onDeleteLocation
        )
    }
}

/**
 * Renders locations on a AMap
 *
 * @param keyedLocationData model data for location markers with unique keys.
 * @param onLocationClick location click events
 */
@Composable
private fun Locations(
    keyedLocationData: Collection<KeyedLocationData>,
    onLocationClick: (LocationKey) -> Unit
) = keyedLocationData.forEach { (key, locationData) ->
    key(key) {
        Marker(
            position = locationData.position,
            onClick = {
                onLocationClick(key)
                true // consume click event to prevent camera move to marker
            }
        )
    }
}
