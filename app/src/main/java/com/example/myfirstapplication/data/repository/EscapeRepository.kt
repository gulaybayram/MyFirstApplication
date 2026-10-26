package com.example.myfirstapplication.data.repository

import com.example.myfirstapplication.data.model.WeatherResponse
import com.example.myfirstapplication.data.remote.ApiClient
import com.example.myfirstapplication.data.remote.WeatherApi
import kotlin.coroutines.cancellation.CancellationException

class EscapeRepository(
    private val api: WeatherApi = ApiClient.weatherApi
) {
    suspend fun fetch(points: List<Pair<Double, Double>>): Result<List<WeatherResponse>> {
        return try {
            val result = api.getForecastBatch(
                latitudes = points.joinToString(",") { it.first.toString() },
                longitudes = points.joinToString(",") { it.second.toString() }
            )
            if (result.size == points.size) Result.success(result)
            else Result.failure(IllegalStateException("Beklenmeyen yanıt"))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}