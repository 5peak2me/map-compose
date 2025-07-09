package com.github.speak2me.app.compose.map.route.plan.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable

@Parcelize
@Serializable
data class Coordinate(
    val longitude: Double,
    val latitude: Double,
    val altitude: Double? = null
) : Parcelable

// <editor-fold defaultstate="collapsed" desc="路线规划: 起始点规划线路点">
@Serializable
data class RoutePlanPoints(
    val code: Int,
    val message: String,
    @Contextual private val data: Routes?
) {
    val routeCoordinates: List<List<Double>>?
        get() = data?.routes?.firstOrNull()?.geometry?.coordinates
}

@Serializable
data class Routes(val routes: List<Geometry>)

@Serializable
data class Geometry(val geometry: Coordinates)

@Serializable
data class Coordinates(val coordinates: List<List<Double>>, val type: String)

// </editor-fold>
