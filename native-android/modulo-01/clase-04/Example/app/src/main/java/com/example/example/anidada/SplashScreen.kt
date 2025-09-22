package com.example.example.anidada

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinish: (hasSession: Boolean) -> Unit) {
    // recuperar token/estado
    LaunchedEffect(Unit) {
        delay(600)
        val hasSession = false // cambia a true para probar salto directo a Home
        onFinish(hasSession)
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}