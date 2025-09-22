package com.example.example.deeplink

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.Home,
            modifier = Modifier.padding(padding)
        ) {
            composable(Routes.Home) {
                HomeScreen(
                    onOpenDetail = { id ->
                        // Navegación interna normal
                        navController.navigate(Routes.detail(id))
                    }
                )
            }

            composable(
                route = Routes.Detail,
                arguments = listOf(navArgument("id") { type = NavType.LongType }),
                deepLinks = listOf(
                    // Deeplink https (App Link)
                    navDeepLink { uriPattern = "https://misitio.com/detail/{id}" },
                    // Opcional: esquema propio (custom scheme)
                    navDeepLink { uriPattern = "myapp://detail/{id}" }
                )
            ) { entry ->
                val id = entry.arguments?.getLong("id") ?: 0L
                DetailScreen(
                    itemId = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}