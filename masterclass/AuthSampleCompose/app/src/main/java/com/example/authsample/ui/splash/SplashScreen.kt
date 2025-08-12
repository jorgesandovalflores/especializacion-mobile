package com.example.authsample.ui.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.authsample.navigation.Routes
import com.example.authsample.ui.theme.Background
import com.example.authsample.ui.theme.OnBackground
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {

    LaunchedEffect(Unit) {
        delay(1500)
        navController.navigate(Routes.Login) {
            popUpTo(Routes.Splash) { inclusive = true }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Comentario: Placeholder de logo
            Box(modifier = Modifier.size(72.dp))
            Text(
                text = "Ruti",
                fontSize = 28.sp,
                fontWeight = FontWeight.SemiBold,
                color = OnBackground
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "Move smarter, move safe.",
                style = MaterialTheme.typography.bodyMedium,
                color = OnBackground.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}
