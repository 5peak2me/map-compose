package com.github.speak2me.app.compose.map.route.plan.data.source

import com.github.speak2me.app.compose.map.route.plan.data.api.RoutePlanApi
import com.github.speak2me.app.compose.map.route.plan.data.api.RoutePlanRequest
import com.github.speak2me.app.compose.map.route.plan.data.model.RoutePlanPoints
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

interface RoutePlanRemoteDataSource {
    suspend fun routePlan(profile: String, waypoints: List<List<Double>>): RoutePlanPoints?
}

class RoutePlanRemoteDataSourceImpl : RoutePlanRemoteDataSource {

    private val json = Json {
        ignoreUnknownKeys = true
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api-mifit-us2.zepp.com")
        .addConverterFactory(
            json.asConverterFactory("application/json; charset=UTF8".toMediaType())
        )
        .client(client)
        .build()

    private val service = retrofit.create(RoutePlanApi::class.java)

    override suspend fun routePlan(
        profile: String,
        waypoints: List<List<Double>>,
    ): RoutePlanPoints? {

        val param = RoutePlanRequest(
            profile = profile,
            coordinates = waypoints
        )

        return service.routePlan(param)
    }
}
