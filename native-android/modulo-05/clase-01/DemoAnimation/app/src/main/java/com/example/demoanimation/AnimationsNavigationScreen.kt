package com.example.demoanimation

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.composable
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

@OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AnimationsNavigationScreen() {
    val navController = rememberAnimatedNavController()
    var selectedAnimation by remember { mutableStateOf("Slide Horizontal") }
    var showDropdown by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Animaciones de Navegación",
                        fontWeight = FontWeight.Medium
                    )
                },
                navigationIcon = {
                    if (navController.currentBackStackEntry?.destination?.route != Routes.Home) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                        }
                    }
                },
                actions = {
                    Box {
                        OutlinedButton(
                            onClick = { showDropdown = true },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Text(
                                selectedAnimation,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            listOf(
                                "Slide Horizontal",
                                "Slide Vertical",
                                "Fade",
                                "Scale",
                                "Zoom + Fade",
                                "Slide + Fade",
                                "Spring",
                                "Combinada"
                            ).forEach { animation ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            animation,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    },
                                    onClick = {
                                        selectedAnimation = animation
                                        showDropdown = false
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Surface(
            modifier = Modifier.padding(paddingValues)
        ) {
            AnimatedNavHost(
                navController = navController,
                startDestination = Routes.Home,
                enterTransition = { getEnterTransition(selectedAnimation) },
                exitTransition = { getExitTransition(selectedAnimation) },
                popEnterTransition = { getPopEnterTransition(selectedAnimation) },
                popExitTransition = { getPopExitTransition(selectedAnimation) }
            ) {
                composable(Routes.Home) {
                    HomeScreen(
                        onGoToDetails = { navController.navigate(Routes.Details) },
                        onGoToSettings = { navController.navigate(Routes.Settings) },
                        onGoToProfile = { navController.navigate(Routes.Profile) },
                        onGoToList = { navController.navigate(Routes.List) }
                    )
                }

                composable(Routes.Details) {
                    DetailsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.Settings) {
                    SettingsScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.Profile) {
                    ProfileScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(Routes.List) {
                    ListScreen(
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
private fun getEnterTransition(animationType: String) = when (animationType) {
    "Slide Horizontal" -> slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide Vertical" -> slideInVertically(
        initialOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Fade" -> fadeIn(animationSpec = tween(durationMillis = 400))
    "Scale" -> scaleIn(
        initialScale = 0.8f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Zoom + Fade" -> fadeIn(animationSpec = tween(300)) + scaleIn(
        initialScale = 0.85f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide + Fade" -> slideInHorizontally(
        initialOffsetX = { it / 2 },
        animationSpec = tween(durationMillis = 400)
    ) + fadeIn(animationSpec = tween(durationMillis = 400))
    "Spring" -> slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    "Combinada" -> slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(durationMillis = 400)
    ) + fadeIn(animationSpec = tween(durationMillis = 500)) + scaleIn(
        initialScale = 0.9f,
        animationSpec = tween(durationMillis = 400)
    )
    else -> fadeIn(animationSpec = tween(durationMillis = 300))
}

@OptIn(ExperimentalAnimationApi::class)
private fun getExitTransition(animationType: String) = when (animationType) {
    "Slide Horizontal" -> slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 2 },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide Vertical" -> slideOutVertically(
        targetOffsetY = { fullHeight -> -fullHeight / 2 },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Fade" -> fadeOut(animationSpec = tween(durationMillis = 300))
    "Scale" -> scaleOut(
        targetScale = 1.2f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Zoom + Fade" -> fadeOut(animationSpec = tween(300)) + scaleOut(
        targetScale = 1.15f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide + Fade" -> slideOutHorizontally(
        targetOffsetX = { -it / 2 },
        animationSpec = tween(durationMillis = 400)
    ) + fadeOut(animationSpec = tween(durationMillis = 400))
    "Spring" -> slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        )
    )
    "Combinada" -> slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(durationMillis = 400)
    ) + fadeOut(animationSpec = tween(durationMillis = 300)) + scaleOut(
        targetScale = 1.1f,
        animationSpec = tween(durationMillis = 400)
    )
    else -> fadeOut(animationSpec = tween(durationMillis = 300))
}

@OptIn(ExperimentalAnimationApi::class)
private fun getPopEnterTransition(animationType: String) = when (animationType) {
    "Slide Horizontal" -> slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 2 },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide Vertical" -> slideInVertically(
        initialOffsetY = { fullHeight -> -fullHeight / 2 },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Fade" -> fadeIn(animationSpec = tween(durationMillis = 400))
    "Scale" -> scaleIn(
        initialScale = 1.2f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    else -> getEnterTransition(animationType)
}

@OptIn(ExperimentalAnimationApi::class)
private fun getPopExitTransition(animationType: String) = when (animationType) {
    "Slide Horizontal" -> slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Slide Vertical" -> slideOutVertically(
        targetOffsetY = { fullHeight -> fullHeight },
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    "Fade" -> fadeOut(animationSpec = tween(durationMillis = 300))
    "Scale" -> scaleOut(
        targetScale = 0.8f,
        animationSpec = tween(durationMillis = 400, easing = EaseInOut)
    )
    else -> getExitTransition(animationType)
}

@Composable
fun HomeScreen(
    onGoToDetails: () -> Unit,
    onGoToSettings: () -> Unit,
    onGoToProfile: () -> Unit,
    onGoToList: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Pantalla Principal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Selecciona una animación en el menú superior y navega para ver los diferentes efectos de transición",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            NavigationButton(
                text = "Detalles del Pedido",
                icon = Icons.Default.Info,
                onClick = onGoToDetails
            )

            NavigationButton(
                text = "Configuración",
                icon = Icons.Default.Settings,
                onClick = onGoToSettings
            )

            NavigationButton(
                text = "Perfil de Usuario",
                icon = Icons.Default.Person,
                onClick = onGoToProfile
            )

            NavigationButton(
                text = "Lista de Elementos",
                icon = Icons.Default.List,
                onClick = onGoToList
            )
        }
    }
}

@Composable
fun NavigationButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.8f),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.size(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun DetailsScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Detalles del Pedido",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    "Información del Pedido",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                DetailRow("Estado:", "En camino")
                DetailRow("Número de pedido:", "#ORD-2847")
                DetailRow("Estimado de entrega:", "30 minutos")
                DetailRow("Dirección:", "Av. Principal 123")
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(text = "Volver a Principal")
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkThemeEnabled by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.Top),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Configuración",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            SettingItem(
                title = "Notificaciones",
                description = "Recibir notificaciones push",
                enabled = notificationsEnabled,
                onEnabledChange = { notificationsEnabled = it }
            )

            SettingItem(
                title = "Tema Oscuro",
                description = "Activar interfaz en modo oscuro",
                enabled = darkThemeEnabled,
                onEnabledChange = { darkThemeEnabled = it }
            )

            SimpleSettingItem("Idioma", "Español")
            SimpleSettingItem("Privacidad", "Configurar")
            SimpleSettingItem("Acerca de", "Versión 1.0.0")
        }

        Button(
            onClick = onBack,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(text = "Volver")
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    description: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
    }
}

@Composable
fun SimpleSettingItem(title: String, value: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ProfileScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Person,
                contentDescription = "Perfil",
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Juan Pérez",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "juan.perez@email.com",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = "Usuario Premium",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }

        Button(onClick = onBack) {
            Text(text = "Volver a Principal")
        }
    }
}

@Composable
fun ListScreen(onBack: () -> Unit) {
    val items = List(20) { "Elemento ${it + 1}" }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "Lista de Elementos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(24.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(12.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }

        Button(
            onClick = onBack,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(text = "Volver a Principal")
        }
    }
}

object Routes {
    const val Home = "home"
    const val Details = "details"
    const val Settings = "settings"
    const val Profile = "profile"
    const val List = "list"
}