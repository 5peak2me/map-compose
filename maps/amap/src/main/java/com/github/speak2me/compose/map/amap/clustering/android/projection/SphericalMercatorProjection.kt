package com.github.speak2me.compose.map.amap.clustering.android.projection

import com.amap.api.maps.model.LatLng
import com.github.speak2me.compose.map.amap.clustering.android.geometry.Point
import kotlin.math.*

public class SphericalMercatorProjection(
    private val worldWidth: Double,
) {
    public fun toPoint(latLng: LatLng): Point {
        val x = latLng.longitude / 360 + .5
        val siny = sin(Math.toRadians(latLng.latitude))
        val y = 0.5 * ln((1 + siny) / (1 - siny)) / -(2 * PI) + .5

        return Point(x * worldWidth, y * worldWidth)
    }

    public fun toLatLng(point: Point): LatLng {
        val x = point.x / worldWidth - 0.5
        val lng = x * 360

        val y = .5 - (point.y / worldWidth)
        val lat = 90 - Math.toDegrees(atan(exp(-y * 2 * PI)) * 2)

        return LatLng(lat, lng)
    }
}