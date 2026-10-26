package com.example.myfirstapplication.data.repository

import com.example.myfirstapplication.data.model.WeatherResponse
import com.example.myfirstapplication.data.remote.ApiClient
import com.example.myfirstapplication.data.remote.WeatherApi
import kotlin.coroutines.cancellation.CancellationException

class WeatherRepository(
    private val api: WeatherApi = ApiClient.weatherApi
) {
    suspend fun getForecast(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            Result.success(api.getForecast(lat, lon))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}