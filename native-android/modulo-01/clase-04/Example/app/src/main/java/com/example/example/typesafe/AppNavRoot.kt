package com.example.example.typesafe

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = ProductList,
            modifier = Modifier.padding(padding)
        ) {

            // Lista de productos: la ruta es el objeto ProductList, sin strings
            composable<ProductList> {
                ProductListScreen(
                    onOpenDetail = { id ->
                        // Navegar con una instancia tipada del destino
                        navController.navigate(ProductDetail(id))
                    }
                )
            }

            // Detalle: el argumento viaja dentro del propio objeto ProductDetail
            composable<ProductDetail> { backStackEntry ->
                // toRoute() reconstruye la data class ya deserializada y tipada
                val detail: ProductDetail = backStackEntry.toRoute()
                ProductDetailScreen(
                    productId = detail.id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
