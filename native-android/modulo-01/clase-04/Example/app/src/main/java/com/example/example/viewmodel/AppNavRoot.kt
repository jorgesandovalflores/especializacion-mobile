package com.example.example.viewmodel

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.*

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "login",
            modifier = Modifier.padding(padding)
        ) {
            composable("login") {
                LoginScreen(
                    onNavigateHome = {
                        navController.navigate("home") {
                            popUpTo("login") { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateRegister = {
                        navController.navigate("register")
                    }
                )
            }
            composable("register") {
                RegisterScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable("home") {
                HomeScreen()
            }
        }
    }
}