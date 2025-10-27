package com.example.android.features.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.core.presentation.theme.ColorPrimary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onLogout: () -> Unit = {},
    onBackPressed: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val bg = Color(0xFFF4F5F6)
    NavigationBarStyle(color = bg, darkIcons = true)

    // Interceptar el botón back
    BackHandler { onBackPressed() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bg)
            .systemBarsPadding()
            .imePadding()
    ) {
        // Botón circular superior izquierdo
        Box(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            MenuIconButton(
                iconRes = com.example.android.R.drawable.feature_home_ic_menu,
                onClick = onMenuClick
            )
        }

        // Centro: texto "home"
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "home",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF0F0F0F)
            )
        }

        // Botón inferior: "Cerrar sesión"
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 24.dp),
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

@Composable
fun HomeScreenRoute(
    onNavigateToMenu: () -> Unit,
    onLogout: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        modifier = modifier,
        onMenuClick = onNavigateToMenu,
        onLogout = onLogout,
        onBackPressed = onBackPressed
    )
}

/**
 * Botón circular con efecto de "pressed"
 */
@Composable
fun MenuIconButton(
    iconRes: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button (
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = ColorPrimary,
            contentColor = Color.White
        ),
        elevation = ButtonDefaults.buttonElevation( // elevación igual a PrimaryButton
            defaultElevation = 6.dp,
            pressedElevation = 8.dp,
            focusedElevation = 6.dp,
            hoveredElevation = 6.dp,
            disabledElevation = 0.dp
        ),
        contentPadding = PaddingValues(0.dp),
        modifier = modifier.size(54.dp)
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = "menu",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
    }
}

@Preview
@Composable
fun PreviewHomeScreen() {
    HomeScreen(
        onLogout = {},
        onBackPressed = {},
        onMenuClick = {}
    )
}
