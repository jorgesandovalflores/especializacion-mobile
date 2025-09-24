package com.example.example.features.mvc

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// View (Compose) en MVC: observa el estado del Controller
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreenMVC(
    controller: ProductController
) {
    // Colección de estado del Controller
    val state by controller.uiState.collectAsState()

    // Disparar carga al entrar
    LaunchedEffect(Unit) { controller.load() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products (MVC)") }
            )
        }
    ) { innerPadding ->

        Box(Modifier.padding(innerPadding).fillMaxSize()) {
            when (state) {
                is MVCState.Loading -> {
                    Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                }
                is MVCState.Error -> {
                    val msg = (state as MVCState.Error).message
                    Text("Error: $msg", Modifier.padding(16.dp))
                }
                is MVCState.Success -> {
                    val items = (state as MVCState.Success).data
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(items) { p ->
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
