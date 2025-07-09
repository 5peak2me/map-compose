package com.github.speak2me.app.compose.map.route.plan.ktx

fun Location(lat: Double = 0.0, lng: Double = 0.0, alt: Double = 0.0) =
    android.location.Location("internal-provider").apply {
        latitude = lat
        longitude = lng
        altitude = alt
    }
