package com.github.speak2me.app.compose.map.demo.google.test.repo

import com.github.speak2me.app.compose.map.demo.google.test.TrackPoint
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TrackRepository {

    // 模拟从API或数据库获取轨迹数据
    fun getTrackPoints(): Flow<List<TrackPoint>> = flow {
        // 模拟网络延迟
        delay(1000)

        // 模拟轨迹数据
        val trackPoints = listOf(
            TrackPoint(1, LatLng(31.2304, 121.4737), 10.0, 0.0),
            TrackPoint(2, LatLng(31.2314, 121.4747), 12.0, 150.0),
            TrackPoint(3, LatLng(31.2324, 121.4757), 15.0, 300.0),
            TrackPoint(4, LatLng(31.2334, 121.4767), 18.0, 450.0),
            TrackPoint(5, LatLng(31.2344, 121.4777), 22.0, 600.0),
            TrackPoint(6, LatLng(31.2354, 121.4787), 25.0, 750.0),
            TrackPoint(7, LatLng(31.2364, 121.4797), 28.0, 900.0),
            TrackPoint(8, LatLng(31.2374, 121.4807), 30.0, 1050.0),
            TrackPoint(9, LatLng(31.2384, 121.4817), 32.0, 1200.0),
            TrackPoint(10, LatLng(31.2394, 121.4827), 35.0, 1350.0),
            TrackPoint(11, LatLng(31.2404, 121.4837), 38.0, 1500.0),
            TrackPoint(12, LatLng(31.2414, 121.4847), 40.0, 1650.0),
            TrackPoint(13, LatLng(31.2424, 121.4857), 42.0, 1800.0),
            TrackPoint(14, LatLng(31.2434, 121.4867), 45.0, 1950.0),
            TrackPoint(15, LatLng(31.2444, 121.4877), 48.0, 2100.0)
        )

        emit(trackPoints)
    }


}