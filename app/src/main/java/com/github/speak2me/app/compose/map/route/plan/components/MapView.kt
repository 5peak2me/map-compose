package com.github.speak2me.app.compose.map.route.plan.components

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.CoordinateConverter
import com.amap.api.maps.CoordinateConverter.CoordType
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.CameraPosition
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.LatLngBounds
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.PolylineOptions.LineCapType
import com.amap.api.maps.model.PolylineOptions.LineJoinType
import com.github.speak2me.app.compose.map.R
import com.github.speak2me.app.compose.map.SmartScaleBar
import com.github.speak2me.app.compose.map.route.plan.RoutePlan2UiState
import com.github.speak2me.app.compose.map.route.plan.data.model.Waypoint
import com.github.speak2me.app.compose.map.route.plan.ktx.isDarkMode
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.gcj02ToWgs84
import com.github.speak2me.app.compose.map.route.plan.utils.CoordinateTransform.wgs84ToGcj02
import com.github.speak2me.compose.map.amap.AMap
import com.github.speak2me.compose.map.amap.CameraPositionState
import com.github.speak2me.compose.map.amap.MapEffect
import com.github.speak2me.compose.map.amap.MapProperties
import com.github.speak2me.compose.map.amap.MapType
import com.github.speak2me.compose.map.amap.MapUiSettings
import com.github.speak2me.compose.map.amap.MapsComposeExperimentalApi
import com.github.speak2me.compose.map.amap.Marker
import com.github.speak2me.compose.map.amap.Polyline
import com.github.speak2me.compose.map.amap.ktx.snapshot
import com.github.speak2me.compose.map.amap.rememberCameraPositionState
import com.github.speak2me.compose.map.amap.rememberUpdatedMarkerState
import com.github.speak2me.compose.map.amap.widget.DisappearingScaleBar
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private suspend fun CameraPositionState.zoomTo(location: LatLng) {
    animate(CameraUpdateFactory.newLatLngZoom(location, 15f))
}

private suspend fun CameraPositionState.bearing(bearing: Float = 0f) {
    val cameraPosition = CameraPosition.builder(position).bearing(bearing).build()
    animate(CameraUpdateFactory.newCameraPosition(cameraPosition))
}

