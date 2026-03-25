package com.github.speak2me.app.compose.map.offline

import androidx.compose.ui.geometry.Size
import com.github.speak2me.app.compose.map.offline.platform.GeoPoint

internal const val MIN_WIDTH_METERS = 16_000f
internal const val MAX_WIDTH_METERS = 200_000f
internal const val DEFAULT_MAX_DISTANCE_METERS = 400_000f
internal const val INITIAL_MARGIN_RATIO = 0.1f
internal const val INIT_ZOOM_EPSILON = 0.0005f

internal val defaultCenter = GeoPoint(31.846594, 117.125279)

typealias FrameGroundSizeMeters = Size
