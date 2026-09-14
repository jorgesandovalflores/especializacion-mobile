package com.example.example.features.mvc

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.example.common.ui.ProductListBackground
import com.example.example.common.ui.ProductListContent
import com.example.example.common.ui.ProductListHeader

// View (Compose) en MVC: observa el estado del Controller y delega el
// renderizado en ProductListContent, compartido con MVP y MVVM.
@Composable
fun ProductListScreenMVC(
    controller: ProductController
) {
    // Colección de estado del Controller, lifecycle-aware (se pausa en background)
    val state by controller.uiState.collectAsStateWithLifecycle()

    // Disparar carga al entrar
    LaunchedEffect(Unit) { controller.load() }

    val products = (state as? MVCState.Success)?.data.orEmpty()

    Scaffold(
        containerColor = ProductListBackground
    ) { innerPadding ->
        Column(Modifier.padding(innerPadding).fillMaxSize()) {
            ProductListHeader(
                loading = state is MVCState.Loading,
                error = (state as? MVCState.Error)?.message,
                resultCount = products.size
            )
            ProductListContent(
                loading = state is MVCState.Loading,
                error = (state as? MVCState.Error)?.message,
                products = products,
                onRetry = { controller.load() },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
