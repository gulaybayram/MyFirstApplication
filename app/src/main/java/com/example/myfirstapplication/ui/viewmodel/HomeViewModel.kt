package com.example.myfirstapplication.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapplication.data.local.FavoritesStore
import com.example.myfirstapplication.data.model.FavoriteCity
import com.example.myfirstapplication.data.repository.WeatherRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val store = FavoritesStore.get(application)
    private val repository = WeatherRepository()

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            store.favorites.collectLatest { load(it) }
        }
    }

    fun retry() {
        viewModelScope.launch { load(store.favorites.value) }
    }

    private suspend fun load(favorites: List<FavoriteCity>) {
        _uiState.update { it.copy(isLoading = true, hasError = false) }

        coroutineScope {
            val home = async {
                repository.getForecast(MY_LOCATION.latitude, MY_LOCATION.longitude)
            }
            val favs = favorites.map { city ->
                async { city to repository.getForecast(city.latitude, city.longitude) }
            }

            val homeResult = home.await()
            val favResults = favs.awaitAll()

            _uiState.value = HomeUiState(
                isLoading = false,
                myLocation = homeResult.getOrNull(),
                favorites = favResults.map { (city, result) ->
                    FavoriteWeather(city, result.getOrNull())
                },
                hasError = homeResult.isFailure
            )
        }
    }

    companion object {
        // GPS'i sonraki adımda ekleyeceğiz, şimdilik sabit
        val MY_LOCATION = FavoriteCity("Bursa", 40.1826, 29.0665)
    }
}