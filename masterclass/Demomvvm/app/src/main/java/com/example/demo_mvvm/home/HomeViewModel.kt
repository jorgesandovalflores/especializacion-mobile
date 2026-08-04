package com.example.demo_mvvm.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel for the Home screen (see references/ui-pattern-mvvm.md in the
 * android-development skill). Holds a single StateFlow<HomeUiState> that the
 * Compose View observes; the View never talks to [HomeModel] directly.
 *
 * This demo instantiates the Model manually (no Hilt) to keep the MVVM pattern
 * itself front and center; a production app would inject it instead.
 */
class HomeViewModel(
    private val model: HomeModel = HomeModel(),
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            try {
                val data = model.fetchHomeData()
                _uiState.value = HomeUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = HomeUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onTransactionClicked(transactionId: String) {
        viewModelScope.launch { model.markTransactionSeen(transactionId) }
    }
}
