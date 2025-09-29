package com.example.android.features.travels.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.travels.domain.model.Trip
import com.example.android.features.travels.domain.usecase.GetPendingTripsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// estado simple de UI
data class TravelUiState(
    val loading: Boolean = false,
    val items: List<Trip> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class TravelViewModel @Inject constructor(
    private val getPendingTrips: GetPendingTripsUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(TravelUiState(loading = true))
    val ui: StateFlow<TravelUiState> = _ui

    init {
        load()
    }

    // carga los viajes pendientes y actualiza el estado
    fun load() {
        viewModelScope.launch {
            _ui.update { it.copy(loading = true, error = null) }
            runCatching { getPendingTrips() }
                .onSuccess { list ->
                    _ui.update { it.copy(loading = false, items = list) }
                }
                .onFailure { e ->
                    _ui.update { it.copy(loading = false, error = e.message ?: "Network error") }
                }
        }
    }
}