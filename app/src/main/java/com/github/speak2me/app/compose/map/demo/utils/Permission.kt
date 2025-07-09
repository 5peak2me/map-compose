package com.github.speak2me.app.compose.map.demo.utils

import android.Manifest
import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

/**
 * 简化的权限管理 Hook
 * 返回权限状态和请求权限的函数
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun useLocationPermission(
    onPermissionGranted: () -> Unit = {},
    onPermissionDenied: () -> Unit = {}
): LocationPermissionState {
    val context = LocalContext.current
    val locationPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 权限状态变化监听
    LaunchedEffect(locationPermissionsState.allPermissionsGranted) {
        if (locationPermissionsState.allPermissionsGranted) {
            onPermissionGranted()
        }
    }

    // 权限被拒绝时的处理
    LaunchedEffect(locationPermissionsState.revokedPermissions) {
        if (locationPermissionsState.revokedPermissions.isNotEmpty()) {
            // 简化处理：直接显示提示信息
            Toast.makeText(context, "需要定位权限才能获取当前位置", Toast.LENGTH_LONG).show()
            onPermissionDenied()
        }
    }

    // 自动请求权限
    LaunchedEffect(Unit) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    return LocationPermissionState(
        isGranted = locationPermissionsState.allPermissionsGranted,
        shouldShowRationale = locationPermissionsState.revokedPermissions.isNotEmpty(),
        requestPermission = {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    )
}

/**
 * 定位权限状态
 */
data class LocationPermissionState(
    val isGranted: Boolean,
    val shouldShowRationale: Boolean,
    val requestPermission: () -> Unit
)