package com.github.speak2me.app.compose.map.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class MapCameraConstraint(
    val minZoom: Float? = null,
    val maxZoom: Float? = null,
)

data class MapUiConfig(
    val compassEnabled: Boolean = false,
    val indoorLevelPickerEnabled: Boolean = false,
    val mapToolbarEnabled: Boolean = false,
    val myLocationButtonEnabled: Boolean = false,
    val rotationGesturesEnabled: Boolean = false,
    val scaleControlsEnabled: Boolean = false,
    val tiltGesturesEnabled: Boolean = false,
    val zoomControlsEnabled: Boolean = false,
)

interface MapScreenProjection {
    /**
     * 将屏幕坐标转换为地理坐标。
     */
    fun fromScreenLocation(x: Int, y: Int): GeoPoint?
}

interface MapController {
    /**
     * 当前地图缩放级别。
     */
    val zoom: Float

    /**
     * 当前地图中心点坐标。
     */
    val center: GeoPoint

    /**
     * 当前可用的屏幕投影能力，地图未就绪时可能为 null。
     */
    val projection: MapScreenProjection?

    /**
     * 立即移动地图相机到指定中心点与缩放级别。
     */
    fun moveTo(center: GeoPoint, zoom: Float)
}

interface MapPlatform {
    /**
     * 计算两个地理坐标点之间的实际距离（米）。
     * 实现应委托到底层地图 SDK 的官方距离计算 API。
     */
    fun distanceMeters(from: GeoPoint, to: GeoPoint): Float

    /**
     * 创建并记忆平台地图控制器。
     */
    @Composable
    fun rememberController(
        initialCenter: GeoPoint,
        initialZoom: Float,
    ): MapController

    /**
     * 渲染平台地图视图。
     */
    @Composable
    fun MapView(
        modifier: Modifier,
        controller: MapController,
        cameraConstraint: MapCameraConstraint,
        uiConfig: MapUiConfig,
    )
}
