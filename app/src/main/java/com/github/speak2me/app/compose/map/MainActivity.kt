package com.github.speak2me.app.compose.map

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.github.speak2me.app.compose.map.markerexamples.AdvancedMarkersActivity
import com.github.speak2me.app.compose.map.markerexamples.MarkerClusteringActivity
import com.github.speak2me.app.compose.map.markerexamples.draggablemarkerscollectionwithpolygon.DraggableMarkersCollectionWithPolygonActivity
import com.github.speak2me.app.compose.map.markerexamples.markerdragevents.MarkerDragEventsActivity
import com.github.speak2me.app.compose.map.markerexamples.markerscollection.MarkersCollectionActivity
import com.github.speak2me.app.compose.map.markerexamples.syncingdraggablemarkerwithdatamodel.SyncingDraggableMarkerWithDataModelActivity
import com.github.speak2me.app.compose.map.markerexamples.updatingnodragmarkerwithdatamodel.UpdatingNoDragMarkerWithDataModelActivity
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MapComposeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                        .systemBarsPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val context = LocalContext.current
                    Column(
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.padding(10.dp))
                        Text(
                            text = getString(R.string.main_activity_title),
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.padding(10.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, BasicMapActivity::class.java))
                            }) {
                            Text(getString(R.string.basic_map_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, AdvancedMarkersActivity::class.java))
                            }) {
                            Text(getString(R.string.advanced_markers))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        MarkerClusteringActivity::class.java
                                    )
                                )
                            }) {
                            Text(getString(R.string.marker_clustering_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        MapInColumnActivity::class.java
                                    )
                                )
                            }) {
                            Text(getString(R.string.map_in_column_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        MapsInLazyColumnActivity::class.java
                                    )
                                )
                            }) {
                            Text(getString(R.string.maps_in_lazy_column_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        LocationTrackingActivity::class.java
                                    )
                                )
                            }) {
                            Text(getString(R.string.location_tracking_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, ScaleBarActivity::class.java))
                            }) {
                            Text(getString(R.string.scale_bar_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
//                                context.startActivity(Intent(context, StreetViewActivity::class.java))
                            }) {
                            Text(getString(R.string.street_view))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, CustomControlsActivity::class.java))
                            }) {
                            Text(getString(R.string.custom_location_button))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, AccessibilityActivity::class.java))
                            }) {
                            Text(getString(R.string.accessibility_button))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, RecompositionActivity::class.java))
                            }) {
                            Text(getString(R.string.recomposition_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, MarkerDragEventsActivity::class.java))
                            }) {
                            Text(getString(R.string.marker_drag_events_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, MarkersCollectionActivity::class.java))
                            }) {
                            Text(getString(R.string.markers_collection_activity))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, SyncingDraggableMarkerWithDataModelActivity::class.java))
                            }) {
                            Text(getString(R.string.syncing_draggable_marker_with_data_model))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, UpdatingNoDragMarkerWithDataModelActivity::class.java))
                            }) {
                            Text(getString(R.string.updating_non_draggable_marker_with_data_model))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, DraggableMarkersCollectionWithPolygonActivity::class.java))
                            }) {
                            Text(getString(R.string.draggable_markers_collection_with_polygon))
                        }
                        Spacer(modifier = Modifier.padding(5.dp))
                        Button(
                            onClick = {
                                context.startActivity(Intent(context, FragmentDemoActivity::class.java))
                            }) {
                            Text(getString(R.string.fragment_demo_activity))
                        }
                    }
                }
            }
        }
    }
}
