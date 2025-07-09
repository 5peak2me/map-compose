package com.github.speak2me.app.compose.map.demo.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings

object LocationPermissionHelper {

    /**
     * 跳转到定位权限设置页面
     * 根据Android版本选择最合适的跳转方式
     */
    fun openLocationSettings(context: Context) {
        when {
            // Android 10+ (API 29+) - 可以直接跳转到定位权限页面
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                openLocationPermissionSettings(context)
            }
            // Android 10以下 - 跳转到系统定位设置
            else -> {
                openAppPermissionSettings(context)
            }
        }
    }

    /**
     * Android 10+ 跳转到系统的定位权限页面
     */
    private fun openLocationPermissionSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // 如果无法打开定位设置，则跳转到应用设置
            openAppPermissionSettings(context)
        }
    }

    /**
     * 跳转到当前应用的权限设置页面
     */
    private fun openAppPermissionSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            openSystemLocationSettings(context)
        }
    }

    /**
     * 跳转到系统定位设置页面
     */
    private fun openSystemLocationSettings(context: Context) {
        try {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            // 最后的fallback - 跳转到系统设置
            val settingsIntent = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(settingsIntent)
        }
    }

}