package com.example.example.features.mvp

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.example.common.model.Product

@OptIn(ExperimentalMaterial3Api::class)
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
        topBar = {
            TopAppBar(
                title = { Text("Products (MVP)") }
            )
        }
    ) { innerPadding ->

        Box(
            Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            when {
                isLoading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                error != null -> Text("Error: $error", Modifier.padding(16.dp))
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = products,
                            key = { it.id }
                        ) { p ->
                            ElevatedCard(Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(p.name, style = MaterialTheme.typography.titleMedium)
                                    Text("$${p.price}")
                                    Text(if (p.inStock) "In stock" else "Out of stock")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

