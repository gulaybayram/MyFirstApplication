package com.example.myfirstapplication.data.remote

import com.example.myfirstapplication.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("v1/forecast")
    suspend fun getForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,weather_code,wind_speed_10m,precipitation,is_day",
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 7
    ): WeatherResponse

    // Birden fazla koordinat tek istekte: "40.1,39.9,..." şeklinde virgüllü liste
    @GET("v1/forecast")
    suspend fun getForecastBatch(
        @Query("latitude") latitudes: String,
        @Query("longitude") longitudes: String,
        @Query("current") current: String =
            "temperature_2m,apparent_temperature,weather_code,wind_speed_10m,precipitation,is_day",
        @Query("daily") daily: String =
            "weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max",
        @Query("timezone") timezone: String = "auto",
        @Query("forecast_days") forecastDays: Int = 1
    ): List<WeatherResponse>

}