package com.example.example.features.mvvm

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.example.common.ui.ProductCategoryFilter
import com.example.example.common.ui.ProductListBackground
import com.example.example.common.ui.ProductListContent
import com.example.example.common.ui.ProductListHeader

// View (Compose) en MVVM: observa el StateFlow del ViewModel y delega el
// renderizado en ProductListContent, compartido con MVC y MVP. El filtro de
// categoría (ProductCategoryFilter, ver design-m02-c01.pen) es exclusivo de
// esta pantalla: la View solo lee ui.categories/ui.selectedCategory y traduce
// los taps en el comando vm.filterByCategory(...), todo el estado derivado
// (ui.products ya filtrado) vive en ProductListUiState. El ViewModel también
// expone LiveData y persiste estado en SavedStateHandle (ver
// ProductListViewModel.kt y viewmodel-flow-state-livedata.md); esta pantalla
// solo consume el StateFlow para mantenerse igual al diseño (design-m02-c01.pen).
@Composable
fun ProductListScreenMVVM(
    vm: ProductListViewModel = viewModel(factory = ProductListViewModel.Factory)
) {
    val ui by vm.ui.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ProductListBackground
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            ProductListHeader(loading = ui.loading, error = ui.error, resultCount = ui.products.size)
            if (ui.categories.isNotEmpty()) {
                ProductCategoryFilter(
                    categories = ui.categories.map { it.label },
                    selected = ui.categories.firstOrNull { it.value == ui.selectedCategory }?.label ?: "Todos",
                    onSelect = { label ->
                        vm.filterByCategory(ui.categories.firstOrNull { it.label == label }?.value)
                    }
                )
            }
            ProductListContent(
                loading = ui.loading,
                error = ui.error,
                products = ui.products,
                onRetry = { vm.retry() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
