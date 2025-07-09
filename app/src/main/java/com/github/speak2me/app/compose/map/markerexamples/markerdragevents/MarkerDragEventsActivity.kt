package com.github.speak2me.app.compose.map.markerexamples.markerdragevents

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.defaultCameraPosition
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.app.compose.map.singapore
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import kotlinx.coroutines.flow.dropWhile

private val TAG = MarkerDragEventsActivity::class.simpleName

/**
 * Demonstrates how to reliably generate a sequence of Marker drag START-DRAG-END events as in the
 * original AMap Marker listener.
 */
class MarkerDragEventsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                AMapWithMarker(
                    modifier = Modifier.fillMaxSize()
                        .systemBarsPadding(),
                )
            }
        }
    }
}

@Composable
private fun AMapWithMarker(
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState { position = defaultCameraPosition }

    AMap(
        modifier = modifier,
        cameraPositionState = cameraPositionState,
    ) {
        DraggableMarker(
            onDragStart = { Log.i(TAG, "onDragStart") },
            onDragEnd = { Log.i(TAG, "onDragEnd") },
            onDrag = { position -> Log.i(TAG, "onDrag: $position") }
        )
    }
}

/**
 * A draggable AMap Marker.
 *
 * @param onDragStart called when marker dragging starts
 * @param onDrag called with an update for the marker's current position during dragging
 * @param onDragEnd called when marker dragging ends
 */
@Composable
private fun DraggableMarker(
    onDragStart: () -> Unit = {},
    onDrag: (LatLng) -> Unit = {},
    onDragEnd: () -> Unit = {}
) {
    val markerState = rememberUpdatedMarkerState(position = singapore)

    Marker(
        state = markerState,
        draggable = true
    )

    LaunchedEffect(Unit) {
        var inDrag = false
        var priorPosition: LatLng? = singapore

        snapshotFlow { markerState.isDragging to markerState.position }
            .dropWhile { (isDragging, position) ->
                !isDragging && position == priorPosition // ignore initial value
            }
            .collect { (isDragging, position) ->
                // Do not even bother to check isDragging state here:
                // it is possible to miss a sequence of states
                // where isDragging == true, then isDragging == false;
                // in this case we would only see a change in position.
                // (Hypothetically we could even miss a change in position
                // if the Marker ended up in its original position at the
                // end of the drag. But then nothing changed at all,
                // so we should be ok to ignore this case altogether.)
                if (!inDrag) {
                    inDrag = true
                    onDragStart()
                }

                if (position != priorPosition) {
                    onDrag(position)
                    priorPosition = position
                }

                if (!isDragging) {
                    inDrag = false
                    onDragEnd()
                }
            }
    }
}
