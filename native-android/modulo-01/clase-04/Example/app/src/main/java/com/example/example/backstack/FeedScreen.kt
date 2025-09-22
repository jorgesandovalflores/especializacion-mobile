package com.example.example.backstack

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(onOpenDetail: (Int) -> Unit) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Feed") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Tap an item to open a detail, then switch tabs and come back.")
            listOf(1, 2, 3, 4, 5).forEach { id ->
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenDetail(id) }
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Feed item #$id", style = MaterialTheme.typography.titleMedium)
                        Text("Open detail and keep state per tab")
                    }
                }
            }
        }
    }
}