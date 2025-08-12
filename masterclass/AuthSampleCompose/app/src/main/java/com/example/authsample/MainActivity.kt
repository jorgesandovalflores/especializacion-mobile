package com.example.authsample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authsample.navigation.Routes
import com.example.authsample.ui.login.LoginScreen
import com.example.authsample.ui.splash.SplashScreen
import com.example.authsample.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Composable
fun App() {
    AppTheme {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = Routes.Splash) {
            composable(Routes.Splash) { SplashScreen(navController) }
            composable(Routes.Login) { LoginScreen() }
        }
    }
}
