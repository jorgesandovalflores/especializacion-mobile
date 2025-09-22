package com.example.example.argumentos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    onOpenDetail: (Int) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tap an item to open detail and return a result")
            listOf(10, 11, 12, 13, 14).forEach { id ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Item #$id", style = MaterialTheme.typography.titleMedium)
                        Text("Open detail → return message")
                    }
                }
            }
        }
    }
}