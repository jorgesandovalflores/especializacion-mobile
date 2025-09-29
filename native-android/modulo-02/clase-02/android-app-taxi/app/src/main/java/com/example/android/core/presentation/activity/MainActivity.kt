package com.example.android.core.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android.features.splash.presentation.SplashScreen
import com.example.android.features.travels.presentation.TravelScreen
import dagger.hilt.android.AndroidEntryPoint

// host de navegación (splash → travel)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { AppRoot() }
    }
}

private object Routes {
    const val Splash = "splash"
    const val Travel = "travel"
}

@Composable
private fun AppRoot() {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Routes.Splash) {
        composable(Routes.Splash) {
            SplashScreen(onFinished = { nav.navigate(Routes.Travel) { popUpTo(Routes.Splash) { inclusive = true } } })
        }
        composable(Routes.Travel) { TravelScreen() }
    }
}