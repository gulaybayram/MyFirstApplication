package com.example.myfirstapplication.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private const val GEOCODING_URL = "https://geocoding-api.open-meteo.com/"
    private const val FORECAST_URL = "https://api.open-meteo.com/"

    private fun retrofit(baseUrl: String): Retrofit =
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    val cityApi: CityApi by lazy {
        retrofit(GEOCODING_URL).create(CityApi::class.java)
    }

    val weatherApi: WeatherApi by lazy {
        retrofit(FORECAST_URL).create(WeatherApi::class.java)
    }
}