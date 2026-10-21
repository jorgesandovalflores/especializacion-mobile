package com.example.android.features.home.presentation

import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.core.presentation.theme.ColorError
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.UserLocation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val WELCOME_TOAST_MS = 3_000L
private const val CAMERA_ANIMATION_MS = 800
private const val DEFAULT_ZOOM = 12f
private const val USER_ZOOM = 16f
private val LIMA = LatLng(-12.0464, -77.0428)
private val ColorText = Color(0xFF0F0F0F)
private val ColorTextSecondary = Color(0xFF6B6B6B)
private val ColorMapLand = Color(0xFFEDEBE6)

@Composable
fun HomeRoute(
    onOpenMenu: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val mapUi by vm.mapUi.collectAsStateWithLifecycle()
    var showWelcome by rememberSaveable { mutableStateOf(true) }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val canAskAgain = activity?.shouldShowRequestPermissionRationale(LOCATION_PERMISSIONS.first()) == true
        vm.onPermissionResult(result.toLocationPermission(canAskAgain))
    }

    LifecycleResumeEffect(Unit) {
        vm.onPermissionChecked(context.currentLocationPermission())
        onPauseOrDispose { }
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { }
    var askedNotifications by rememberSaveable { mutableStateOf(false) }
    val tracking = mapUi is MapUiState.Success

    LaunchedEffect(tracking) {
        if (tracking && !askedNotifications && context.needsNotificationPermission()) {
            askedNotifications = true
            notificationLauncher.launch(NOTIFICATION_PERMISSION)
        }
    }

    LaunchedEffect(showWelcome) {
        if (showWelcome) {
            delay(WELCOME_TOAST_MS)
            showWelcome = false
        }
    }

    HomeScreen(
        state = mapUi,
        showWelcome = showWelcome,
        onOpenMenu = onOpenMenu,
        onLogout = { vm.logout(onLoggedOut) },
        onRequestPermission = { permissionLauncher.launch(LOCATION_PERMISSIONS) },
        onOpenAppSettings = {
            context.startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
            )
        },
        onOpenLocationSettings = {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        },
        onRetry = vm::retry,
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    state: MapUiState,
    showWelcome: Boolean,
    onOpenMenu: () -> Unit,
    onLogout: () -> Unit,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBarStyle(darkIcons = true)

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LIMA, DEFAULT_ZOOM)
    }
    var centered by rememberSaveable { mutableStateOf(false) }
    var panelHeight by remember { mutableStateOf(0.dp) }
    val success = state as? MapUiState.Success

    LaunchedEffect(success != null) {
        if (success != null && !centered) {
            camera.centerOn(success.location)
            centered = true
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ColorMapLand)
    ) {
        HomeMap(
            camera = camera,
            location = success?.location,
            bottomPadding = panelHeight
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MapCircleButton(
                icon = Icons.Filled.Menu,
                contentDescription = "Abrir menú",
                onClick = onOpenMenu,
                containerColor = ColorPrimary,
                contentColor = Color.White
            )
            MapCircleButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = "Cerrar sesión",
                onClick = onLogout
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .onSizeChanged { panelHeight = with(density) { it.height.toDp() } },
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = success != null,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                MapCircleButton(
                    icon = Icons.Filled.LocationOn,
                    contentDescription = "Centrar en mi ubicación",
                    onClick = { success?.let { scope.launch { camera.centerOn(it.location) } } },
                    modifier = Modifier
                        .navigationBarsPadding()
                        .padding(end = 16.dp, bottom = 16.dp)
                )
            }
            AnimatedVisibility(
                visible = success == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut()
            ) {
                LocationPanel(
                    state = state,
                    onRequestPermission = onRequestPermission,
                    onOpenAppSettings = onOpenAppSettings,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onRetry = onRetry
                )
            }
        }

        ToastHost(
            toast = if (showWelcome) ToastMessage(ToastType.Success, "Sesión iniciada correctamente") else null,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun HomeMap(
    camera: CameraPositionState,
    location: UserLocation?,
    bottomPadding: Dp
) {
    if (LocalInspectionMode.current) {
        MapPreviewBackground(showUser = location != null)
        return
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        contentPadding = PaddingValues(bottom = bottomPadding),
        mapColorScheme = ComposeMapColorScheme.LIGHT
    ) {
        if (location != null) {
            val position = LatLng(location.latitude, location.longitude)
            Circle(
                center = position,
                radius = location.accuracyMeters.toDouble(),
                fillColor = ColorPrimary.copy(alpha = 0.15f),
                strokeColor = ColorPrimary.copy(alpha = 0.5f),
                strokeWidth = 2f
            )
            MarkerComposable(
                state = rememberUpdatedMarkerState(position),
                anchor = Offset(0.5f, 0.5f),
                contentDescription = "Tu ubicación"
            ) {
                UserDot()
            }
        }
    }
}

@Composable
private fun UserDot() {
    Box(
        modifier = Modifier
            .size(22.dp)
            .background(ColorPrimary, CircleShape)
            .border(4.dp, Color.White, CircleShape)
    )
}

@Composable
private fun MapPreviewBackground(showUser: Boolean) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(ColorMapLand)
            val road = Color.White
            drawLine(road, Offset(0f, size.height * 0.30f), Offset(size.width, size.height * 0.22f), strokeWidth = 28f)
            drawLine(road, Offset(0f, size.height * 0.62f), Offset(size.width, size.height * 0.70f), strokeWidth = 20f)
            drawLine(road, Offset(size.width * 0.28f, 0f), Offset(size.width * 0.36f, size.height), strokeWidth = 22f)
            drawLine(road, Offset(size.width * 0.74f, 0f), Offset(size.width * 0.66f, size.height), strokeWidth = 16f)
            drawCircle(Color(0xFFCDE8C8), radius = size.width * 0.12f, center = Offset(size.width * 0.84f, size.height * 0.42f))
            if (showUser) {
                drawCircle(ColorPrimary.copy(alpha = 0.15f), radius = size.width * 0.16f, center = center)
            }
        }
        if (showUser) UserDot()
    }
}

