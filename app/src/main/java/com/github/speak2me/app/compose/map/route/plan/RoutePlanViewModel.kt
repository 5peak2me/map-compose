package com.github.speak2me.app.compose.map.route.plan

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.location.Location
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Environment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.amap.api.maps.model.LatLng
import com.github.speak2me.app.compose.map.route.plan.components.RouteMode
import com.github.speak2me.app.compose.map.route.plan.components.OverviewState
import com.github.speak2me.app.compose.map.route.plan.components.SportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.github.speak2me.app.compose.map.route.plan.data.model.Route
import com.github.speak2me.app.compose.map.route.plan.data.model.Waypoint
import com.github.speak2me.app.compose.map.route.plan.data.repository.RouteRepository
import com.github.speak2me.app.compose.map.route.plan.data.repository.RouteRepositoryImpl
import com.github.speak2me.app.compose.map.route.plan.data.model.Trackpoint

import com.github.speak2me.app.compose.map.route.plan.memento.MementoManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.Job
import java.io.File
import java.io.FileOutputStream
import java.lang.Math.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.time.Duration.Companion.milliseconds

data class RoutePlan2UiState(
    val startPoint: LatLng? = null, // 起点
    val endPoint: LatLng? = null,   // 终点
    val route: Route? = null,

    val isLoading: Boolean = false,

    val canUndo: Boolean = false,
    val undoDescription: String? = null,
    val canRedo: Boolean = false,
    val redoDescription: String? = null,

    val currentLocation: LatLng? = null,
    val searchLocation: LatLng? = null,
    val isLocationPermissionGranted: Boolean = false,
    val error: String? = null,

    val selectedTrackPoint: Int? = null, // 新增：当前选中的轨迹点索引
    val overviewState: OverviewState = OverviewState.Overview1, // 新增：图表状态
    val selectedWaypoint: Waypoint? = null,
    val isReverseRouteUsed: Boolean = false, // 新增：原路返回是否已被使用
    val selectedRouteMode: RouteMode = RouteMode.ROUTE_PLANNING, // 新增：当前选中的路线模式
    val selectedSportType: SportType? = null, // 新增：当前选中的运动类型
    val shouldTakeScreenshot: Boolean = false, // 新增：是否应该进行截图
) {
    // 获取自定义位置点
    val waypoints: List<LatLng>
        get() = route?.waypoints?.map { LatLng(it.latitude, it.longitude) } ?: emptyList()

    // 获取轨迹点
    val trackpoints: List<LatLng>
        get() = route?.tracks?.map { LatLng(it.latitude, it.longitude) } ?: emptyList()

    val hasTracks: Boolean
        get() = route?.tracks?.isNotEmpty() == true

    // 获取起止点列表（用于向后兼容）
    val locations: List<LatLng>
        get() = listOfNotNull(startPoint, endPoint)

    override fun toString(): String {
        return "RoutePlan2UiState(" +
//                "isLoading=$isLoading, " +
                "route=${route.hashCode()}, " +
                "canUndo=$canUndo, " +
                "undoDescription=$undoDescription, " +
                "canRedo=$canRedo, " +
                "redoDescription=$redoDescription, " +
//                "currentLocation=$currentLocation, " +
//                "isLocationPermissionGranted=$isLocationPermissionGranted, " +
//                "error=$error, " +
                ")"
    }
}

