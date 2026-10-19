package com.example.android.features.menu.presentation

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.usecase.RefreshMenuState
import kotlinx.coroutines.delay

private const val TOAST_DURATION_MS = 3_000L
private const val SKELETON_ITEMS = 4

@Composable
fun MenuRoute(
    onBack: () -> Unit,
    onMenuClick: (Menu) -> Unit,
    modifier: Modifier = Modifier,
    vm: MenuViewModel = hiltViewModel()
) {
    val items by vm.menu.collectAsStateWithLifecycle()
    val refresh by vm.refreshUi.collectAsStateWithLifecycle()
    var showError by remember { mutableStateOf(false) }

    LaunchedEffect(refresh) {
        showError = refresh is RefreshMenuState.Error
        if (showError) {
            delay(TOAST_DURATION_MS)
            showError = false
        }
    }

    MenuScreen(
        items = items,
        refresh = refresh,
        showError = showError,
        onBack = onBack,
        onRetry = vm::refresh,
        onMenuClick = onMenuClick,
        modifier = modifier
    )
}

@Composable
fun MenuScreen(
    items: List<Menu>,
    refresh: RefreshMenuState,
    showError: Boolean,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMenuClick: (Menu) -> Unit,
    modifier: Modifier = Modifier
) {
    val refreshing = refresh is RefreshMenuState.Loading || refresh is RefreshMenuState.Idle
    val errorMessage = (refresh as? RefreshMenuState.Error)?.message
    val toast = errorMessage?.takeIf { showError }?.let { ToastMessage(ToastType.Error, it) }

    NavigationBarStyle(darkIcons = true)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding()
        ) {
            MenuTopBar(onBack = onBack)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
            ) {
                if (refreshing && items.isNotEmpty()) {
                    LinearProgressIndicator(
                        color = ColorPrimary,
                        trackColor = ColorPrimary.copy(alpha = 0.15f),
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            when {
                items.isNotEmpty() -> MenuList(items = items, onMenuClick = onMenuClick)
                refreshing -> MenuSkeleton()
                else -> MenuEmpty(
                    message = errorMessage ?: "No hay opciones disponibles",
                    onRetry = onRetry
                )
            }
        }

        ToastHost(
            toast = toast,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun MenuTopBar(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            onClick = onBack,
            shape = CircleShape,
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.size(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFF0F0F0F)
                )
            }
        }
        Text(
            text = "Menú",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F0F0F)
        )
    }
}

@Composable
private fun MenuList(
    items: List<Menu>,
    onMenuClick: (Menu) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = items, key = { it.key }) { item ->
            MenuItemCard(item = item, onClick = { onMenuClick(item) })
        }
    }
}

@Composable
private fun MenuItemCard(
    item: Menu,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(ColorPrimary.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = menuIcon(item.icon),
                    contentDescription = null,
                    tint = ColorPrimary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Text(
                text = item.text,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF0F0F0F),
                modifier = Modifier.weight(1f)
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFF9AA0A6)
            )
        }
    }
}

@Composable
private fun MenuSkeleton() {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.45f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 700), RepeatMode.Reverse),
        label = "skeletonAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(SKELETON_ITEMS) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .alpha(alpha)
                    .background(Color(0xFFE2E2E2), RoundedCornerShape(16.dp))
            )
        }
    }
}

@Composable
private fun MenuEmpty(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No pudimos mostrar el menú",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0F0F0F)
        )
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B6B6B),
            textAlign = TextAlign.Center
        )
        PrimaryButton(text = "Reintentar", onClick = onRetry)
    }
}

private fun menuIcon(icon: String): ImageVector = when (icon) {
    "home" -> Icons.Filled.Home
    "profile" -> Icons.Filled.Person
    "history" -> Icons.Filled.DateRange
    "support" -> Icons.Filled.Info
    else -> Icons.Filled.Star
}

private val previewItems = listOf(
    Menu("passenger_home", "Pedir taxi", "home", "app-taxi://passenger/home", 1),
    Menu("passenger_profile", "Mi perfil", "profile", "app-taxi://passenger/profile", 2),
    Menu("passenger_historic", "Mis viajes", "history", "app-taxi://passenger/historic", 3),
    Menu("passenger_support", "Ayuda", "support", "app-taxi://passenger/support", 4)
)

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MenuScreenPreviewLoading() {
    MenuScreen(
        items = emptyList(),
        refresh = RefreshMenuState.Loading,
        showError = false,
        onBack = {},
        onRetry = {},
        onMenuClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MenuScreenPreviewCached() {
    MenuScreen(
        items = previewItems,
        refresh = RefreshMenuState.Loading,
        showError = false,
        onBack = {},
        onRetry = {},
        onMenuClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MenuScreenPreviewSuccess() {
    MenuScreen(
        items = previewItems,
        refresh = RefreshMenuState.Success,
        showError = false,
        onBack = {},
        onRetry = {},
        onMenuClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MenuScreenPreviewOffline() {
    MenuScreen(
        items = previewItems,
        refresh = RefreshMenuState.Error("No se pudo conectar con el servidor. Revisa tu conexión"),
        showError = true,
        onBack = {},
        onRetry = {},
        onMenuClick = {}
    )
}

@Preview(showBackground = true, backgroundColor = 0xFFF0F0F0)
@Composable
private fun MenuScreenPreviewError() {
    MenuScreen(
        items = emptyList(),
        refresh = RefreshMenuState.Error("No se pudo conectar con el servidor. Revisa tu conexión"),
        showError = true,
        onBack = {},
        onRetry = {},
        onMenuClick = {}
    )
}
