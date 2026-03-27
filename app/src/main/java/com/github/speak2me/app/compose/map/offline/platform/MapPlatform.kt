package com.github.speak2me.app.compose.map.offline.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    override fun toString(): String = "($longitude, $latitude)"
}

data class GeoBounds(
    val southwest: GeoPoint,
    val northeast: GeoPoint,
) {
    override fun toString(): String = "Bounds(sw=$southwest, ne=$northeast)"
}

data class GeoPolygon(
    val points: List<GeoPoint>,
)

data class CameraPosition(
    val center: GeoPoint,
    val zoom: Float,
    val tilt: Float = 0f,
    val bearing: Float = 0f,
)

data class CameraSnapshot(
    val position: CameraPosition,
    val isMoving: Boolean,
    val visibleBounds: GeoBounds,
)

@Stable
interface CameraUpdate {
    /**
     * 将相机移动到指定中心点与缩放级别。
     */
    data class Center(
        val center: GeoPoint,
        val zoom: Float,
    ) : CameraUpdate

    /**
     * 将相机移动到可完整展示指定地理范围的位置，且该区域在视口中居中展示。
     * @param padding 额外边距（像素）。
     */
    data class FitBounds(
        val bounds: GeoBounds,
        val padding: Int = 0,
    ) : CameraUpdate
}

data class MapCameraConstraint(
    val minZoom: Float? = null,
    val maxZoom: Float? = null,
)

data class MapUiConfig(
    val compassEnabled: Boolean = false,
    val indoorLevelPickerEnabled: Boolean = false,
    val isMyLocationEnabled: Boolean = true,
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

@Stable
interface MapCameraState {
    /**
     * 当前地图相机位置状态。
     */
    val position: CameraPosition

    /**
     * 当前地图是否处于相机移动中。
     */
    val isMoving: Boolean

    /**
     * 当前地图缩放级别。
     */
    val zoom: Float
        get() = position.zoom

    /**
     * 当前地图中心点坐标。
     */
    val center: GeoPoint
        get() = position.center

    /**
     * 当前可用的屏幕投影能力，地图未就绪时可能为 null。
     */
    val projection: MapScreenProjection?

    /**
     * 当前可见区域地理边界，地图未就绪时可能为 null。
     */
    val visibleBounds: GeoBounds?

    /**
     * 监听相机状态快照流，地图未就绪时不应发出数据。
     * 新平台（如 Google 地图）通常只需实现 position/isMoving/visibleBounds 即可复用该默认实现。
     */
    fun cameraSnapshotFlow(): Flow<CameraSnapshot> = snapshotFlow {
        val bounds = visibleBounds ?: return@snapshotFlow null
        CameraSnapshot(
            position = position,
            isMoving = isMoving,
            visibleBounds = bounds
        )
    }
        .filterNotNull()
        .distinctUntilChanged()

    /**
     * 立即应用相机更新命令。
     */
    fun move(update: CameraUpdate)

    /**
     * 以动画方式应用相机更新命令。
     * @param durationMs 动画时长（毫秒）。默认使用底层 SDK 的默认时长。
     */
    suspend fun animate(update: CameraUpdate, durationMs: Int = Int.MAX_VALUE)

    /**
     * 立即移动地图相机到指定中心点与缩放级别（兼容）。
     */
    fun moveTo(center: GeoPoint, zoom: Float) {
        move(update = CameraUpdate.Center(center = center, zoom = zoom))
    }

}

@Stable
interface MapPlatform {
    /**
     * 计算两个地理坐标点之间的实际距离（米）。
     * 实现应委托到底层地图 SDK 的官方距离计算 API。
     */
    fun distanceMeters(from: GeoPoint, to: GeoPoint): Float

    /**
     * 创建并记忆平台地图相机状态。
     */
    @Composable
    fun rememberCameraState(
        initialUpdate: CameraUpdate,
    ): MapCameraState

    /**
     * 创建并记忆平台地图相机状态（兼容：中心点 + 缩放级别）。
     */
    @Composable
    fun rememberCameraState(
        initialCenter: GeoPoint,
        initialZoom: Float,
    ): MapCameraState = rememberCameraState(
        initialUpdate = CameraUpdate.Center(
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
        cameraState: MapCameraState,
        cameraConstraint: MapCameraConstraint,
        uiConfig: MapUiConfig,
    )
}

private fun GeoPolygon.toGeoBoundsOrNull(): GeoBounds? {
    if (points.isEmpty()) return null
    var minLat = Double.POSITIVE_INFINITY
    var maxLat = Double.NEGATIVE_INFINITY
    var minLng = Double.POSITIVE_INFINITY
    var maxLng = Double.NEGATIVE_INFINITY
    points.forEach { point ->
        minLat = minOf(minLat, point.latitude)
        maxLat = maxOf(maxLat, point.latitude)
        minLng = minOf(minLng, point.longitude)
        maxLng = maxOf(maxLng, point.longitude)
    }
    return GeoBounds(
        southwest = GeoPoint(latitude = minLat, longitude = minLng),
        northeast = GeoPoint(latitude = maxLat, longitude = maxLng)
    )
}
