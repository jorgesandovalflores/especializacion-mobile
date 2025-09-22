package com.example.example.backstack

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen() {
    var query by rememberSaveable { mutableStateOf("") }
    Scaffold(
        topBar = { TopAppBar(title = { Text("Search") }) }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedTextField(
                value = query, onValueChange = { query = it },
                label = { Text("Query") }, modifier = Modifier.fillMaxWidth()
            )
            Text("Type something, switch tabs, and come back — it should be restored.")
        }
    }
}