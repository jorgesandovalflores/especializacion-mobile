package com.example.android.features.signup.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton

/* =========================
   STEP 1
   ========================= */
@Composable
fun SignUpStep1Route(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    SignUpStep1Screen(onNext = onNext, modifier = modifier)
}

@Composable
fun SignUpStep1Screen(
    onNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color.White
    NavigationBarStyle(color = bg, darkIcons = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Centro: texto
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "sign up – step 1",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF0F0F0F)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            PrimaryButton(
                text = "Continuar",
                onClick = onNext,
                enabled = true,
                loading = false,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun SignUpStep1ScreenPreview() {
    SignUpStep1Screen(onNext = {})
}