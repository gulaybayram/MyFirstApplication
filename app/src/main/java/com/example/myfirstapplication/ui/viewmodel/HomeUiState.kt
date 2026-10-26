package com.example.myfirstapplication.ui.viewmodel

import com.example.myfirstapplication.data.model.FavoriteCity
import com.example.myfirstapplication.data.model.WeatherResponse

data class FavoriteWeather(
    val city: FavoriteCity,
    val weather: WeatherResponse?   // null = bu şehir yüklenemedi
)

data class HomeUiState(
    val isLoading: Boolean = true,
    val myLocation: WeatherResponse? = null,
    val favorites: List<FavoriteWeather> = emptyList(),
    val hasError: Boolean = false
)