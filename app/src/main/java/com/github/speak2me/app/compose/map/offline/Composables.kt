package com.github.speak2me.app.compose.map.offline

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize

@Composable
internal fun rememberContainerSize(
    width: Dp,
    height: Dp,
    density: Density,
): IntSize = remember(width, height, density) {
    with(density) {
        IntSize(width.roundToPx(), height.roundToPx())
    }
}

@Composable
internal fun rememberAspectRatio(containerSize: IntSize): Float = remember(containerSize) {
    if (containerSize.width <= 0) 1f
    else containerSize.height.toFloat() / containerSize.width.toFloat()
}
