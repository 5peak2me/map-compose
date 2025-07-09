package com.github.speak2me.app.compose.map.demo.google.test

data class MapElevationUiState(
    val trackPoints: List<TrackPoint> = emptyList(),
    val selectedPointIndex: Int? = null,
    val hasMarker: Boolean = false,
    val chartDragPosition: Float = 0f,
    val isLoading: Boolean = false,
    val error: String? = null
)