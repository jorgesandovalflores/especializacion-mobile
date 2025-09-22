package com.example.example.argumentos

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: Int,
    onFinishWithResult: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Detail #$id") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Item ID: $id", style = MaterialTheme.typography.headlineSmall)
            Button(onClick = {
                onFinishWithResult("Favorite for #$id added")
            }) {
                Text("Save and return")
            }
        }
    }
}