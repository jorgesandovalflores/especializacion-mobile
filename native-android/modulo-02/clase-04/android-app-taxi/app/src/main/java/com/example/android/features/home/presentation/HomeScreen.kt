package com.example.android.features.home.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import kotlinx.coroutines.delay

private const val WELCOME_TOAST_MS = 3_000L

@Composable
fun HomeRoute(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel()
) {
    var showWelcome by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(showWelcome) {
        if (showWelcome) {
            delay(WELCOME_TOAST_MS)
            showWelcome = false
        }
    }

    HomeScreen(
        showWelcome = showWelcome,
        onLogout = { vm.logout(onLoggedOut) },
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    showWelcome: Boolean,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarStyle(darkIcons = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Home",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F0F0F)
                )
                Text(
                    text = "Tu sesión está activa",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color(0xFF6B6B6B)
                )
            }

            PrimaryButton(
                text = "Cerrar sesión",
                onClick = onLogout,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }

        ToastHost(
            toast = if (showWelcome) ToastMessage(ToastType.Success, "Sesión iniciada correctamente") else null,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
private fun HomeScreenPreview() {
    HomeScreen(showWelcome = true, onLogout = {})
}
