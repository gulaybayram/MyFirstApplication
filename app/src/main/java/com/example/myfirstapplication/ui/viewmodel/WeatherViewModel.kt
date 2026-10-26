package com.example.myfirstapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapplication.data.local.FavoritesStore
import com.example.myfirstapplication.data.model.FavoriteCity
import com.example.myfirstapplication.data.model.WeatherResponse
import com.example.myfirstapplication.data.repository.WeatherRepository
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository()
    private val store = FavoritesStore.get(application)

    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState: StateFlow<WeatherUiState> = _uiState.asStateFlow()

    val favorites: StateFlow<List<FavoriteCity>> = store.favorites

    private var lastLat: Double? = null
    private var lastLon: Double? = null

    fun load(lat: Double, lon: Double) {
        if (lat == lastLat && lon == lastLon && _uiState.value !is WeatherUiState.Error) return
        lastLat = lat
        lastLon = lon
        fetch(lat, lon)
    }

    fun retry() {
        val lat = lastLat ?: return
        val lon = lastLon ?: return
        fetch(lat, lon)
    }

    fun toggleFavorite(city: FavoriteCity) = store.toggle(city)

    private fun fetch(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = WeatherUiState.Loading
            repository.getForecast(lat, lon).fold(
                onSuccess = { _uiState.value = WeatherUiState.Success(it) },
                onFailure = { e ->
                    _uiState.value = WeatherUiState.Error(
                        if (e is IOException) "İnternet bağlantını kontrol et."
                        else "Hava durumu alınamadı."
                    )
                }
            )
        }
    }
}