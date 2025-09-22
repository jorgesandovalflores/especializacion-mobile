package com.example.example.configuracion

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    var counter by rememberSaveable { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Home") },
                actions = {
                    TextButton(
                        onClick = {
                            // "logout" simple → volver a Login y limpiar Home
                            navController.navigate("login") {
                                popUpTo("home") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    ) { Text("Logout") }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Counter: $counter")
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = { counter++ }) { Text("Increment") }
                OutlinedButton(onClick = { counter = 0 }) { Text("Reset") }
            }
            Text(
                "El back stack se respeta:\n" +
                        "- Splash se elimina al ir a Login.\n" +
                        "- Login se elimina al ir a Home.\n" +
                        "Presionar back en Home cerrará la app (no hay pantallas previas)."
            )
        }
    }
}
