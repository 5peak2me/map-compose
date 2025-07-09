package com.github.speak2me.app.compose.map.route.plan.data.model

import android.location.Location

/**
 * 轨迹点
 */
data class Trackpoint(
    val latitude: Double,
    val longitude: Double,
    val altitude: Double,
    val distance: Double = 0.0,
    val name: String = ""
)

/**
 * 位置点
 */
data class Waypoint(
    val index: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val altitude: Double = 0.0,
    val distance: Double = 0.0,
)

data class Route(
    val id: Long,
    val name: String, // 路线名称
    val distance: Double, // 总距离，单位：米
    val waypoints: List<Waypoint> = emptyList(), // 自定义位置点
    val tracks: List<Trackpoint> = emptyList(), // 生成的轨迹点
    val segments: List<WaySegment> = emptyList()
)

/**
 * 路线段
 */
data class WaySegment(
    // 对应的轨迹点的索引
    val index: Int,
    // 对应的位置点的名称
    val name: String,
    // 对应的上一个位置点（起点）到这个位置点的距离
    val distance: Double,
    // 对应的上一个位置点（起点）到这个位置点的累计上升
    val elevationGain: Double,
    // 对应的上一个位置点（起点）到这个位置点的累计下降
    val elevationLoss: Double,
    // 对应的上一个位置点（起点）到这个位置点的最高海拔
    val elevationMax: Double,
    // 对应的上一个位置点（起点）到这个位置点的最低海拔
    val elevationMin: Double,
)

fun Waypoint.toLocation(): Location {
    return com.github.speak2me.app.compose.map.route.plan.ktx.Location(latitude, latitude, altitude)
}

fun Trackpoint.toLocation(): Location {
    return com.github.speak2me.app.compose.map.route.plan.ktx.Location(latitude, latitude, altitude)
}
