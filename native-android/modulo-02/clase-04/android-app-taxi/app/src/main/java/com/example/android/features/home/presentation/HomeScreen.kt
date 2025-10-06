package com.example.android.features.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val bg = Color.White
    NavigationBarStyle(color = bg, darkIcons = true)

    // Interceptar el botón back
    BackHandler {
        onBackPressed()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Centro: texto "home"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "home",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF0F0F0F)
            )
        }

        // Botón al mismo nivel que en SignInValidateOtpScreen
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Cerrar sesión",
                onClick = onLogout,
                enabled = true,
                loading = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}