package com.example.example.anidada

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController

@Composable
fun AppNavRoot() {
    val navController = rememberNavController()

    Scaffold { padding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(padding)
        ) {
            // Splash de arranque (simula lectura de sesión)
            composable("splash") {
                SplashScreen(
                    onFinish = { hasSession ->
                        if (hasSession) {
                            // Ir directo a Home y limpiar Splash
                            navController.navigate("home") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        } else {
                            // Entrar al grafo auth y limpiar Splash
                            navController.navigate("auth") {
                                popUpTo("splash") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }

            // === GRAFO ANIDADO: AUTH ===
            navigation(startDestination = "signin", route = "auth") {

                composable("signin") {
                    SignInScreen(
                        onGoToSignUp = { navController.navigate("signup") },
                        onSuccess = {
                            // Al loguear, limpiar TODO el grafo 'auth' y saltar a Home
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

                composable("signup") {
                    SignUpScreen(
                        onBackToSignIn = { navController.popBackStack() },
                        onRegistered = {
                            // Tras registrarse, mismo patrón que SignIn → Home
                            navController.navigate("home") {
                                popUpTo("auth") { inclusive = true }
                                launchSingleTop = true
                            }
                        }
                    )
                }

            }
            // === FIN GRAFO AUTH ===

            // Home
            composable("home") {
                HomeScreen(
                    onLogout = {
                        // Volver al flujo auth como primera pantalla; limpiar Home
                        navController.navigate("auth") {
                            popUpTo("home") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}