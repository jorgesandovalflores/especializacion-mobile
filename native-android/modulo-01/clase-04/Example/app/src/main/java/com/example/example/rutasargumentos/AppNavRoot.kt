package com.example.example.rutasargumentos

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.compose.foundation.layout.padding

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.ProductList,
            modifier = Modifier.padding(padding)
        ) {

            // Lista de productos
            composable(Routes.ProductList) {
                ProductListScreen(
                    onOpenDetail = { id ->
                        // Navegar enviando un ID (int)
                        navController.navigate(Routes.productDetail(id))
                    }
                )
            }

            // Detalle con argumento tipado Int
            composable(
                route = Routes.ProductDetail,
                arguments = listOf(navArgument("id") { type = NavType.IntType })
            ) { backStackEntry ->
                // Leer argumento; valor por defecto si no llega
                val id = backStackEntry.arguments?.getInt("id") ?: 0
                ProductDetailScreen(
                    productId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}