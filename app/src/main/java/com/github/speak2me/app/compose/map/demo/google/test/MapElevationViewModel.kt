package com.github.speak2me.app.compose.map.demo.google.test

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.speak2me.app.compose.map.demo.google.test.repo.TrackRepository
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

class MapElevationViewModel(
    private val trackRepository: TrackRepository = TrackRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(MapElevationUiState())
    val uiState: StateFlow<MapElevationUiState> = _uiState.asStateFlow()

    init {
        loadTrackPoints()
    }

    private fun loadTrackPoints() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            trackRepository.getTrackPoints()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = exception.message
                    )
                }
                .collect { trackPoints ->
                    _uiState.value = _uiState.value.copy(
                        trackPoints = trackPoints,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun onMapClick(clickPoint: LatLng) {
        val currentState = _uiState.value
        if (currentState.trackPoints.isEmpty()) return

        val nearestIndex = findNearestTrackPoint(clickPoint, currentState.trackPoints)
        val chartPosition = nearestIndex.toFloat() / (currentState.trackPoints.size - 1)

        _uiState.value = currentState.copy(
            selectedPointIndex = nearestIndex,
            hasMarker = true,
            chartDragPosition = chartPosition
        )
    }

    fun onChartDragPositionChange(newPosition: Float) {
        val currentState = _uiState.value
        if (currentState.trackPoints.isEmpty()) return

        val newIndex = (newPosition * (currentState.trackPoints.size - 1)).roundToInt()
            .coerceIn(0, currentState.trackPoints.size - 1)

        _uiState.value = currentState.copy(
            selectedPointIndex = newIndex,
            chartDragPosition = newPosition
        )
    }

    fun clearMarker() {
        _uiState.value = _uiState.value.copy(
            selectedPointIndex = null,
            hasMarker = false,
            chartDragPosition = 0f
        )
    }

    fun refreshTrackPoints() {
        loadTrackPoints()
    }

    // 计算地图中心点
    fun getMapCenter(): LatLng? {
        val trackPoints = _uiState.value.trackPoints
        if (trackPoints.isEmpty()) return null

        val avgLat = trackPoints.map { it.latLng.latitude }.average()
        val avgLng = trackPoints.map { it.latLng.longitude }.average()
        return LatLng(avgLat, avgLng)
    }

    // 获取当前选中的轨迹点
    fun getSelectedTrackPoint(): TrackPoint? {
        val currentState = _uiState.value
        val selectedIndex = currentState.selectedPointIndex
        return if (selectedIndex != null && selectedIndex < currentState.trackPoints.size) {
            currentState.trackPoints[selectedIndex]
        } else null
    }

    // 计算两点之间的距离（米）
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371000.0 // 地球半径（米）
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return R * c
    }

    // 找到最近的轨迹点
    private fun findNearestTrackPoint(clickPoint: LatLng, trackPoints: List<TrackPoint>): Int {
        var minDistance = Double.MAX_VALUE
        var nearestIndex = 0

        trackPoints.forEachIndexed { index, trackPoint ->
            val distance = calculateDistance(
                clickPoint.latitude, clickPoint.longitude,
                trackPoint.latLng.latitude, trackPoint.latLng.longitude
            )
            if (distance < minDistance) {
                minDistance = distance
                nearestIndex = index
            }
        }

        return nearestIndex
    }
}