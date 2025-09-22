package com.example.example.viewmodel

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    var counter by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Home") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Counter: $counter")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { counter++ }) { Text("Increment") }
                OutlinedButton(onClick = { counter = 0 }) { Text("Reset") }
            }
        }
    }
}