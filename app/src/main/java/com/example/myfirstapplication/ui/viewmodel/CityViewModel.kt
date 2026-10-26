package com.example.myfirstapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapplication.data.repository.CityRepository
import java.io.IOException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CityViewModel(
    private val repository: CityRepository = CityRepository()
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CityUiState>(CityUiState.Empty(""))
    val uiState: StateFlow<CityUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        search(newQuery, debounceMs = 400)
    }

    fun retry() {
        search(_query.value, debounceMs = 0)
    }

    private fun search(rawQuery: String, debounceMs: Long) {
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            val q = rawQuery.trim()
            if (q.length < 2) {
                _uiState.value = CityUiState.Empty(q)
                return@launch
            }

            _uiState.value = CityUiState.Loading
            delay(debounceMs) // kullanıcı yazmayı bitirsin, her harfte istek atma

            repository.searchCities(q).fold(
                onSuccess = { cities ->
                    _uiState.value =
                        if (cities.isEmpty()) CityUiState.Empty(q)
                        else CityUiState.Success(cities)
                },
                onFailure = { e ->
                    _uiState.value = CityUiState.Error(
                        if (e is IOException) "İnternet bağlantını kontrol et."
                        else "Bir şeyler ters gitti."
                    )
                }
            )
        }
    }
}