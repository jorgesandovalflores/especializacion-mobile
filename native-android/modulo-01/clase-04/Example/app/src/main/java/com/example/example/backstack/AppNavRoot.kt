package com.example.example.backstack

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    // definición de pestañas top-level (cada una es un grafo)
    data class Tab(val graphRoute: String, val label: String, val icon: ImageVector)
    val tabs = listOf(
        Tab(Routes.FeedGraph, "Feed", Icons.Filled.Home),
        Tab(Routes.SearchGraph, "Search", Icons.Filled.Search),
        Tab(Routes.ProfileGraph, "Profile", Icons.Filled.Person),
    )

    Scaffold(
        bottomBar = {
            val currentDestination by navController.currentBackStackEntryAsState()
            NavigationBar {
                tabs.forEach { tab ->
                    val selected = currentDestination?.destination.isInHierarchy(tab.graphRoute)
                    NavigationBarItem(
                        selected = selected,
                        onClick = {
                            // navegar al grafo del tab con control de back stack
                            navController.navigate(tab.graphRoute) {
                                // Opción A: usar la ruta raíz explícita (coincide con tu ejemplo)
                                popUpTo(Routes.Root) { saveState = true }

                                // Opción B (recomendada por docs): al startDestination del grafo actual
                                // popUpTo(navController.graph.findStartDestination().id) { saveState = true }

                                launchSingleTop = true     // evita duplicados del mismo destino
                                restoreState = true        // restaura estado del tab si existía
                            }
                        },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            route = Routes.Root,                        // raíz del grafo
            startDestination = Routes.FeedGraph,        // tab inicial
            modifier = Modifier.padding(padding)
        ) {
            // ======== FEED GRAPH ========
            navigation(
                startDestination = Routes.Feed,
                route = Routes.FeedGraph
            ) {
                composable(Routes.Feed) {
                    FeedScreen(
                        onOpenDetail = { id -> navController.navigate(Routes.feedDetail(id)) }
                    )
                }
                composable(
                    route = Routes.FeedDetail,
                    arguments = listOf(navArgument("id") { type = NavType.IntType })
                ) { entry ->
                    val id = entry.arguments?.getInt("id") ?: 0
                    FeedDetailScreen(
                        id = id,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            // ======== SEARCH GRAPH ========
            navigation(
                startDestination = Routes.Search,
                route = Routes.SearchGraph
            ) {
                composable(Routes.Search) { SearchScreen() }
                // aquí puedes añadir subpantallas de Search si lo necesitas
            }

            // ======== PROFILE GRAPH ========
            navigation(
                startDestination = Routes.Profile,
                route = Routes.ProfileGraph
            ) {
                composable(Routes.Profile) { ProfileScreen() }
                // aquí puedes añadir subpantallas de Profile si lo necesitas
            }
        }
    }
}

private fun NavDestination?.isInHierarchy(route: String): Boolean {
    return this?.hierarchy?.any { it.route == route } == true
}