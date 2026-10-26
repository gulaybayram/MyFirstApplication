package com.example.myfirstapplication.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val daily: DailyWeather
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("apparent_temperature") val feelsLike: Double,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("precipitation") val precipitation: Double,
    @SerializedName("is_day") val isDay: Int
)

data class DailyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val tempMax: List<Double>,
    @SerializedName("temperature_2m_min") val tempMin: List<Double>,
    @SerializedName("precipitation_probability_max") val rainProbability: List<Int?>,
    @SerializedName("weather_code") val weatherCode: List<Int>
)