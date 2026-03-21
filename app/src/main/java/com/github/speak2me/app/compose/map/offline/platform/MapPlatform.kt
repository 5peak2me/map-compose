package com.github.speak2me.app.compose.map.offline.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
)

data class GeoBounds(
    val southwest: GeoPoint,
    val northeast: GeoPoint,
)

data class GeoPolygon(
    val points: List<GeoPoint>,
)

sealed interface CameraUpdate {
    /**
     * 将相机移动到指定中心点与缩放级别。
     */
    data class CenterZoom(
        val center: GeoPoint,
        val zoom: Float,
    ) : CameraUpdate

    /**
     * 将相机移动到可完整展示指定地理范围的位置。
     * @param paddingPx 额外边距（像素）。
     */
    data class FitBounds(
        val bounds: GeoBounds,
        val paddingPx: Int = 0,
    ) : CameraUpdate

    /**
     * 将相机移动到可完整展示指定多边形区域的位置。
     * @param paddingPx 额外边距（像素）。
     */
    data class FitPolygon(
        val polygon: GeoPolygon,
        val paddingPx: Int = 0,
    ) : CameraUpdate
}

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
     * 应用相机更新命令。
     */
    fun move(update: CameraUpdate, animated: Boolean = false)

    /**
     * 立即移动地图相机到指定中心点与缩放级别（兼容）。
     */
    fun moveTo(center: GeoPoint, zoom: Float) {
        move(
            update = CameraUpdate.CenterZoom(center = center, zoom = zoom),
            animated = false
        )
    }

    /**
     * 立即移动地图相机以完整展示指定地理范围（兼容）。
     * @param paddingPx 额外边距（像素）。
     */
    fun moveTo(bounds: GeoBounds, paddingPx: Int = 0) {
        move(
            update = CameraUpdate.FitBounds(bounds = bounds, paddingPx = paddingPx),
            animated = false
        )
    }

    /**
     * 立即移动地图相机以完整展示指定多边形区域（兼容）。
     * @param paddingPx 额外边距（像素）。
     */
    fun moveTo(polygon: GeoPolygon, paddingPx: Int = 0) {
        move(
            update = CameraUpdate.FitPolygon(polygon = polygon, paddingPx = paddingPx),
            animated = false
        )
    }
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
        initialUpdate: CameraUpdate,
    ): MapController

    /**
     * 创建并记忆平台地图控制器（兼容：中心点 + 缩放级别）。
     */
    @Composable
    fun rememberController(
        initialCenter: GeoPoint,
        initialZoom: Float,
    ): MapController = rememberController(
        initialUpdate = CameraUpdate.CenterZoom(
            center = initialCenter,
            zoom = initialZoom
        )
    )

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