class RoutePlan2ViewModel @JvmOverloads constructor(
    private val application: Application,
    private val routeRepository: RouteRepository = RouteRepositoryImpl()
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(RoutePlan2UiState())
    val uiState: StateFlow<RoutePlan2UiState> = _uiState.asStateFlow()

    private val mementoManager = MementoManager<RoutePlan2UiState>()
    
    // 添加防抖机制
    private var routePlanningJob: Job? = null

    init {
        // 监听备忘录状态变化
        viewModelScope.launch {
            mementoManager.state.collect { undoRedoState ->
                _uiState.update { currentState ->
                    currentState.copy(
                        canUndo = undoRedoState.canUndo,
                        undoDescription = undoRedoState.undoDescription,
                        canRedo = undoRedoState.canRedo,
                        redoDescription = undoRedoState.redoDescription
                    )
                }
            }
        }
    }

    /**
     * 保存当前状态到备忘录 - 使用简化的 API
     */
    private fun saveState() {
        val state = _uiState.value
        // 只需要传递状态，操作类型和描述通过扩展函数自动获取
        mementoManager.save(state)
    }

    fun onMapClick(latLng: LatLng) {
        // 检查是否点击了搜索标记
        val currentState = _uiState.value

        // 检查是否点击了轨迹点或位置点
        val editMode = currentState.selectedRouteMode
        val sportType = currentState.selectedSportType ?: SportType.OUTDOOR_RUNNING

//        if (currentState.hasRoute) {
//            // 首先检查是否点击了位置点（waypoints）
//            val waypointIndex = findNearestWaypoint(latLng, currentState.route?.tracks ?: emptyList())
//            if (waypointIndex != -1) {
//                // 点击了位置点，切换到Overview2状态
//                _uiState.update { state ->
//                    state.copy(
//                        chartState = OverviewState.Overview2,
//                        selectedWaypoint = waypointIndex,
//                    )
//                }
//                return
//            }
//
//            // 然后检查是否点击了轨迹点
//            if (currentState.selectedTrackPoint != null) {
//                val selectedPoint = currentState.trackpoints.getOrNull(currentState.selectedTrackPoint)
//                if (selectedPoint != null) {
//                    val distance = calculateDistance(
//                        latLng.latitude, latLng.longitude,
//                        selectedPoint.latitude, selectedPoint.longitude
//                    )
//                    if (distance < 50) { // 50米范围内认为是点击了轨迹点
//                        // 显示轨迹点信息，切换到Overview2状态
//                        _uiState.update { state ->
//                            state.copy(
//                                chartState = OverviewState.Overview2,
//                            )
//                        }
//                        return
//                    }
//                }
//            }
//        }

        when (editMode) {
            RouteMode.ADD_LOCATION -> {
                // 添加位置模式：只有当存在轨迹时才处理点击
                if (currentState.hasTracks) {
                    val currentState = _uiState.value
                    val route = currentState.route ?: return

                    // 找到轨迹上距离点击坐标最近的点
                    val nearestPointIndex = findNearestTrackPoint(latLng, route.tracks)

                    _uiState.update { state ->
                        state.copy(
                            selectedTrackPoint = nearestPointIndex,
                            overviewState = OverviewState.Overview2
                        )
                    }
                }
            }

            else -> {
                // 路线规划模式：添加路点并生成轨迹
                handleRoutePlanning(latLng, sportType, editMode)
            }
        }
    }

    fun onWaypointClick(waypoint: Waypoint) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    overviewState = OverviewState.Overview3,
                    selectedWaypoint = waypoint
                )
            }
        }
    }

    /**
     * 检查网络连接状态
     */
    private fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun handleRoutePlanning(latLng: LatLng, sportType: SportType, editMode: RouteMode) {
        // 取消之前的路线规划任务
        routePlanningJob?.cancel()
        
        routePlanningJob = viewModelScope.launch {
            // 开始加载
            if (_uiState.value.startPoint != null || _uiState.value.endPoint != null) {
                _uiState.update {
                    it.copy(isLoading = true)
                }
            }

            delay(300.milliseconds)
            _uiState.update { state ->
                println("=== 路线规划开始 ===")
                println("当前状态: startPoint=${state.startPoint}, endPoint=${state.endPoint}")
                println("当前轨迹点数量: ${state.route?.tracks?.size ?: 0}")
                println("新点击点: ${latLng.latitude},${latLng.longitude}")
                
                // 根据当前状态决定是设置起点还是终点
                val (newStartPoint, newEndPoint) = when {
                    // 没有轨迹时：第一个点作为起点，第二个点作为终点
                    state.route?.tracks?.isEmpty() != false -> {
                        println("模式：首次规划")
                        when {
                            state.startPoint == null -> {
                                // 第一个点作为起点
                                println("设置起点: ${latLng.latitude},${latLng.longitude}")
                                latLng to null
                            }
                            state.endPoint == null -> {
                                // 第二个点作为终点
                                println("设置终点: ${latLng.latitude},${latLng.longitude}")
                                println("当前起点: ${state.startPoint.latitude},${state.startPoint.longitude}")
                                state.startPoint to latLng
                            }
                            else -> {
                                // 已有起终点，更新终点
                                println("更新终点: ${latLng.latitude},${latLng.longitude}")
                                println("当前起点: ${state.startPoint.latitude},${state.startPoint.longitude}")
                                println("原终点: ${state.endPoint.latitude},${state.endPoint.longitude}")
                                state.startPoint to latLng
                            }
                        }
                    }
                    
                    // 有轨迹时：以当前轨迹终止点作为新的起点，新点击点作为终点
                    else -> {
                        println("模式：连续规划")
                        val currentTracks = state.route.tracks
                        val lastTrackPoint = currentTracks.lastOrNull()
                        
                        if (lastTrackPoint != null) {
                            // 以轨迹终止点作为新的起点
                            val newStart = LatLng(lastTrackPoint.latitude, lastTrackPoint.longitude)
                            println("继续规划 - 以轨迹终止点作为新起点: ${newStart.latitude},${newStart.longitude}")
                            println("新终点: ${latLng.latitude},${latLng.longitude}")
                            newStart to latLng
                        } else {
                            // 异常情况：有轨迹但没有轨迹点，使用当前起终点
                            println("异常情况：有轨迹但没有轨迹点，使用当前起终点")
                            state.startPoint to latLng
                        }
                    }
                }

                println("规划参数: newStartPoint=${newStartPoint}, newEndPoint=${newEndPoint}")

                // 创建新的Waypoint用于路径规划
                val locations = listOfNotNull(newStartPoint, newEndPoint)
                println("传递给仓库的locations: ${locations.map { "${it.latitude},${it.longitude}" }}")
                
                val newRoute = routeRepository.onRoutePlanning(state.route, locations, sportType, editMode)
                println("仓库返回的轨迹点数量: ${newRoute.tracks.size}")

                // 当轨迹存在时，更新startPoint和endPoint以反映当前轨迹的实际起止点
                val (updatedStartPoint, updatedEndPoint) = if (newRoute.tracks.isNotEmpty()) {
                    val firstTrackPoint = newRoute.tracks.first()
                    val lastTrackPoint = newRoute.tracks.last()
                    val actualStartPoint = LatLng(firstTrackPoint.latitude, firstTrackPoint.longitude)
                    val actualEndPoint = LatLng(lastTrackPoint.latitude, lastTrackPoint.longitude)
                    
                    println("更新起止点以反映轨迹:")
                    println("轨迹起点: ${actualStartPoint.latitude},${actualStartPoint.longitude}")
                    println("轨迹终点: ${actualEndPoint.latitude},${actualEndPoint.longitude}")
                    
                    actualStartPoint to actualEndPoint
                } else {
                    println("没有轨迹点，使用原始起止点")
                    newStartPoint to newEndPoint
                }

                println("最终起止点: startPoint=${updatedStartPoint}, endPoint=${updatedEndPoint}")
                println("=== 路线规划结束 ===")

                state.copy(
                    isLoading = false,
                    route = newRoute,
                    startPoint = updatedStartPoint,
                    endPoint = updatedEndPoint,
                    isReverseRouteUsed = false // 重置原路返回状态，因为用户开始新的路线规划
                )
            }

            // 保存操作后的状态到备忘录
            saveState()
        }
    }

    private fun findNearestTrackPoint(clickPoint: LatLng, tracks: List<Trackpoint>): Int {
        if (tracks.isEmpty()) return -1

        var nearestIndex = 0
        var minDistance = Double.MAX_VALUE

        tracks.forEachIndexed { index, trackpoint ->
            val distance = calculateDistance(
                clickPoint.latitude, clickPoint.longitude,
                trackpoint.latitude, trackpoint.longitude
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = index
            }
        }

        return nearestIndex
    }

    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371000 // 地球半径（米）
        val dLat = toRadians(lat2 - lat1)
        val dLon = toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    fun swapStartEnd() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val currentRoute = currentState.route ?: return@update currentState
                val newRoute = routeRepository.swapStartEnd(currentRoute)
                if (newRoute != null) {
                    // 更新起止点以反映轨迹的实际起止点
                    val (updatedStartPoint, updatedEndPoint) = if (newRoute.tracks.isNotEmpty()) {
                        val firstTrackPoint = newRoute.tracks.first()
                        val lastTrackPoint = newRoute.tracks.last()
                        val actualStartPoint = LatLng(firstTrackPoint.latitude, firstTrackPoint.longitude)
                        val actualEndPoint = LatLng(lastTrackPoint.latitude, lastTrackPoint.longitude)
                        
                        println("交换起止点后更新为轨迹实际起止点:")
                        println("轨迹起点: ${actualStartPoint.latitude},${actualStartPoint.longitude}")
                        println("轨迹终点: ${actualEndPoint.latitude},${actualEndPoint.longitude}")
                        
                        actualStartPoint to actualEndPoint
                    } else {
                        // 如果没有轨迹，直接交换起止点
                        currentState.endPoint to currentState.startPoint
                    }

                    currentState.copy(
                        route = newRoute,
                        startPoint = updatedStartPoint,
                        endPoint = updatedEndPoint
                    )
                } else {
                    currentState
                }
            }

            // 保存操作后的状态到备忘录
            saveState()
        }
    }

    fun reverseRoute() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                val currentRoute = currentState.route ?: return@update currentState
                val newRoute = routeRepository.reverseRoute(currentRoute)
                if (newRoute != null) {
                    // 更新起止点以反映轨迹的实际起止点
                    val (updatedStartPoint, updatedEndPoint) = if (newRoute.tracks.isNotEmpty()) {
                        val firstTrackPoint = newRoute.tracks.first()
                        val lastTrackPoint = newRoute.tracks.last()
                        val actualStartPoint = LatLng(firstTrackPoint.latitude, firstTrackPoint.longitude)
                        val actualEndPoint = LatLng(lastTrackPoint.latitude, lastTrackPoint.longitude)
                        
                        println("往返路线后更新为轨迹实际起止点:")
                        println("轨迹起点: ${actualStartPoint.latitude},${actualStartPoint.longitude}")
                        println("轨迹终点: ${actualEndPoint.latitude},${actualEndPoint.longitude}")
                        println("路线将包含往返路径: A→B→C→D→C→B→A")
                        
                        actualStartPoint to actualEndPoint
                    } else {
                        // 如果没有轨迹，保持原有起止点
                        currentState.startPoint to currentState.endPoint
                    }

                    currentState.copy(
                        route = newRoute,
                        startPoint = updatedStartPoint,
                        endPoint = updatedEndPoint,
                        isReverseRouteUsed = true // 标记原路返回已被使用
                    )
                } else {
                    currentState
                }
            }

            // 保存操作后的状态到备忘录
            saveState()
        }
    }

    /**
     * 撤销操作
     */
    fun undo() {
        println("执行撤销操作")
        val restoredState = mementoManager.undo()
        if (restoredState != null) {
            println("撤销恢复状态: waypoints=${restoredState.waypoints.size}, tracks=${restoredState.trackpoints.size}")
            // 只更新业务状态，撤销/重做状态由Flow自动更新
            // 注意：重置路线模式，不从restoredState中恢复
            _uiState.update { currentState ->
                currentState.copy(
                    route = restoredState.route,
                    startPoint = restoredState.startPoint,
                    endPoint = restoredState.endPoint,
                    isReverseRouteUsed = restoredState.isReverseRouteUsed, // 恢复原路返回状态
                    selectedRouteMode = restoredState.selectedRouteMode,
                    selectedSportType = restoredState.selectedSportType,
                )
            }
            println("撤销完成: waypoints=${_uiState.value.waypoints.size}, tracks=${_uiState.value.trackpoints.size}")
        } else {
            println("撤销到空状态")
            // 只更新业务状态，撤销/重做状态由Flow自动更新
            _uiState.update { currentState ->
                currentState.copy(
                    route = null,
                    startPoint = null,
                    endPoint = null,
                )
            }
            println("撤销完成: 空状态")
        }
    }

    /**
     * 重做操作
     */
    fun redo() {
        println("执行重做操作")
        val restoredState = mementoManager.redo()
        if (restoredState != null) {
            println("重做恢复状态: waypoints=${restoredState.waypoints.size}, tracks=${restoredState.trackpoints.size}")
            // 只更新业务状态，撤销/重做状态由Flow自动更新
            // 注意：重置路线模式，不从restoredState中恢复
            _uiState.update { currentState ->
                currentState.copy(
                    route = restoredState.route,
                    startPoint = restoredState.startPoint,
                    endPoint = restoredState.endPoint,
                    isReverseRouteUsed = restoredState.isReverseRouteUsed,
                    selectedRouteMode = restoredState.selectedRouteMode,
                    selectedSportType = restoredState.selectedSportType,
                )
            }
            println("重做完成: waypoints=${_uiState.value.waypoints.size}, tracks=${_uiState.value.trackpoints.size}")
        } else {
            println("重做失败: 没有可重做的状态")
        }
    }

    /**
     * 添加位置点（到指定位置）
     */
    fun addWaypoint(waypoint: Waypoint) {
        _uiState.update { currentState ->
            val currentRoute = currentState.route ?: return@update currentState

            val newRoute = routeRepository.onAddWaypoint(currentRoute, waypoint.copy(name = waypoint.name))

            currentState.copy(
                route = newRoute,
                overviewState = OverviewState.Overview1,
                selectedTrackPoint = null,
            )
        }

        // 保存操作后的状态到备忘录
        saveState()
    }

    /**
     * 删除位置点
     */
    fun deleteWaypoint(waypoint: Waypoint) {
        _uiState.update { currentState ->
            val currentRoute = currentState.route ?: return@update currentState
            val newRoute = routeRepository.onDelWaypoint(currentRoute, waypoint.index)

            if (newRoute != null) {
                currentState.copy(
                    route = newRoute,
                    overviewState = OverviewState.Overview1,
                )
            } else {
                currentState
            }
        }

        // 保存操作后的状态到备忘录
        saveState()
    }

    /**
     * 更新位置点
     */
    fun updateWaypoint(waypoint: Waypoint) {
        _uiState.update { currentState ->
            val currentRoute = currentState.route ?: return@update currentState
            val newRoute = routeRepository.onUpdWaypoint(currentRoute, waypoint)

            if (newRoute != null) {
                currentState.copy(
                    route = newRoute
                )
            } else {
                currentState
            }
        }
    }

    /**
     * 隐藏轨迹点信息
     */
    fun cancelWaypoint() {
        _uiState.update { state ->
            state.copy(
                overviewState = OverviewState.Overview1,
                selectedTrackPoint = null
            )
        }
    }

    // ==================== 定位相关方法 ====================
    // 定位器
    private var locator: Locator? = null

    /**
     * 权限授予后调用
     */
    fun onLocationPermissionGranted() {
        _uiState.update { it.copy(isLocationPermissionGranted = true) }
        // 权限授予后立即开始定位
        startLocationUpdates()
    }

    /**
     * 开始定位（获取当前位置）
     */
    fun startLocationUpdates() {
        locator = Locator.with(application)
            .location()
            .onSuccess { result ->
                _uiState.update {
                    it.copy(
                        currentLocation = result.latLng,
                    )
                }
            }
            .onError { errorMsg ->
                setError(errorMsg)
            }
    }

    /**
     * 清除定位错误
     */
    /**
     * 设置错误信息
     */
    fun setError(message: String) {
        _uiState.update {
            it.copy(error = message)
        }
        // 3秒后自动清除错误信息
        viewModelScope.launch {
            delay(3000)
            clearError()
        }
    }

    /**
     * 清除错误信息
     */
    fun clearError() {
        _uiState.update {
            it.copy(error = null)
        }
    }

    /**
     * 获取当前选中轨迹点的信息
     */
    fun getSelectedWayPoint(): Waypoint? {
        val currentState = _uiState.value
        val route = currentState.route ?: return null

        return when (currentState.overviewState) {
            OverviewState.Overview1 -> null

            OverviewState.Overview2 -> {
                // Overview2状态：返回选中的轨迹点信息（用于添加路点）
                val selectedIndex = currentState.selectedTrackPoint ?: return null
                val trackpoint = route.tracks.getOrNull(selectedIndex) ?: return null

                Waypoint(
                    index = selectedIndex,
                    name = trackpoint.name.ifEmpty { "轨迹点${selectedIndex + 1}" },
                    latitude = trackpoint.latitude,
                    longitude = trackpoint.longitude,
                    altitude = trackpoint.altitude,
                    distance = trackpoint.distance
                )
            }

            OverviewState.Overview3 -> {
                // Overview3状态：返回选中的位置点信息（用于更新路点）
                val selectedWaypoint = currentState.selectedWaypoint ?: return null
                route.waypoints.find { it.index == selectedWaypoint.index }
            }
        }
    }

    /**
     * 保存路线
     */
    fun saveRoute(name: String) {
        viewModelScope.launch {
            // 开始加载
            _uiState.update { state ->
                state.copy(isLoading = true, shouldTakeScreenshot = true)
            }

            delay(500L)

            val currentRoute = _uiState.value.route
            if (currentRoute != null) {
                // 验证路线数据
                val result = routeRepository.saveRoute(currentRoute)
                setError(result.message)
            } else {
                setError("没有可保存的路线")
            }

            // 结束加载
            _uiState.update { state ->
                state.copy(isLoading = false)
            }
        }
    }

    /**
     * 处理图表滑动，更新选中的轨迹点
     */
    fun onChartScroll(progress: Float) {
        val currentState = _uiState.value
        val route = currentState.route ?: return
        val tracks = route.tracks

        if (tracks.isEmpty()) return

        // 根据进度计算轨迹点索引
        val targetIndex = (progress * (tracks.size - 1)).toInt().coerceIn(0, tracks.size - 1)

        _uiState.update { state ->
            state.copy(
                selectedTrackPoint = targetIndex,
            )
        }
    }

    /**
     * 更新运动类型
     */
    fun updateSportType(sportType: SportType) {
        _uiState.update { it.copy(selectedSportType = sportType) }
    }

    /**
     * 更新编辑模式
     */
    fun updateRouteMode(routeMode: RouteMode) {
        _uiState.update { it.copy(selectedRouteMode = routeMode) }
    }

    override fun onCleared() {
        super.onCleared()
        locator?.destroy()
        locator = null
    }

    fun onSearchLocationChanged(it: Location) {
        _uiState.update { state ->
            state.copy(searchLocation = LatLng(it.latitude, it.longitude))
        }
    }

    /**
     * 截图完成回调
     */
    fun onScreenshotTaken(bitmap: Bitmap?) {
        viewModelScope.launch {
            val path = bitmap?.let { saveBitmapToFile(application, it) }
            _uiState.update { state ->
                state.copy(shouldTakeScreenshot = false)
            }
        }
    }

    /**
     * 保存bitmap到本地文件
     */
    private suspend fun saveBitmapToFile(context: Context, bitmap: Bitmap): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "map_screenshot_$timeStamp.jpg"

            // 对于Android 10+，使用应用专属目录
            val picturesDir = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                File(context.getExternalFilesDir(Environment.DIRECTORY_PICTURES), "MapCompose")
            } else {
                val externalPicturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                File(externalPicturesDir, "MapCompose")
            }

            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }

            val file = File(picturesDir, fileName)
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()

            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
