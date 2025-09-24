package com.example.example.features.mvvm

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

// View (Compose) en MVVM: observa el StateFlow del ViewModel
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreenMVVM(
    vm: ProductListViewModel = viewModel()
) {
    val ui by vm.ui.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Products (MVVM)") }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                ui.loading -> Box(Modifier.fillMaxSize()) { CircularProgressIndicator() }
                ui.error != null -> Text("Error: ${ui.error}", Modifier.padding(16.dp))
                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(ui.data) { p ->
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
