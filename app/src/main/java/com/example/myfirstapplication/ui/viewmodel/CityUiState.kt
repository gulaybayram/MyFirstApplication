package com.example.myfirstapplication.ui.viewmodel

import com.example.myfirstapplication.data.model.City

sealed interface CityUiState {
    data object Loading : CityUiState
    data class Success(val cities: List<City>) : CityUiState
    data class Empty(val query: String) : CityUiState
    data class Error(val message: String) : CityUiState
}