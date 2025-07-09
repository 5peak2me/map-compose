package com.github.speak2me.app.compose.map.route.plan.data.api

import com.github.speak2me.app.compose.map.route.plan.data.model.RoutePlanPoints
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

@Serializable
data class RoutePlanRequest(
    val profile: String,
    val coordinates: List<List<Double>>,
)

interface RoutePlanApi {

    @Headers("apptoken: UQVBQEJyQktGXip6SltGSlpuQkZgBAAEAAAAAVWXwD5Sc1t4mBdPlHM3TMdMbj31C0d8nWZJLwaixz5F6_H3zoCtNZqEMVZzw0LVqSMQ6hpccoa12PPA3odSaFr0kNA87Jec5EP5VSB4uxAV0UXZtw93CUqev2ULsqgzcoRt0QcZH1Bvzb3XixQGHzn0Jyh-MCxnjgIXy6hs7tHH6ReRORBJHHKL19z6S7AYh")
    @POST("route/v1")
    suspend fun routePlan(@Body request: RoutePlanRequest): RoutePlanPoints?
}
