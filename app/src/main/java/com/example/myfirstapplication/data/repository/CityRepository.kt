package com.example.myfirstapplication.data.repository

import com.example.myfirstapplication.data.model.City
import com.example.myfirstapplication.data.remote.ApiClient
import com.example.myfirstapplication.data.remote.CityApi
import kotlin.coroutines.cancellation.CancellationException

class CityRepository(
    private val api: CityApi = ApiClient.cityApi
) {
    suspend fun searchCities(query: String): Result<List<City>> {
        return try {
            val response = api.searchCities(name = query)
            Result.success(response.results.orEmpty())
        } catch (e: CancellationException) {
            throw e // Coroutine iptalini yutma
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}