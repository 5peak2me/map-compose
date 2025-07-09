package com.github.speak2me.app.compose.map.route.plan.data.repository

import android.location.Location
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.route.plan.components.RouteMode
import com.github.speak2me.app.compose.map.route.plan.components.SportType
import com.github.speak2me.app.compose.map.route.plan.data.source.RoutePlanRemoteDataSource
import com.github.speak2me.app.compose.map.route.plan.data.source.RoutePlanRemoteDataSourceImpl
import com.github.speak2me.app.compose.map.route.plan.data.model.Route
import com.github.speak2me.app.compose.map.route.plan.data.model.Trackpoint
import com.github.speak2me.app.compose.map.route.plan.data.model.Waypoint
import com.github.speak2me.app.compose.map.route.plan.data.model.WaySegment
import kotlin.math.abs
import kotlin.random.Random

/**
 * 路线数据仓库接口
 * 负责路线相关的数据操作，包括轨迹生成、距离计算等
 */
interface RouteRepository {

    /**
     * 路线规划操作
     * @param currentRoute 当前路线
     * @param locations 所有点击的位置点列表
     * @param sportType 运动类型
     * @param editMode 编辑模式
     * @return 更新后的路线
     */
    suspend fun onRoutePlanning(currentRoute: Route?, locations: List<LatLng>, sportType: SportType, editMode: RouteMode): Route

    /**
     * 交换起终点操作
     * @param currentRoute 当前路线
     * @return 更新后的路线，如果路点不足2个则返回原路线
     */
    fun swapStartEnd(currentRoute: Route): Route?

    /**
     * 反转路线操作
     * @param currentRoute 当前路线
     * @return 更新后的路线，如果路点不足2个则返回原路线
     */
    fun reverseRoute(currentRoute: Route): Route?

    /**
     * 保存路线
     * @param route 要保存的路线
     * @return 保存结果，包含成功状态和消息
     */
    fun saveRoute(route: Route): SaveRouteResult

    /**
     * 更新位置点
     * @param currentRoute 当前路线
     * @param waypoint 更新后的位置点
     * @return 更新后的路线，如果索引无效则返回null
     */
    fun onUpdWaypoint(currentRoute: Route, waypoint: Waypoint): Route?

    /**
     * 添加位置点
     * @param currentRoute 当前路线
     * @param waypoint 要添加的位置点
     * @return 更新后的路线
     */
    fun onAddWaypoint(currentRoute: Route, waypoint: Waypoint): Route

    /**
     * 删除位置点
     * @param currentRoute 当前路线
     * @param index 要删除的位置点索引
     * @return 更新后的路线，如果索引无效则返回null
     */
    fun onDelWaypoint(currentRoute: Route, index: Int): Route?
}

/**
 * 路线数据仓库实现类
 */
