package com.github.speak2me.app.compose.map.demo.google

import android.content.Context
import com.google.android.gms.maps.MapsInitializer

internal object GMapInitializer {
    fun initialize(context: Context) {
        MapsInitializer.initialize(context, MapsInitializer.Renderer.LEGACY) {

        }
    }
}
