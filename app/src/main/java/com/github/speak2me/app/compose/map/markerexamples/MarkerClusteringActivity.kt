package com.github.speak2me.app.compose.map.markerexamples

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val TAG = MarkerClusteringActivity::class.simpleName

class MarkerClusteringActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AMapClustering()
        }
    }
}

@Composable
fun AMapClustering() {
//    val items = remember { mutableStateListOf<MyItem>() }
//    LaunchedEffect(Unit) {
//        for (i in 1..10) {
//            val position = LatLng(
//                singapore2.latitude + Random.nextFloat(),
//                singapore2.longitude + Random.nextFloat(),
//            )
//            items.add(MyItem(position, "Marker", "Snippet", 0f))
//        }
//    }
//    Box(
//        modifier = Modifier.fillMaxSize()
//            .systemBarsPadding()
//    ) {
//        AMapClustering(items = items)
//    }
}

//@Composable
//fun AMapClustering(items: List<MyItem>) {
//    var clusteringType by remember {
//        mutableStateOf(ClusteringType.Default)
//    }
//    AMap(
//        modifier = Modifier.fillMaxSize(),
//        cameraPositionState = rememberCameraPositionState {
//            position = CameraPosition.fromLatLngZoom(singapore, 6f)
//        }
//    ) {
//        when (clusteringType) {
//            ClusteringType.Default -> {
//                DefaultClustering(
//                    items = items,
//                )
//            }
//
//            ClusteringType.CustomUi -> {
//                CustomUiClustering(
//                    items = items,
//                )
//            }
//
//            ClusteringType.CustomRenderer -> {
//                CustomRendererClustering(
//                    items = items,
//                )
//            }
//        }
//
//        MarkerInfoWindow(
//            state = rememberUpdatedMarkerState(position = singapore),
//            onClick = {
//                Log.d(TAG, "Non-cluster marker clicked! $it")
//                true
//            }
//        )
//    }
//
//    ClusteringTypeControls(
//        onClusteringTypeClick = {
//            clusteringType = it
//        },
//    )
//}

//@OptIn(MapsComposeExperimentalApi::class)
//@Composable
//private fun DefaultClustering(items: List<MyItem>) {
//    Clustering(
//        items = items,
//        // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
//        onClusterClick = {
//            Log.d(TAG, "Cluster clicked! $it")
//            false
//        },
//        onClusterItemClick = {
//            Log.d(TAG, "Cluster item clicked! $it")
//            false
//        },
//        onClusterItemInfoWindowClick = {
//            Log.d(TAG, "Cluster item info window clicked! $it")
//        },
//        // Optional: Custom rendering for non-clustered items
//        clusterItemContent = null
//    )
//}

//@OptIn(MapsComposeExperimentalApi::class)
//@Composable
//private fun CustomUiClustering(items: List<MyItem>) {
//    Clustering(
//        items = items,
//        // Optional: Handle clicks on clusters, cluster items, and cluster item info windows
//        onClusterClick = {
//            Log.d(TAG, "Cluster clicked! $it")
//            false
//        },
//        onClusterItemClick = {
//            Log.d(TAG, "Cluster item clicked! $it")
//            false
//        },
//        onClusterItemInfoWindowClick = {
//            Log.d(TAG, "Cluster item info window clicked! $it")
//        },
//        // Optional: Custom rendering for clusters
//        clusterContent = { cluster ->
//            CircleContent(
//                modifier = Modifier.size(40.dp),
//                text = "%,d".format(cluster.size),
//                color = Color.Blue,
//            )
//        },
//        // Optional: Custom rendering for non-clustered items
//        clusterItemContent = null,
//        // Optional: Customization hook for clusterManager and renderer when they're ready
//        onClusterManager = { clusterManager ->
//            (clusterManager.renderer as DefaultClusterRenderer).minClusterSize = 2
//        },
//    )
//}

//@OptIn(MapsComposeExperimentalApi::class)
//@Composable
//fun CustomRendererClustering(items: List<MyItem>) {
//    val configuration = LocalConfiguration.current
//    val screenHeight = configuration.screenHeightDp.dp
//    val screenWidth = configuration.screenWidthDp.dp
//    val clusterManager = rememberClusterManager<MyItem>()
//
//    // Here the clusterManager is being customized with a NonHierarchicalViewBasedAlgorithm.
//    // This speeds up by a factor the rendering of items on the screen.
//    clusterManager?.setAlgorithm(
//        NonHierarchicalViewBasedAlgorithm(
//            screenWidth.value.toInt(),
//            screenHeight.value.toInt()
//        )
//    )
//    val renderer = rememberClusterRenderer(
//        clusterContent = { cluster ->
//            CircleContent(
//                modifier = Modifier.size(40.dp),
//                text = "%,d".format(cluster.size),
//                color = Color.Green,
//            )
//        },
//        clusterItemContent = {
//            CircleContent(
//                modifier = Modifier.size(20.dp),
//                text = "",
//                color = Color.Green,
//            )
//        },
//        clusterManager = clusterManager,
//    )
//
//    SideEffect {
//        clusterManager ?: return@SideEffect
//        clusterManager.setOnClusterClickListener {
//            Log.d(TAG, "Cluster clicked! $it")
//            false
//        }
//        clusterManager.setOnClusterItemClickListener {
//            Log.d(TAG, "Cluster item clicked! $it")
//            false
//        }
//        clusterManager.setOnClusterItemInfoWindowClickListener {
//            Log.d(TAG, "Cluster item info window clicked! $it")
//        }
//    }
//    SideEffect {
//        if (clusterManager?.renderer != renderer) {
//            clusterManager?.renderer = renderer ?: return@SideEffect
//        }
//    }
//
//    if (clusterManager != null) {
//        Clustering(
//            items = items,
//            clusterManager = clusterManager,
//        )
//    }
//}

@Composable
private fun CircleContent(
    color: Color,
    text: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier,
        shape = CircleShape,
        color = color,
        contentColor = Color.White,
        border = BorderStroke(1.dp, Color.White)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text,
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ClusteringTypeControls(
    onClusteringTypeClick: (ClusteringType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier
            .fillMaxWidth()
            .horizontalScroll(state = ScrollState(0)),
        horizontalArrangement = Arrangement.Start
    ) {
        ClusteringType.entries.forEach {
            MapButton(
                text = when (it) {
                    ClusteringType.Default -> "Default"
                    ClusteringType.CustomUi -> "Custom UI"
                    ClusteringType.CustomRenderer -> "Custom Renderer"
                },
                onClick = { onClusteringTypeClick(it) }
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

private enum class ClusteringType {
    Default,
    CustomUi,
    CustomRenderer,
}

//data class MyItem(
//    val itemPosition: LatLng,
//    val itemTitle: String,
//    val itemSnippet: String,
//    val itemZIndex: Float,
//) : ClusterItem {
//    override fun getPosition(): LatLng =
//        itemPosition
//
//    override fun getTitle(): String =
//        itemTitle
//
//    override fun getSnippet(): String =
//        itemSnippet
//
//    override fun getZIndex(): Float =
//        itemZIndex
//}
