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

    @Headers("apptoken: UQVBQEJyQktGXip6SltGSlpuQkZgBAAEAAAAAys-lyKHU3TraUIX4NeP69qth02uESi8AMpcTvAVPlV4PkKC847jjfHW_C-IMevW0JtFMehxI15zigQ3mOuamZOkJ-SCnS3yFKn9fDo2oKmE5WCgupzbkoIgRf9NxCPTstw6GTTqY05wFLLh7uPAGf3apg8z0SPt2HOFdCTr6dwNZshXJmhPSDvaXofauWjTK")
    @POST("route/v1")
    suspend fun routePlan(@Body request: RoutePlanRequest): RoutePlanPoints?
}
