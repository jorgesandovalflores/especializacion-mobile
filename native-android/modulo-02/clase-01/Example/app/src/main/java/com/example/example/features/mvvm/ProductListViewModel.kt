package com.example.example.features.mvvm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.example.common.data.LoggingProductRepository
import com.example.example.common.data.ProductRemoteRepository
import com.example.example.common.data.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// ViewModel: no conoce a la View, expone el estado de negocio de dos formas
// distintas para poder compararlas en vivo (ver viewmodel-flow-state-livedata.md):
//  - StateFlow (ui)          -> moderno, natural en Compose con collectAsStateWithLifecycle().
//  - LiveData (lastUpdatedAt) -> legacy, lifecycle-aware por sí mismo, se observa con observeAsState().
// El filtro "solo en stock" NO vive aquí: es Compose State, local a la pantalla
// (ver ProductListScreenMVVM.kt), porque es un detalle de UI, no de negocio.
class ProductListViewModel(
    private val repository: ProductRepository = LoggingProductRepository(ProductRemoteRepository()),
    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
) : ViewModel() {

    private val _ui = MutableStateFlow(ProductListUiState(loading = true))
    val ui: StateFlow<ProductListUiState> = _ui

    private val _lastUpdatedAt = MutableLiveData<Long?>(null)
    val lastUpdatedAt: LiveData<Long?> = _lastUpdatedAt

    // Memento (ver patrones-diseno-arquitecturas.md): el conteo de reintentos
    // se guarda en SavedStateHandle, así que sobrevive a la muerte de proceso,
    // no solo a la rotación de pantalla.
    val retryCount: StateFlow<Int> = savedStateHandle.getStateFlow(KEY_RETRY_COUNT, 0)

    init {
        load()
    }

    // Carga datos y actualiza el estado (Command: "cargar productos" como acción de UI)
    fun load() {
        _ui.value = ProductListUiState(loading = true)
        viewModelScope.launch {
            runCatching { repository.fetchProducts() }
                .onSuccess {
                    _ui.value = ProductListUiState(data = it)
                    _lastUpdatedAt.value = System.currentTimeMillis()
                }
                .onFailure { _ui.value = ProductListUiState(error = it.message ?: "Unknown error") }
        }
    }

    fun retry() {
        savedStateHandle[KEY_RETRY_COUNT] = (savedStateHandle.get<Int>(KEY_RETRY_COUNT) ?: 0) + 1
        load()
    }

    // Factory sin reflexión: crea el ViewModel inyectando un SavedStateHandle
    // real provisto por el propio framework de navegación/Activity.
    companion object {
        private const val KEY_RETRY_COUNT = "retry_count"

        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProductListViewModel(savedStateHandle = createSavedStateHandle())
            }
        }
    }
}