@Composable
fun MapView(
    modifier: Modifier = Modifier,
    uiState: RoutePlan2UiState,
    @DrawableRes selectedIcon: Int,
    onLocateClick: () -> Unit,
    onTypeClick: () -> Unit,
    onModeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onMapClick: (LatLng) -> Unit,
    onWaypointClick: (Waypoint) -> Unit,
    onScreenshotTaken: (Bitmap?) -> Unit,
) {
    val context = LocalContext.current

    val converter = CoordinateConverter(context)
    converter.from(CoordType.GPS)

    val scope = rememberCoroutineScope()

    var showCompass by remember { mutableStateOf(false) }
    var satellite by remember { mutableStateOf(false) }
    var shouldLocateToTracks by remember { mutableStateOf(false) }
    var shouldLocateToCurrent by remember { mutableStateOf(false) }
    var isSearchMarkerVisible by remember(uiState.searchLocation) { mutableStateOf(uiState.searchLocation != null) }

    // 记录地图容器的尺寸
    var mapWidth by remember { mutableIntStateOf(0) }
    var mapHeight by remember { mutableIntStateOf(0) }

    val cameraPositionState = rememberCameraPositionState()

    // 监听状态变化并通知父组件
    LaunchedEffect(cameraPositionState) {
        snapshotFlow { cameraPositionState.position.bearing }
            .collect { bearing ->
                showCompass = bearing != 0f
            }
    }

    // 监听当前位置变化，自动移动地图到当前位置
    LaunchedEffect(uiState.currentLocation, shouldLocateToCurrent) {
        uiState.currentLocation?.let { location ->
            scope.launch {
                cameraPositionState.zoomTo(location)
                shouldLocateToCurrent = false
            }
        }
    }

    // 监听搜索marker变化，自动定位到搜索位置
    LaunchedEffect(uiState.searchLocation) {
        uiState.searchLocation?.let {
            scope.launch {
                cameraPositionState.zoomTo(it.convert(converter))
            }
        }
    }

    // 监听轨迹居中请求
    LaunchedEffect(shouldLocateToTracks) {
        if (shouldLocateToTracks && uiState.trackpoints.isNotEmpty()) {
            scope.launch {
                val bounds = uiState.trackpoints
                    .map { it.convert(converter) }
                    .fold(LatLngBounds.Builder()) { builder, latLng ->
                        builder.include(latLng)
                    }
                    .build()
                // https://a.amap.com/lbs/static/unzip/Android_Map_Doc/3D/com/amap/api/maps/model/class-use/LatLngBounds.html
                // 设置地图视野以显示整个轨迹
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds,
                        (mapWidth * 0.8).roundToInt(),
                        mapHeight / 2,
                        100
                    ),
                )

                // 重置标志
                shouldLocateToTracks = false
            }
        }
    }

    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                compassEnabled = false,
                myLocationButtonEnabled = false,
                scaleControlsEnabled = true,
                tiltGesturesEnabled = false,
                zoomControlsEnabled = true,
            )
        )
    }

    AMap(
        modifier = modifier
            .fillMaxSize()
            .onSizeChanged { size ->
                mapWidth = size.width
                mapHeight = size.height
            },
        cameraPositionState = cameraPositionState,
        uiSettings = uiSettings,
        properties = MapProperties(
            mapType = if (satellite) MapType.SATELLITE else (if (context.isDarkMode) MapType.NIGHT else MapType.NORMAL)
        ),
        onMapClick = {
            val (wgsLat, wgsLon) = gcj02ToWgs84(it.latitude, it.longitude)
            onMapClick(LatLng(wgsLat, wgsLon))
        },
        onPOIClick = {
            val (wgsLat, wgsLon) = gcj02ToWgs84(it.coordinate.latitude, it.coordinate.longitude)
            onMapClick(LatLng(wgsLat, wgsLon))
        }
    ) {
        if (uiState.trackpoints.isNotEmpty()) {
            Tracks(points = uiState.trackpoints.map { it.convert(converter) })

            Points(points = uiState.route?.waypoints.orEmpty(), onPointClick = onWaypointClick)

            // 显示选中的轨迹点
            uiState.selectedTrackPoint?.let { selectedIndex ->
                uiState.trackpoints.getOrNull(selectedIndex)?.let { selectedPoint ->
                    Marker(
                        position = selectedPoint.convert(converter),
                        drawable = R.drawable.route_ic_track_location,
                        anchor = Offset(0.5f, 1f),
                        onClick = {
                            // 点击轨迹点时显示信息
                            onMapClick(selectedPoint.convert(converter))
                        }
                    )
                }
            }

            Screenshot(uiState, onScreenshotTaken)
        } else {
            // 显示起点和终点
            Locations(locations = uiState.locations.map { it.convert(converter) })
        }

        uiState.currentLocation?.let { location ->
            Marker(
                position = location,
                drawable = R.drawable.route_ic_my_location,
                onClick = {
                    val (wgsLat, wgsLon) = gcj02ToWgs84(location.latitude, location.longitude)
                    onMapClick(LatLng(wgsLat, wgsLon))
                }
            )
        }

        // show search marker
        uiState.searchLocation?.let {
            if (isSearchMarkerVisible) {
                Marker(
                    position = it.convert(converter),
                    drawable = R.drawable.route_ic_map_marker,
                    onClick = { marker ->
                        // 点击搜索标记时，先清除搜索标记，然后进行地图点击处理
                        onMapClick(it)
                        isSearchMarkerVisible = false
                    }
                )
            }
        }
    }

    // 地图控制组件
    MapTitle(
        start = {
//            DisappearingScaleBar(
//                modifier = Modifier.align(Alignment.CenterStart),
//                cameraPositionState = cameraPositionState
//            )
            SmartScaleBar(
                modifier = Modifier.align(Alignment.CenterStart),
                cameraPositionState = cameraPositionState,
            )
        },
        onClick = {}
    )

    MapPanel(
        showCompass = showCompass,
        selectedIcon = selectedIcon,
        onCompassClick = {
            scope.launch {
                cameraPositionState.bearing(0f)
            }
        },
        onLayerClick = {
            satellite = !satellite
        },
        onLocateClick = {
            // 定位按钮逻辑：有轨迹时回到轨迹起点，否则定位到当前位置
            if (uiState.hasTracks && uiState.locations.isNotEmpty()) {
                // 有轨迹时，直接设置内部状态触发轨迹居中
                shouldLocateToTracks = true
            } else if (uiState.currentLocation != null) {
                // 没有轨迹但有位置信息时，直接设置内部状态触发定位到当前位置
                shouldLocateToCurrent = true
            } else {
                // 没有位置信息时，调用外部处理进行定位
                onLocateClick()
            }
        },
        onTypeClick = onTypeClick,
        onModeClick = onModeClick,
        onSearchClick = onSearchClick,
    )
}

