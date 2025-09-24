package com.example.example.features.mvc

import com.example.example.common.data.FakeProductRepository
import com.example.example.common.model.Product
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// coordina modelo y actualiza estado observado por la View
class ProductController(
    private val repository: FakeProductRepository = FakeProductRepository()
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _uiState = MutableStateFlow<MVCState>(MVCState.Loading)
    val uiState: StateFlow<MVCState> = _uiState

    // Carga de datos y actualización de estado
    fun load() {
        _uiState.value = MVCState.Loading
        scope.launch {
            runCatching { repository.fetchProducts() }
                .onSuccess { _uiState.value = MVCState.Success(it) }
                .onFailure { _uiState.value = MVCState.Error(it.message ?: "Unknown error") }
        }
    }
}

// Estados simples para la View en MVC
sealed class MVCState {
    data object Loading : MVCState()
    data class Success(val data: List<Product>) : MVCState()
    data class Error(val message: String) : MVCState()
}