class RouteRepositoryImpl(
    private val remoteDataSource: RoutePlanRemoteDataSource = RoutePlanRemoteDataSourceImpl(),
) : RouteRepository {

    override suspend fun onRoutePlanning(currentRoute: Route?, locations: List<LatLng>, sportType: SportType, editMode: RouteMode): Route {

        println("=== RouteRepository.onRoutePlanning 开始 ===")
        println("sportType=${sportType.profile}, editMode=$editMode")
        println("locations数量: ${locations.size}")
        println("locations: ${locations.map { "${it.latitude},${it.longitude}" }}")

        val currentTracks = currentRoute?.tracks ?: emptyList()
        println("现有轨迹点数量: ${currentTracks.size}")

        // 使用locations列表来规划路径
        if (locations.size >= 2) {
            println("调用远程数据源进行路线规划...")
            val routePlanResult = remoteDataSource.routePlan(
                sportType.profile,
                locations.map { listOf(it.longitude, it.latitude) })
            
            println("远程数据源返回结果: $routePlanResult")
            
            val newRoutePoints = routePlanResult
                ?.routeCoordinates
                ?.mapNotNull { coordinate ->
                    when {
                        coordinate.size >= 3 -> {
                            val trackpoint = Trackpoint(
                                longitude = coordinate[0],
                                latitude = coordinate[1],
                                altitude = coordinate[2]
                            )
                            println("创建轨迹点(3坐标): ${trackpoint.latitude},${trackpoint.longitude}")
                            trackpoint
                        }

                        coordinate.size == 2 -> {
                            val trackpoint = Trackpoint(
                                longitude = coordinate[0],
                                latitude = coordinate[1],
                                altitude = 0.0
                            )
                            println("创建轨迹点(2坐标): ${trackpoint.latitude},${trackpoint.longitude}")
                            trackpoint
                        }

                        else -> {
                            println("跳过无效坐标: $coordinate")
                            null
                        }
                    }
                }
                ?.takeIf { it.isNotEmpty() }
            
            println("解析后的轨迹点数量: ${newRoutePoints?.size ?: 0}")
            
            newRoutePoints?.let { points ->
                // 如果是连续规划（有现有轨迹），需要重新计算距离
                if (currentTracks.isNotEmpty()) {
                    println("连续规划：现有轨迹点数量=${currentTracks.size}，新轨迹点数量=${points.size}")
                    
                    // 获取现有轨迹的累计距离作为新轨迹的起始距离
                    val baseDistance = currentTracks.lastOrNull()?.distance ?: 0.0
                    println("基础距离: $baseDistance")
                    
                    // 重新计算新轨迹点的距离
                    val adjustedNewPoints = mutableListOf<Trackpoint>()
                    var cumulativeDistance = baseDistance
                    
                    for (i in points.indices) {
                        val point = points[i]
                        if (i == 0) {
                            // 第一个点使用基础距离
                            val adjustedPoint = point.copy(distance = baseDistance)
                            adjustedNewPoints.add(adjustedPoint)
                            println("调整第${i+1}个点距离: $baseDistance")
                        } else {
                            // 计算与前一个点的距离
                            val prevPoint = points[i - 1]
                            val segmentDistance = calculateDistance(
                                LatLng(prevPoint.latitude, prevPoint.longitude),
                                LatLng(point.latitude, point.longitude)
                            )
                            cumulativeDistance += segmentDistance
                            val adjustedPoint = point.copy(distance = cumulativeDistance)
                            adjustedNewPoints.add(adjustedPoint)
                            println("调整第${i+1}个点距离: $cumulativeDistance (段距离: $segmentDistance)")
                        }
                    }
                    
                    // 连续规划：保留现有轨迹，追加新轨迹点
                    val newTracks = currentTracks.toMutableList()
                    newTracks.addAll(adjustedNewPoints)
                    println("连续规划完成：总轨迹点数量=${newTracks.size}")
                    
                    // 计算总距离
                    val totalDistance = newTracks.lastOrNull()?.distance ?: 0.0
                    println("总距离: $totalDistance 米")

                    return Route(
                        id = currentRoute?.id ?: System.currentTimeMillis(),
                        name = currentRoute?.name ?: "路线${System.currentTimeMillis()}",
                        distance = totalDistance,
                        waypoints = currentRoute?.waypoints ?: emptyList(),
                        tracks = newTracks,
                        segments = currentRoute?.segments ?: emptyList()
                    )
                } else {
                    // 首次规划，直接使用新轨迹点
                    println("首次规划：新轨迹点数量=${points.size}")
                    
                    // 计算总距离
                    val totalDistance = points.lastOrNull()?.distance ?: 0.0
                    println("总距离: $totalDistance 米")

                    return Route(
                        id = currentRoute?.id ?: System.currentTimeMillis(),
                        name = currentRoute?.name ?: "路线${System.currentTimeMillis()}",
                        distance = totalDistance,
                        waypoints = currentRoute?.waypoints ?: emptyList(),
                        tracks = points,
                        segments = currentRoute?.segments ?: emptyList()
                    )
                }
            }
            
            println("警告：没有生成轨迹点，可能是远程数据源返回空结果")
        } else {
            println("警告：locations数量不足，需要至少2个点")
        }

        // 如果没有生成新的轨迹点，返回原路线或空路线
        println("返回原路线或空路线")
        return currentRoute ?: Route(
            id = System.currentTimeMillis(),
            name = "路线${System.currentTimeMillis()}",
            distance = 0.0,
            tracks = emptyList()
        )
    }

    override fun swapStartEnd(currentRoute: Route): Route? {
        // 轨迹起止点互换：将整个轨迹反转
        val newWaypoints = currentRoute.waypoints.reversed().toMutableList()

        // 反转轨迹点，并重新计算距离
        val newTracks = mutableListOf<Trackpoint>()
        val reversedTracks = currentRoute.tracks.reversed()

        var cumulativeDistance = 0.0
        for (i in reversedTracks.indices) {
            val track = reversedTracks[i]
            if (i == 0) {
                // 第一个点距离为0
                newTracks.add(track.copy(distance = 0.0))
            } else {
                // 计算与前一个点的距离
                val prevTrack = reversedTracks[i - 1]
                val distance = calculateDistance(
                    LatLng(prevTrack.latitude, prevTrack.longitude),
                    LatLng(track.latitude, track.longitude)
                )
                cumulativeDistance += distance
                newTracks.add(track.copy(distance = cumulativeDistance))
            }
        }

        // 重新生成路线段，基于反转后的轨迹
        val newSegments = mutableListOf<WaySegment>()
        for (i in 0 until newWaypoints.lastIndex) {
            val start = newWaypoints[i]
            val end = newWaypoints[i + 1]

            // 找到对应的轨迹段（简化匹配逻辑）
            val startTrackIndex = newTracks.indexOfFirst {
                abs(it.latitude - start.latitude) < 0.0001 &&
                        abs(it.longitude - start.longitude) < 0.0001
            }
            val endTrackIndex = newTracks.indexOfFirst {
                abs(it.latitude - end.latitude) < 0.0001 &&
                        abs(it.longitude - end.longitude) < 0.0001
            }

            if (startTrackIndex != -1 && endTrackIndex != -1) {
                val segmentTracks = if (startTrackIndex <= endTrackIndex) {
                    newTracks.subList(startTrackIndex, endTrackIndex + 1)
                } else {
                    newTracks.subList(endTrackIndex, startTrackIndex + 1)
                }

                val segmentDistance = segmentTracks.lastOrNull()?.distance?.minus(
                    segmentTracks.firstOrNull()?.distance ?: 0.0
                ) ?: 0.0
                val elevationStats = calculateElevationStats(segmentTracks)

                newSegments.add(
                    WaySegment(
                        index = i,
                        name = "段${i + 1}",
                        distance = segmentDistance,
                        elevationGain = elevationStats.elevationGain,
                        elevationLoss = elevationStats.elevationLoss,
                        elevationMax = elevationStats.elevationMax,
                        elevationMin = elevationStats.elevationMin
                    )
                )
            }
        }

        // 计算总距离
        val totalDistance = newTracks.lastOrNull()?.distance ?: 0.0

        return Route(
            id = currentRoute.id,
            name = currentRoute.name,
            distance = totalDistance,
            waypoints = newWaypoints,
            tracks = newTracks,
            segments = newSegments
        )
    }

    override fun reverseRoute(currentRoute: Route): Route? {
        // 创建往返路线：A→B→C→D→C→B→A
        val originalWaypoints = currentRoute.waypoints
        val returnWaypoints = originalWaypoints.dropLast(1).reversed() // 去掉最后一个点，然后反转

        // 组合原始路点和返回路点
        val roundTripWaypoints = originalWaypoints + returnWaypoints

        val originalTracks = currentRoute.tracks
        val returnTracks = originalTracks.dropLast(1).reversed()

        // 组合原始轨迹和返回轨迹
        val roundTripTracks = originalTracks + returnTracks

        return Route(
            id = currentRoute.id,
            name = currentRoute.name,
            distance = currentRoute.distance,
            waypoints = roundTripWaypoints,
            tracks = roundTripTracks,
        )
    }

    /**
     * 计算两点间距离（米）
     */
    fun calculateDistance(from: LatLng, to: LatLng): Double {
        val results = FloatArray(1)
        Location.distanceBetween(
            from.latitude, from.longitude,
            to.latitude, to.longitude,
            results
        )
        return results[0].toDouble()
    }

    /**
     * 生成两点间的轨迹点
     * 包含中间点和海拔数据，模拟真实路线
     */
    private fun generateTrackpoints(start: LatLng, end: LatLng): List<Trackpoint> {
        val points = mutableListOf<Trackpoint>()
        var totalDistance = 0.0

        // 计算两点间距离
        val directDistance = calculateDistance(start, end)

        // 生成随机海拔变化，模拟真实地形
        val startAltitude = 100.0 + (Random.nextDouble() * 200) // 100-300米
        val endAltitude = 100.0 + (Random.nextDouble() * 200) // 100-300米

        // Add start point
        points.add(Trackpoint(start.latitude, start.longitude, startAltitude, 0.0, ""))

        // Add intermediate points to make the route curved with realistic elevation
        val steps = 8 // 增加中间点数量
        var previousPoint = start
        var previousAltitude = startAltitude

        for (i in 1 until steps) {
            val fraction = i.toFloat() / steps

            // 生成曲线路径
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction

            // 添加随机偏移，模拟真实路径
            val offset = 0.0003 * (i % 3 - 1) // 更自然的偏移
            val currentPoint = LatLng(lat + offset, lng + offset)

            // 计算距离
            val distance = calculateDistance(previousPoint, currentPoint)
            totalDistance += distance

            // 生成海拔变化，模拟起伏地形
            val altitudeChange = (Random.nextDouble() - 0.5) * 20 // -10到+10米的变化
            val currentAltitude = previousAltitude + altitudeChange

            points.add(
                Trackpoint(
                    lat + offset,
                    lng + offset,
                    currentAltitude.coerceAtLeast(50.0), // 最低50米
                    totalDistance,
                    ""
                )
            )

            previousPoint = currentPoint
            previousAltitude = currentAltitude
        }

        // Add end point
        val finalDistance = calculateDistance(previousPoint, end)
        totalDistance += finalDistance
        points.add(Trackpoint(end.latitude, end.longitude, endAltitude, totalDistance, "0.0"))

        return points
    }

    /**
     * 生成直线轨迹点（适用于多个点）
     */
    private fun generateStraightLineTrackpointsForMultiplePoints(locations: List<LatLng>): List<Trackpoint> {
        val points = mutableListOf<Trackpoint>()
        var totalDistance = 0.0

        for (i in locations.indices) {
            val location = locations[i]
            if (i > 0) {
                val prevLocation = locations[i - 1]
                val distance = calculateDistance(prevLocation, location)
                totalDistance += distance
            }

            // 生成随机海拔（模拟真实地形）
            val altitude = 100.0 + Random.nextDouble() * 200.0 // 100-300米
            points.add(
                Trackpoint(
                    location.latitude,
                    location.longitude,
                    altitude,
                    totalDistance,
                    ""
                )
            )
        }
        return points
    }

    /**
     * 生成复杂曲线轨迹点（适用于多个点）
     */
    private fun generateTrackpointsForMultiplePoints(locations: List<LatLng>): List<Trackpoint> {
        val points = mutableListOf<Trackpoint>()
        var totalDistance = 0.0

        for (i in 0 until locations.lastIndex) {
            val start = locations[i]
            val end = locations[i + 1]
            val routePoints = generateTrackpoints(start, end)

            // 调整距离，使其累积
            val adjustedPoints = routePoints.map { trackpoint ->
                trackpoint.copy(distance = trackpoint.distance + totalDistance)
            }
            points.addAll(adjustedPoints)

            totalDistance += routePoints.lastOrNull()?.distance ?: 0.0
        }

        // 添加最后一个点
        if (locations.isNotEmpty()) {
            val lastLocation = locations.last()
            val altitude = 100.0 + Random.nextDouble() * 200.0 // 100-300米
            points.add(
                Trackpoint(
                    lastLocation.latitude,
                    lastLocation.longitude,
                    altitude,
                    totalDistance,
                    ""
                )
            )
        }

        return points
    }

    /**
     * 计算轨迹点的海拔统计信息
     */
    private fun calculateElevationStats(trackpoints: List<Trackpoint>): ElevationStats {
        if (trackpoints.isEmpty()) {
            return ElevationStats(0.0, 0.0, 0.0, 0.0)
        }

        var elevationGain = 0.0
        var elevationLoss = 0.0
        var elevationMax = trackpoints.first().altitude
        var elevationMin = trackpoints.first().altitude

        for (i in 1 until trackpoints.size) {
            val current = trackpoints[i]
            val previous = trackpoints[i - 1]
            val elevationChange = current.altitude - previous.altitude

            if (elevationChange > 0) {
                elevationGain += elevationChange
            } else {
                elevationLoss += abs(elevationChange)
            }

            elevationMax = maxOf(elevationMax, current.altitude)
            elevationMin = minOf(elevationMin, current.altitude)
        }

        return ElevationStats(elevationGain, elevationLoss, elevationMax, elevationMin)
    }

    override fun saveRoute(route: Route): SaveRouteResult {
        return try {
            // 这里可以实现保存到本地存储或云端的功能
            // 目前只是打印保存信息
            println("保存路线: ${route.name}")
            println("- 总距离: ${(route.distance / 1000).format(2)} km")
            println("- 位置点数量: ${route.waypoints.size}")
            println("- 轨迹点数量: ${route.tracks.size}")
            println("- 路线段数量: ${route.segments.size}")

            // 可以在这里添加实际的保存逻辑
            // 例如：保存到 SharedPreferences、数据库或云端

            SaveRouteResult(
                isSuccess = true,
                message = "路线保存成功！",
                savedRouteId = route.id
            )
        } catch (e: Exception) {
            SaveRouteResult(
                isSuccess = false,
                message = "保存失败: ${e.message}"
            )
        }
    }

    private fun Double.format(digits: Int) = "%.${digits}f".format(this)

    override fun onUpdWaypoint(currentRoute: Route, waypoint: Waypoint): Route? {
        val index = waypoint.index
        if (index < 0) {
            return null
        }

        val newWaypoints = currentRoute.waypoints.toMutableList()
        newWaypoints[index] = waypoint

        return currentRoute.copy(waypoints = newWaypoints)
    }

    override fun onAddWaypoint(currentRoute: Route, waypoint: Waypoint): Route {
        val newWaypoints = currentRoute.waypoints.toMutableList()

        // 如果指定了索引，在指定位置插入；否则添加到末尾
        val insertIndex = newWaypoints.size

        newWaypoints.add(insertIndex, waypoint)

        return currentRoute.copy(waypoints = newWaypoints)
    }

    override fun onDelWaypoint(currentRoute: Route, index: Int): Route? {
        if (index < 0) {
            return null
        }

        val newWaypoints = currentRoute.waypoints.toMutableList()
        newWaypoints.removeIf { it.index == index }

        return currentRoute.copy(waypoints = newWaypoints)
    }

}

/**
 * 海拔统计数据类
 */
data class ElevationStats(
    val elevationGain: Double,
    val elevationLoss: Double,
    val elevationMax: Double,
    val elevationMin: Double,
)

/**
 * 保存路线结果数据类
 */
data class SaveRouteResult(
    val isSuccess: Boolean,
    val message: String,
    val savedRouteId: Long? = null,
)