@Composable
private fun Locations(locations: List<LatLng>) {
    if (locations.isEmpty()) return
    Marker(
        position = locations.first(),
        drawable = R.drawable.route_ic_point_start,
    )
    if (locations.size <= 1) return
    Marker(
        position = locations.last(),
        drawable = R.drawable.route_ic_point_end,
    )
}

@Composable
private fun Marker(
    position: LatLng,
    @DrawableRes drawable: Int,
    anchor: Offset = Offset(0.5f, 0.5f),
    tag: String? = null,
    onClick: (Marker) -> Unit = {}
) {
    Marker(
        state = rememberUpdatedMarkerState(position = position),
        icon = BitmapDescriptorFactory.fromResource(drawable),
        anchor = anchor,
        tag = tag,
        onClick = { onClick(it); true },
    )
}

@Composable
private fun Polyline(positions: List<LatLng>, color: Color, width: Float) {
    Polyline(
        points = positions,
        color = color,
        width = width,
        lineCapType = LineCapType.LineCapRound,
        lineJoinType = LineJoinType.LineJoinRound,
    )
}

@Composable
private fun Tracks(points: List<LatLng>) {
    if (points.size >= 2) {
        Marker(
            position = points.first(),
            tag = "start",
            drawable = R.drawable.route_ic_point_start,
        )
        Polyline(positions = points, color = Color.White, width = 24f)
        Polyline(positions = points, color = Color(0xFFFF3366), width = 16f)
        Marker(
            position = points.last(),
            tag = "end",
            drawable = R.drawable.route_ic_point_end,
        )
    }
}

@Composable
private fun Points(points: List<Waypoint>, onPointClick: (Waypoint) -> Unit) {
    points.forEachIndexed { index, point ->
        val (gcjLat, gcjLon) = wgs84ToGcj02(point.latitude, point.longitude)
        Marker(
            position = LatLng(gcjLat, gcjLon),
            drawable = R.drawable.sport_route_marker_bg_black,
            onClick = {
                onPointClick(point)
            }
        )
    }
}

@Composable
private fun Screenshot(uiState: RoutePlan2UiState, onScreenshotTaken: (Bitmap?) -> Unit) {
    @OptIn(MapsComposeExperimentalApi::class)
    MapEffect(uiState) { map ->
        if (uiState.shouldTakeScreenshot) {
//            map.mapScreenMarkers.removeAll { it.`object` == null }
            map.mapScreenMarkers.filter { it.`object` == null }.forEach(Marker::remove)
            map.snapshot { bitmap ->
                onScreenshotTaken(bitmap)
            }
        }
    }
}

private fun LatLng.convert(converter: CoordinateConverter): LatLng {
    return converter.coord(this).convert()
}
