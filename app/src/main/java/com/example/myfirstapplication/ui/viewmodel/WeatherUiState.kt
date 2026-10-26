package com.example.myfirstapplication.ui.viewmodel

import com.example.myfirstapplication.data.model.WeatherResponse

sealed interface WeatherUiState {
    data object Loading : WeatherUiState
    data class Success(val weather: WeatherResponse) : WeatherUiState
    data class Error(val message: String) : WeatherUiState
}