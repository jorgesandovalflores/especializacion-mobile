package com.example.android.features.splash.presentation

import com.example.android.R
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.commons.presentation.MOTION_MEDIUM_MS
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.core.presentation.theme.ColorSecondary
import com.example.android.features.splash.domain.usecase.StartDestination
import kotlinx.coroutines.launch

@Composable
fun SplashRoute(
    onGoSignIn: () -> Unit,
    onGoSignUp: () -> Unit,
    onGoHome: () -> Unit,
    modifier: Modifier = Modifier,
    vm: SplashViewModel = hiltViewModel()
) {
    val destination by vm.destination.collectAsStateWithLifecycle()

    LaunchedEffect(destination) {
        when (destination) {
            StartDestination.SignIn -> onGoSignIn()
            StartDestination.SignUp -> onGoSignUp()
            StartDestination.Home -> onGoHome()
            null -> Unit
        }
    }

    SplashScreen(modifier = modifier)
}

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    NavigationBarStyle(darkIcons = false)

    val inPreview = LocalInspectionMode.current
    val logoScale = remember { Animatable(if (inPreview) 1f else 0.6f) }
    val logoAlpha = remember { Animatable(if (inPreview) 1f else 0f) }
    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(MOTION_MEDIUM_MS)) }
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorPrimary),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.feature_splash_logo),
                contentDescription = "Splash Logo",
                modifier = Modifier
                    .size(160.dp)
                    .graphicsLayer {
                        scaleX = logoScale.value
                        scaleY = logoScale.value
                        alpha = logoAlpha.value
                    }
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
private fun SplashScreenPreview() {
    SplashScreen()
}
