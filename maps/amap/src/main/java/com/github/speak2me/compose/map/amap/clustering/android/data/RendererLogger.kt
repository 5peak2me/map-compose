package com.github.speak2me.compose.map.amap.clustering.android.data

import android.util.Log

/**
 * Simple logging utility for geospatial renderers and managers.
 */
public object RendererLogger {
    private var isEnabled = false

    @JvmStatic
    public fun d(
        tag: String,
        msg: String,
    ) {
        if (isEnabled) {
            Log.d(tag, msg)
        }
    }

    @JvmStatic
    public fun e(
        tag: String,
        msg: String,
    ) {
        if (isEnabled) {
            Log.e(tag, msg)
        }
    }

    @JvmStatic
    public fun setEnabled(enabled: Boolean) {
        isEnabled = enabled
    }
}
