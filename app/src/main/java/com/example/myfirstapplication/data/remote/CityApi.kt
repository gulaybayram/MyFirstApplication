package com.example.myfirstapplication.data.remote

import com.example.myfirstapplication.data.model.CitySearchResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface CityApi {
    @GET("v1/search")
    suspend fun searchCities(
        @Query("name") name: String,
        @Query("count") count: Int = 20,
        @Query("language") language: String = "tr",
        @Query("format") format: String = "json"
    ): CitySearchResponse
}