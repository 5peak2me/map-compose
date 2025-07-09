package com.github.speak2me.app.compose.map.route.plan.utils

import kotlin.math.*

object CoordinateTransform {

    private const val R = 6378245.0  // 地球半径
    private const val EE = 0.006693421622965943  // 扁率

    // region WGS-84 <=> GCJ-02
    fun wgs84ToGcj02(lat: Double, lon: Double): Pair<Double, Double> {
        if (!isInChina(lat, lon)) return lat to lon
        val (deltaLat, deltaLon) = delta(lat, lon)
        return lat + deltaLat to lon + deltaLon
    }
    // endregion

    // region GCJ-02 -> WGS-84 (approximate)
    fun gcj02ToWgs84(lat: Double, lon: Double): Pair<Double, Double> {
        if (!isInChina(lat, lon)) return lat to lon
        val (mgLat, mgLon) = wgs84ToGcj02(lat, lon)
        return lat * 2 - mgLat to lon * 2 - mgLon
    }
    // endregion

    // region GCJ-02 -> BD-09
    fun gcj02ToBd09(lat: Double, lon: Double): Pair<Double, Double> {
        val z = sqrt(lon * lon + lat * lat) + 0.00002 * sin(lat * PI)
        val theta = atan2(lat, lon) + 0.000003 * cos(lon * PI)
        val bdLon = z * cos(theta) + 0.0065
        val bdLat = z * sin(theta) + 0.006
        return bdLat to bdLon
    }
    // endregion

    // region BD-09 -> GCJ-02
    fun bd09ToGcj02(lat: Double, lon: Double): Pair<Double, Double> {
        val x = lon - 0.0065
        val y = lat - 0.006
        val z = sqrt(x * x + y * y) - 0.00002 * sin(y * PI)
        val theta = atan2(y, x) - 0.000003 * cos(x * PI)
        val ggLon = z * cos(theta)
        val ggLat = z * sin(theta)
        return ggLat to ggLon
    }
    // endregion

    private fun isInChina(lat: Double, lon: Double): Boolean {
        return lon in 73.66..135.05 && lat in 3.86..53.55
    }

    private fun delta(lat: Double, lon: Double): Pair<Double, Double> {
        var deltaLat = transformLat(lon - 105.0, lat - 35.0)
        var deltaLon = transformLon(lon - 105.0, lat - 35.0)
        val radLat = lat / 180.0 * PI
        var magic = sin(radLat)
        magic = 1 - EE * magic * magic
        val sqrtMagic = sqrt(magic)
        deltaLat = (deltaLat * 180.0) / ((R * (1 - EE)) / (magic * sqrtMagic) * PI)
        deltaLon = (deltaLon * 180.0) / (R / sqrtMagic * cos(radLat) * PI)
        return deltaLat to deltaLon
    }

    private fun transformLat(x: Double, y: Double): Double {
        var ret = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y + 0.2 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(y * PI) + 40.0 * sin(y / 3.0 * PI)) * 2.0 / 3.0
        ret += (160.0 * sin(y / 12.0 * PI) + 320 * sin(y * PI / 30.0)) * 2.0 / 3.0
        return ret
    }

    private fun transformLon(x: Double, y: Double): Double {
        var ret = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1 * sqrt(abs(x))
        ret += (20.0 * sin(6.0 * x * PI) + 20.0 * sin(2.0 * x * PI)) * 2.0 / 3.0
        ret += (20.0 * sin(x * PI) + 40.0 * sin(x / 3.0 * PI)) * 2.0 / 3.0
        ret += (150.0 * sin(x / 12.0 * PI) + 300.0 * sin(x / 30.0 * PI)) * 2.0 / 3.0
        return ret
    }

}
