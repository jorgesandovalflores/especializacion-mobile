package com.example.example.backstack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen() {
    var counter by rememberSaveable { mutableStateOf(0) }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Profile") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Counter: $counter")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { counter++ }) { Text("Increment") }
                OutlinedButton(onClick = { counter = 0 }) { Text("Reset") }
            }
            Text("Switch tabs and come back — state should be restored.")
        }
    }
}