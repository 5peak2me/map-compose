package com.github.speak2me.app.compose.map.demo.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Assuming these are defined elsewhere or you'll create them
data class LayerTypeItem<T>(
    val type: T, // Or your own enum/class for layer types
    val icon: ImageVector,
    val label: String
)

@Composable
fun <T> LayerMenu(
    modifier: Modifier = Modifier,
    currentMapType: T,
    layerTypes: List<LayerTypeItem<T>>,
    onMapTypeChange: (T) -> Unit,
) {
    var isLayerMenuExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FloatingActionButton(
            onClick = { isLayerMenuExpanded = !isLayerMenuExpanded },
            shape = CircleShape
        ) {
            Icon(
                imageVector = Icons.Default.Layers,
                contentDescription = "Toggle Layers Menu"
            )
        }

        // Animated horizontal menu for layer types
        AnimatedVisibility(
            visible = isLayerMenuExpanded,
            enter = slideInHorizontally(initialOffsetX = { fullWidth -> -fullWidth }) + fadeIn(),
            exit = slideOutHorizontally(targetOffsetX = { fullWidth -> -fullWidth }) + fadeOut()
        ) {
            Row(
                modifier = Modifier
                    .padding(end = 8.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                layerTypes.forEach { layerItem ->
                    OutlinedButton(
                        onClick = {
                            onMapTypeChange(layerItem.type)
                            isLayerMenuExpanded = false // Optionally close menu on selection
                        },
                        shape = CircleShape, // Or other shape like RoundedCornerShape(8.dp)
                        colors = if (currentMapType == layerItem.type) {
                            ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer,
                                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            ButtonDefaults.outlinedButtonColors(containerColor = Color.LightGray)
                        }
                    ) {
                        Icon(imageVector = layerItem.icon, contentDescription = layerItem.label)
                        Spacer(Modifier.width(4.dp))
                        Text(layerItem.label)
                    }
                }
            }
        }
    }
}

@Composable
fun <T> MapPanel(
    currentMapType: T,
    layerTypes: List<LayerTypeItem<T>>,
    onThemeChange: () -> Unit,
    onMapTypeChange: (T) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(start = 8.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.Top),
    ) {
        FloatingActionButton(onClick = onThemeChange, shape = CircleShape) {
            Icon(
                imageVector = Icons.Filled.Face,
                contentDescription = null
            )
        }
        LayerMenu(
            currentMapType = currentMapType,
            layerTypes = layerTypes,
            onMapTypeChange = onMapTypeChange
        )
    }
}
