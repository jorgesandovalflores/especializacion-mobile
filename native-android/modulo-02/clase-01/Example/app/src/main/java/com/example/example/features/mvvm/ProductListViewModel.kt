package com.example.example.features.mvvm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.example.common.data.FakeProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel: contiene lógica de presentación y expone StateFlow
class ProductListViewModel(
    private val repository: FakeProductRepository = FakeProductRepository()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProductListUiState(loading = true))
    val ui: StateFlow<ProductListUiState> = _ui

    init {
        load()
    }

    // Carga datos y actualiza el estado
    fun load() {
        _ui.value = ProductListUiState(loading = true)
        viewModelScope.launch {
            runCatching { repository.fetchProducts() }
                .onSuccess { _ui.value = ProductListUiState(data = it) }
                .onFailure { _ui.value = ProductListUiState(error = it.message ?: "Unknown error") }
        }
    }
}