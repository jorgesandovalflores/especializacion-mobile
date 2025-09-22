package com.example.example.configuracion

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.padding

@Composable
fun AppNavRoot() {
    // Controlador central de navegación
    val navController = rememberNavController()

    // Estructura base con Scaffold
    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            composable("splash") { SplashScreen(navController) }
            composable("login") { LoginScreen(navController) }
            composable("home")  { HomeScreen(navController) }
        }
    }
}