package com.example.android.features.splash.presentation

import com.example.android.R
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.core.presentation.theme.ColorSecondary
import kotlinx.coroutines.delay

// Muestra un loader 1.5s y navega a "signIn"
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val bg = ColorPrimary

    NavigationBarStyle(color = bg, darkIcons = true)

    // Timer para navegación
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    // Fondo + logo + loader
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.feature_splash_logo),
                contentDescription = "Splash Logo",
                modifier = Modifier.size(160.dp)
            )
            CircularProgressIndicator(
                color = ColorSecondary,
                strokeWidth = 4.dp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SplashScreen {}
}
