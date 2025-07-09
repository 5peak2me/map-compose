package com.github.speak2me.app.compose.map.demo.google.test

import com.google.android.gms.maps.model.LatLng

data class TrackPoint(
    val id: Int,
    val latLng: LatLng,
    val elevation: Double,
    val distance: Double = 0.0
)