package com.example.example.features.mvp

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.example.common.model.Product
import com.example.example.common.ui.ProductListBackground
import com.example.example.common.ui.ProductListContent
import com.example.example.common.ui.ProductListHeader

// View (Compose) en MVP: implementa el contrato como View pasiva y delega el
// renderizado en ProductListContent, compartido con MVC y MVVM.
@Composable
fun ProductListScreenMVP(
    presenter: ProductListContract.Presenter = ProductListPresenter()
) {
    // Estado local de UI
    var isLoading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var products by remember { mutableStateOf<List<Product>>(emptyList()) }

    // Implementación de la interfaz View
    val viewImpl = remember {
        object : ProductListContract.View {
            override fun showLoading() { isLoading = true; error = null }
            override fun showProducts(list: List<Product>) {
                isLoading = false; error = null; products = list
            }
            override fun showError(message: String) {
                isLoading = false; error = message
            }
        }
    }

    // Ciclo de vida: attach/detach + load
    DisposableEffect(Unit) {
        presenter.attach(viewImpl)
        presenter.load()
        onDispose { presenter.detach() }
    }

    Scaffold(
        containerColor = ProductListBackground
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            ProductListHeader(loading = isLoading, error = error, resultCount = products.size)
            ProductListContent(
                loading = isLoading,
                error = error,
                products = products,
                onRetry = { presenter.load() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
