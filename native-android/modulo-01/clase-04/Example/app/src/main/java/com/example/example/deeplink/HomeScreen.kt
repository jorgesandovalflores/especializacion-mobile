package com.example.example.deeplink

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onOpenDetail: (Long) -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Home") }) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tap to open detail internally, or try the deeplink URL.")
            listOf(40L, 41L, 42L, 43L).forEach { id ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Item #$id", style = MaterialTheme.typography.titleMedium)
                        Text("Open detail (internal nav)")
                    }
                }
            }
        }
    }
}