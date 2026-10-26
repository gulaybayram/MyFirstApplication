package com.example.myfirstapplication.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myfirstapplication.data.DestinationData
import com.example.myfirstapplication.data.repository.EscapeRepository
import com.example.myfirstapplication.ui.util.EscapeAdvisor
import com.example.myfirstapplication.ui.util.EscapeResult
import com.example.myfirstapplication.ui.util.TripFilter
import java.io.IOException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface EscapeUiState {
    data object Loading : EscapeUiState
    data class Success(val result: EscapeResult) : EscapeUiState
    data class Error(val message: String) : EscapeUiState
}

class EscapeViewModel : ViewModel() {

    private val repository = EscapeRepository()
    private val origin = HomeViewModel.MY_LOCATION
    private val destinations = DestinationData.all

    private val _uiState = MutableStateFlow<EscapeUiState>(EscapeUiState.Loading)
    val uiState: StateFlow<EscapeUiState> = _uiState.asStateFlow()

    private val _filter = MutableStateFlow(TripFilter.DAY)
    val filter: StateFlow<TripFilter> = _filter.asStateFlow()

    val originName: String get() = origin.name

    init {
        load()
    }

    fun setFilter(filter: TripFilter) {
        _filter.value = filter
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = EscapeUiState.Loading
            val points = EscapeAdvisor.points(origin.latitude, origin.longitude, destinations)
            repository.fetch(points).fold(
                onSuccess = {
                    _uiState.value = EscapeUiState.Success(
                        EscapeAdvisor.build(origin.latitude, origin.longitude, destinations, it)
                    )
                },
                onFailure = { e ->
                    _uiState.value = EscapeUiState.Error(
                        if (e is IOException) "İnternet bağlantını kontrol et."
                        else "Rotalar alınamadı."
                    )
                }
            )
        }
    }
}