@Composable
private fun MapCircleButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    contentColor: Color = ColorPrimary
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = containerColor,
        contentColor = contentColor,
        shadowElevation = 6.dp,
        modifier = modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = contentDescription)
        }
    }
}

@Composable
private fun LocationPanel(
    state: MapUiState,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        color = Color.White,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        shadowElevation = 12.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { it / 3 } + fadeOut())
            },
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.navigationBars)
                .padding(horizontal = 24.dp, vertical = 24.dp),
            label = "location-panel"
        ) { current ->
            when (current) {
                MapUiState.Idle -> IdlePanel()
                is MapUiState.Permission -> PermissionPanel(
                    blocked = current.blocked,
                    onRequestPermission = onRequestPermission,
                    onOpenAppSettings = onOpenAppSettings
                )
                is MapUiState.Success -> Unit
                is MapUiState.Error -> ErrorPanel(
                    error = current.error,
                    onRequestPermission = onRequestPermission,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onRetry = onRetry
                )
            }
        }
    }
}

@Composable
private fun IdlePanel() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            color = ColorPrimary,
            strokeWidth = 3.dp,
            modifier = Modifier.size(28.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PanelTitle("Buscando tu ubicación…")
            PanelBody("El GPS puede tardar unos segundos en encontrarte")
        }
    }
}

@Composable
private fun PermissionPanel(
    blocked: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(
            icon = if (blocked) Icons.Filled.Lock else Icons.Filled.LocationOn,
            tint = ColorPrimary,
            title = if (blocked) "Permiso de ubicación bloqueado" else "Activa tu ubicación",
            body = if (blocked) {
                "Actívalo en Ajustes › Permisos › Ubicación para verte en el mapa"
            } else {
                "La usamos para mostrarte en el mapa y enviarte el taxi más cercano"
            }
        )
        PrimaryButton(
            text = if (blocked) "Abrir ajustes" else "Permitir ubicación",
            onClick = if (blocked) onOpenAppSettings else onRequestPermission
        )
    }
}

@Composable
private fun ErrorPanel(
    error: LocationError,
    onRequestPermission: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRetry: () -> Unit
) {
    val (title, body, action) = when (error) {
        LocationError.SettingsDisabled -> Triple(
            "La ubicación del teléfono está apagada",
            "Enciéndela para encontrarte en el mapa",
            "Activar ubicación"
        )
        LocationError.PermissionMissing -> Triple(
            "Sin permiso de ubicación",
            "Vuelve a otorgarlo para verte en el mapa",
            "Permitir ubicación"
        )
        LocationError.Unavailable -> Triple(
            "No pudimos encontrarte",
            "Revisa tu señal o acércate a una ventana e inténtalo otra vez",
            "Reintentar"
        )
    }
    val onAction = when (error) {
        LocationError.SettingsDisabled -> onOpenLocationSettings
        LocationError.PermissionMissing -> onRequestPermission
        LocationError.Unavailable -> onRetry
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(icon = Icons.Filled.Warning, tint = ColorError, title = title, body = body)
        PrimaryButton(text = action, onClick = onAction)
    }
}

@Composable
private fun PanelHeader(
    icon: ImageVector,
    tint: Color,
    title: String,
    body: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(tint.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = tint)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PanelTitle(title)
            PanelBody(body)
        }
    }
}

@Composable
private fun PanelTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = ColorText
    )
}

@Composable
private fun PanelBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = ColorTextSecondary
    )
}

private suspend fun CameraPositionState.centerOn(location: UserLocation) {
    animate(
        CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), USER_ZOOM),
        CAMERA_ANIMATION_MS
    )
}

private val previewLocation = UserLocation(
    latitude = -12.09713,
    longitude = -77.03654,
    accuracyMeters = 12f,
    timeMillis = 0L,
    isMock = false
)

@Composable
private fun HomeScreenPreview(state: MapUiState) {
    HomeScreen(
        state = state,
        showWelcome = false,
        onOpenMenu = {},
        onLogout = {},
        onRequestPermission = {},
        onOpenAppSettings = {},
        onOpenLocationSettings = {},
        onRetry = {}
    )
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewIdle() {
    HomeScreenPreview(MapUiState.Idle)
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewPermission() {
    HomeScreenPreview(MapUiState.Permission(blocked = false))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewPermissionBlocked() {
    HomeScreenPreview(MapUiState.Permission(blocked = true))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewSuccess() {
    HomeScreenPreview(MapUiState.Success(previewLocation, precise = true))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewApproximate() {
    HomeScreenPreview(MapUiState.Success(previewLocation.copy(accuracyMeters = 1_200f), precise = false))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewSettingsOff() {
    HomeScreenPreview(MapUiState.Error(LocationError.SettingsDisabled))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewUnavailable() {
    HomeScreenPreview(MapUiState.Error(LocationError.Unavailable))
}
