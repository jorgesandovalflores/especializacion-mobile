package com.example.android.core.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.android.features.splash.presentation.SplashScreen
import com.example.android.features.signin.presentation.SignInPhoneScreen
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object SignIn : Route("sign_in")
    data object Home : Route("home")
    data object SignUp : Route("sign_up")
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim  = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim  = android.graphics.Color.TRANSPARENT
            )
        )
        window.isStatusBarContrastEnforced = false
        window.isNavigationBarContrastEnforced = false

        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Splash.path) {

        // Splash
        composable(Route.Splash.path) {
            SplashScreen(
                onFinished = {
                    nav.navigate(Route.SignIn.path) {
                        popUpTo(Route.Splash.path) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        // SignIn -> expone solo dos eventos: Home y SignUp
        composable(Route.SignIn.path) {
            SignInPhoneScreen(
                onGoHome = { phone ->

                },
                onGoSignUp = {

                }
            )
        }

        // Home (placeholder mínimo)
        composable(Route.Home.path) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Home")
            }
        }

        // SignUp (placeholder mínimo)
        composable(Route.SignUp.path) {
            Box(Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                Text("Sign Up")
            }
        }
    }
}