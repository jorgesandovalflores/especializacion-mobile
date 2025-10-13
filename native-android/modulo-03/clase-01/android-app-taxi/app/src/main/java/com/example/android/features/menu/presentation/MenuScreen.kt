package com.example.android.features.menu.presentation

import com.example.android.R
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import com.example.android.commons.domain.model.Passenger
import com.example.android.commons.domain.enum.PassengerStatusEnum
import com.example.android.commons.domain.usecase.GetPassengerLocalState
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.usecase.GetMenuCacheState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MenuTopBar(
    @DrawableRes navIconRes: Int,
    onNavClick: () -> Unit
) {
    TopAppBar(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = { Text(text = "", style = MaterialTheme.typography.titleLarge) },
        navigationIcon = {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .size(40.dp)
                    .semantics { contentDescription = "menu_nav_left" },
                onClick = onNavClick
            ) {
                Icon(
                    painter = painterResource(navIconRes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(24.dp)

                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@Composable
fun PassengerCard(
    passenger: Passenger?,
    loading: Boolean,
    modifier: Modifier = Modifier
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val shimmerBase = Color.LightGray.copy(alpha = 0.3f)
    val shimmerHighlight = Color.White.copy(alpha = 0.6f)

    // Animación shimmer infinita
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmerAnim"
    )

    val brush = Brush.linearGradient(
        colors = listOf(shimmerBase, shimmerHighlight, shimmerBase),
        start = androidx.compose.ui.geometry.Offset(translateAnim - 200f, 0f),
        end = androidx.compose.ui.geometry.Offset(translateAnim, 200f)
    )

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = surfaceColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (loading) {
                // Avatar skeleton
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            } else {
                // Avatar real
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = passenger?.givenName?.firstOrNull()?.uppercase() ?: "U",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                if (loading) {
                    Spacer(
                        modifier = Modifier
                            .height(16.dp)
                            .fillMaxWidth(0.5f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(8.dp))
                    Spacer(
                        modifier = Modifier
                            .height(12.dp)
                            .fillMaxWidth(0.4f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                    Spacer(Modifier.height(6.dp))
                    Spacer(
                        modifier = Modifier
                            .height(10.dp)
                            .fillMaxWidth(0.3f)
                            .clip(RoundedCornerShape(4.dp))
                            .background(brush)
                    )
                } else {
                    val fullName = listOfNotNull(passenger?.givenName, passenger?.familyName)
                        .joinToString(" ")
                        .ifBlank { "Passenger" }

                    Text(
                        text = fullName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = passenger?.phoneNumber ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    passenger?.email?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (loading) {
                Spacer(
                    modifier = Modifier
                        .width(40.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            } else {
                Text(
                    text = passenger?.status ?: "",
                    style = MaterialTheme.typography.labelMedium,
                    color = when (passenger?.status?.lowercase()) {
                        PassengerStatusEnum.ACTIVE.value -> MaterialTheme.colorScheme.primary
                        PassengerStatusEnum.SUSPENDED.value,
                        PassengerStatusEnum.INACTIVE_REGISTER.value -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun MenuItemCard(
    item: Menu,
    @DrawableRes rightArrowRes: Int,
    onClick: (Menu) -> Unit
) {
    val context = LocalContext.current

    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }

    val shape = RoundedCornerShape(14.dp)

    Card(
        onClick = { onClick(item) },                 // ← usa el Card clickable de M3
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(item.iconUrl)
                    .crossfade(true)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = item.text,
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray.copy(alpha = 0.3f)),
                error = painterResource(android.R.drawable.ic_menu_report_image),
                placeholder = painterResource(android.R.drawable.ic_menu_gallery)
            )

            Spacer(Modifier.width(12.dp))

            Text(
                text = item.text,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )

            Icon(
                painter = painterResource(rightArrowRes),
                contentDescription = "Open",
                modifier = Modifier
                    .size(20.dp)
                    .semantics { contentDescription = "menu_right_${item.key}" },
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MenuScreen(
    onNavClick: () -> Unit,
    onMenuClick: (Menu) -> Unit,
    passengerLocalState: GetPassengerLocalState,
    menuCacheState: GetMenuCacheState
) {
    val bg = Color(0xFFF4F5F6)
    NavigationBarStyle(color = bg, darkIcons = true)

    // Estados derivados
    val loadingPassenger = passengerLocalState is GetPassengerLocalState.Loading
    val loadingMenu = menuCacheState is GetMenuCacheState.Loading

    val passenger = (passengerLocalState as? GetPassengerLocalState.Success)?.value
    val menus = (menuCacheState as? GetMenuCacheState.Success)?.items ?: emptyList()
    val errorMenu = (menuCacheState as? GetMenuCacheState.Error)?.message

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(bg),
        color = bg
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            MenuTopBar(
                navIconRes = R.drawable.feature_menu_ic_left,
                onNavClick = onNavClick
            )

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                // --- Passenger ---
                item {
                    PassengerCard(
                        passenger = passenger,
                        loading = loadingPassenger,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp, bottom = 16.dp)
                    )
                }

                // --- Menús ---
                when {
                    loadingMenu -> {
                        // Mostrar skeletons
                        items(4) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.LightGray.copy(alpha = 0.3f))
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    menus.isNotEmpty() -> {
                        items(
                            items = menus.sortedBy { it.order },
                            key = { it.key }
                        ) { menu ->
                            MenuItemCard(
                                item = menu,
                                rightArrowRes = R.drawable.feature_menu_ic_right,
                                onClick = onMenuClick
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                        }
                    }
                    errorMenu != null -> {
                        item {
                            Text(
                                text = errorMenu,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                    else -> {
                        item {
                            Text(
                                text = "No hay opciones disponibles",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MenuScreenRoute(
    onNavClick: () -> Unit,
    onMenuClick: (Menu) -> Unit,
    vm: MenuViewModel = hiltViewModel()
) {
    // Estados del ViewModel
    val passengerState by vm.userUi.collectAsState()
    val menuState by vm.menuUi.collectAsState()

    LaunchedEffect (Unit) {
        vm.callGetUser()
        vm.callGetMenu()
    }

    // Render de UI pura
    MenuScreen(
        onNavClick = onNavClick,
        onMenuClick = onMenuClick,
        passengerLocalState = passengerState,
        menuCacheState = menuState
    )
}


/* -------------------------------------------------------
   Preview: Pantallas y estados
-------------------------------------------------------- */
@Preview(name = "MenuScreen - Success", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMenuScreenSuccess() {
    MaterialTheme {
        MenuScreen(
            onNavClick = {},
            onMenuClick = {},
            passengerLocalState = GetPassengerLocalState.Success(
                Passenger(
                    id = "1",
                    phoneNumber = "51987654321",
                    givenName = "Jorge",
                    familyName = "Sandoval",
                    email = "jorge@example.com",
                    photoUrl = null,
                    status = "active"
                )
            ),
            menuCacheState = GetMenuCacheState.Success(
                listOf(
                    Menu("history", "Mis viajes", "ic_menu_history", "app://history", 1),
                    Menu("payments", "Pagos", "ic_menu_payments", "app://payments", 2),
                    Menu("support", "Ayuda", "ic_menu_support", "app://support", 3)
                )
            )
        )
    }
}

@Preview(name = "MenuScreen - Loading", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMenuScreenLoading() {
    MaterialTheme {
        MenuScreen(
            onNavClick = {},
            onMenuClick = {},
            passengerLocalState = GetPassengerLocalState.Loading,
            menuCacheState = GetMenuCacheState.Loading
        )
    }
}

@Preview(name = "MenuScreen - Error", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewMenuScreenError() {
    MaterialTheme {
        MenuScreen(
            onNavClick = {},
            onMenuClick = {},
            passengerLocalState = GetPassengerLocalState.Error("No se pudo cargar pasajero"),
            menuCacheState = GetMenuCacheState.Error("No se pudo obtener el menú")
        )
    }
}
