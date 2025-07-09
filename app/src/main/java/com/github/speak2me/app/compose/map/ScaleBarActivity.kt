package com.github.speak2me.app.compose.map

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.DarkGray
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.ui.theme.MapComposeTheme
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.widget.DisappearingScaleBar
import com.github.speak2me.compose.map.amap.widget.ScaleBar

class ScaleBarActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isMapLoaded by remember { mutableStateOf(false) }

            // To control and observe the map camera
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }

            val scaleBackground = MaterialTheme.colorScheme.background.copy(alpha = 0.4f)
            val scaleBorderStroke = BorderStroke(width = 1.dp, DarkGray.copy(alpha = 0.2f))

            Box(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding(),
            ) {
                AMap(
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = {
                        isMapLoaded = true
                    }
                )

                Box(
                    modifier = Modifier
                        .padding(top = 5.dp, start = 5.dp)
                        .align(Alignment.TopStart)
                        .background(
                            scaleBackground,
                            shape = MaterialTheme.shapes.medium
                        )
                        .border(
                            scaleBorderStroke,
                            shape = MaterialTheme.shapes.medium
                        ),
                ) {
                    DisappearingScaleBar(
                        modifier = Modifier.padding(end = 4.dp),
                        cameraPositionState = cameraPositionState
                    )
                }

                Box(
                    modifier = Modifier
                        .padding(top = 5.dp, end = 5.dp)
                        .align(Alignment.TopEnd)
                        .background(
                            scaleBackground,
                            shape = MaterialTheme.shapes.medium,
                        )
                        .border(
                            scaleBorderStroke,
                            shape = MaterialTheme.shapes.medium
                        ),
                    ) {
                    ScaleBar(
                        modifier = Modifier.padding(end = 4.dp),
                        cameraPositionState = cameraPositionState
                    )

                }
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

@Preview
@Composable
fun PreviewScaleBar() {
    val cameraPositionState = remember {
        CameraPositionState(
            position = CameraPosition(
                LatLng(48.137154, 11.576124), // Example coordinates: Munich, Germany
                12f,
                0f,
                0f
            )
        )
    }

    MapComposeTheme {
        ScaleBar(
            modifier = Modifier.padding(end = 4.dp),
            cameraPositionState = cameraPositionState
        )
    }
}