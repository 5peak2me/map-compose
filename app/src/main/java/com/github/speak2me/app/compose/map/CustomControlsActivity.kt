package com.github.speak2me.app.compose.map

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.amap.api.maps.CameraUpdateFactory
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import kotlinx.coroutines.launch


class CustomControlsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var isMapLoaded by remember { mutableStateOf(false) }
            val coroutineScope = rememberCoroutineScope()
            // This needs to be manually deactivated to avoid having a custom and the native
            // location button
            val uiSettings by remember {
                mutableStateOf(
                    MapUiSettings(
                        myLocationButtonEnabled = false,
                        zoomGesturesEnabled = false
                    )
                )
            }
            // Observing and controlling the camera's state can be done with a CameraPositionState
            val cameraPositionState = rememberCameraPositionState {
                position = defaultCameraPosition
            }

            Box(
                modifier = Modifier.fillMaxSize()
                    .systemBarsPadding(),
            ) {
                AMap (
                    modifier = Modifier.matchParentSize(),
                    cameraPositionState = cameraPositionState,
                    onMapLoaded = {
                        isMapLoaded = true
                    },
                    uiSettings = uiSettings,
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
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MapButton(
                        "This is a custom location button",
                        onClick = {
                            Toast.makeText(
                                this@CustomControlsActivity,
                                "Click on my location",
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                    MapButton(
                        "+",
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomIn())
                            }
                        })
                    MapButton(
                        "-",
                        onClick = {
                            coroutineScope.launch {
                                cameraPositionState.animate(CameraUpdateFactory.zoomOut())
                            }
                        })
                }
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


}
