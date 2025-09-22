package com.example.example.rutasargumentos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    onOpenDetail: (Int) -> Unit
) {
    // Comentario: data mock
    val products = remember { listOf(101, 102, 103, 104) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Products") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            products.forEach { id ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Product #$id", style = MaterialTheme.typography.titleMedium)
                        Text("Tap to open detail")
                    }
                }
            }
        }
    }
}