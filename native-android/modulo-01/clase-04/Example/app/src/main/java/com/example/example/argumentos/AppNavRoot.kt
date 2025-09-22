package com.example.example.argumentos

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding)
        ) {
            // HOME: consume el resultado aquí (no dentro de HomeScreen)
            composable(Routes.Home) { backStackEntry ->
                val savedStateHandle = backStackEntry.savedStateHandle

                // Observa el resultado como StateFlow (valor por defecto "")
                val resultFlow = remember(savedStateHandle) {
                    savedStateHandle.getStateFlow("detail_result", "")
                }
                val resultValue by resultFlow.collectAsState()

                // Un host para mostrar el snackbar
                val snackbarHostState = remember { SnackbarHostState() }

                // Cuando llega un resultado no vacío, muéstralo y límpialo
                LaunchedEffect(resultValue) {
                    if (resultValue.isNotBlank()) {
                        snackbarHostState.showSnackbar("Result: $resultValue")
                        savedStateHandle["detail_result"] = "" // limpiar para no re-procesar
                    }
                }

                HomeScreen(
                    snackbarHostState = snackbarHostState,
                    onOpenDetail = { id -> navController.navigate(Routes.detail(id)) }
                )
            }

            // DETAIL: recibe el id y devuelve un resultado al entry anterior
            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { entry ->
                val id = entry.arguments?.getInt("id") ?: 0
                DetailScreen(
                    id = id,
                    onFinishWithResult = { message ->
                        navController.previousBackStackEntry
                            ?.savedStateHandle
                            ?.set("detail_result", message)
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}