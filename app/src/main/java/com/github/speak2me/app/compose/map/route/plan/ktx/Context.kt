package com.github.speak2me.app.compose.map.route.plan.ktx

import android.content.Context
import android.content.res.Configuration

internal inline val Context.isDarkMode: Boolean
    get() = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
