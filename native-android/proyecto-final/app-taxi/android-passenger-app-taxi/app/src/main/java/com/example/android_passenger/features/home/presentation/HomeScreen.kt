package com.example.android_passenger.features.home.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.android_passenger.commons.presentation.NavigationBarStyle
import com.example.android_passenger.core.presentation.theme.ColorPrimary
import com.example.android_passenger.core.presentation.utils.LocationHelper
import com.example.android_passenger.core.presentation.utils.centerOn
import com.example.android_passenger.core.presentation.utils.rememberLocationPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch
import com.example.android_passenger.commons.presentation.ComponentPinLocationUser
import com.example.android_passenger.commons.domain.usecase.GetPassengerLocalState
import com.example.android_passenger.features.home.domain.model.AlertHome
import com.example.android_passenger.features.home.domain.usecase.AlertHomeFirestoreUseCaseState

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onMenuClick: () -> Unit = {},
    previewAlertHome: AlertHome? = null,
    previewShowAlert: Boolean = false
) {
    val bg = Color(0xFFF4F5F6)
    NavigationBarStyle(color = bg, darkIcons = true)

    BackHandler { onBackPressed() }

    // Estado de permisos de ubicación
    val locationPermissionState = rememberLocationPermissionState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ===== OBTENER photoUrl DESDE HomeViewModel =====
    val homeViewModel: HomeViewModel? = if (previewAlertHome == null) hiltViewModel() else null
    val userState by homeViewModel?.userUi?.collectAsState() ?: remember { mutableStateOf(GetPassengerLocalState.Idle) }

    // ===== OBTENER ALERTA DESDE FIRESTORE =====
    val alertHomeState by homeViewModel?.alertHomeState?.collectAsState() ?: remember {
        mutableStateOf(AlertHomeFirestoreUseCaseState.Success(previewAlertHome))
    }
    val showAlert by homeViewModel?.showAlert?.collectAsState() ?: remember {
        mutableStateOf(previewShowAlert && previewAlertHome?.isValid == true)
    }

    LaunchedEffect(Unit) {
        if (previewAlertHome == null) {
            homeViewModel?.callGetUser()
            homeViewModel?.callGetAlertHome()
        }
    }

    val userPhotoUrl: String? = when (val s = userState) {
        is GetPassengerLocalState.Success -> s.value.photoUrl
        else -> null
    }

    var hasAttemptedLocation by remember { mutableStateOf(false) }

    val hasPermissionForPreview = previewAlertHome != null
    val hasLocationPermission = if (previewAlertHome == null) {
        locationPermissionState.hasPermission
    } else {
        true // Para preview, simular permisos concedidos
    }

    LaunchedEffect(Unit) {
        if (previewAlertHome == null && !locationPermissionState.hasPermission) {
            locationPermissionState.requestPermission()
        }
    }

    LaunchedEffect(hasLocationPermission) {
        if (hasLocationPermission && !hasAttemptedLocation) {
            hasAttemptedLocation = true
            kotlinx.coroutines.delay(500)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        if (hasLocationPermission) {
            MapContent(
                shouldCenterOnUserLocation = !hasAttemptedLocation,
                onLocationCentered = { hasAttemptedLocation = true },
                onMenuClick = onMenuClick,
                userPhotoUrl = userPhotoUrl
            )
        } else {
            PermissionRequestScreen(
                onRequestPermission = {
                    if (previewAlertHome == null) {
                        locationPermissionState.requestPermission()
                    }
                },
                onMenuClick = onMenuClick
            )
        }

        if (showAlert && alertHomeState is AlertHomeFirestoreUseCaseState.Success) {
            val alertHome = (alertHomeState as AlertHomeFirestoreUseCaseState.Success).alertHome
            alertHome?.let { alert ->
                AlertHomeBanner(
                    alertHome = alert,
                    onDismiss = { homeViewModel?.dismissAlert() },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 36.dp)
                )
            }
        }
    }
}

@Composable
fun HomeScreenRoute(
    onNavigateToMenu: () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
) {
    HomeScreen(
        modifier = modifier,
        onMenuClick = onNavigateToMenu,
        onBackPressed = onBackPressed
    )
}

@Composable
private fun MapContent(
    shouldCenterOnUserLocation: Boolean,
    onLocationCentered: () -> Unit,
    onMenuClick: () -> Unit,
    userPhotoUrl: String? // <-- nuevo parámetro
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Ubicación por defecto (Lima)
    val defaultLocation = LatLng(-12.0464, -77.0428)
    val userLocation = remember { mutableStateOf<LatLng?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 12f)
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            compassEnabled = true,
            myLocationButtonEnabled = false
        )
    }

    val mapProperties = remember {
        MapProperties(
            mapType = MapType.NORMAL,
            isMyLocationEnabled = true
        )
    }

    LaunchedEffect(shouldCenterOnUserLocation) {
        if (shouldCenterOnUserLocation) {
            scope.launch {
                val currentLatLng = LocationHelper.getCurrentLatLng(context)

                if (currentLatLng != null) {
                    userLocation.value = currentLatLng
                    cameraPositionState.centerOn(currentLatLng, 15f)
                    onLocationCentered()
                } else {
                    userLocation.value = defaultLocation
                    cameraPositionState.centerOn(defaultLocation, 12f)
                    onLocationCentered()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        )

        Box(
            modifier = Modifier
                .padding(top = 56.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            MenuIconButton(
                iconRes = com.example.android_passenger.R.drawable.feature_home_ic_menu,
                onClick = onMenuClick
            )
        }

        Box(
            modifier = Modifier
                .padding(top = 56.dp, end = 16.dp)
                .align(Alignment.TopEnd)
        ) {
            MenuIconButton(
                iconRes = android.R.drawable.ic_menu_mylocation,
                onClick = {
                    scope.launch {
                        val currentLatLng = LocationHelper.getCurrentLatLng(context)

                        if (currentLatLng != null) {
                            userLocation.value = currentLatLng
                            cameraPositionState.centerOn(currentLatLng, 15f)
                        } else {
                            val targetLocation = userLocation.value ?: defaultLocation
                            cameraPositionState.centerOn(targetLocation, 15f)
                        }
                    }
                }
            )
        }

        if (userLocation.value != null) {
            val profileSize = 56.dp
            val pinHeight = 18.dp
            val gap = 6.dp
            val pinOffsetY = -(profileSize / 2 + gap + pinHeight)

            ComponentPinLocationUser(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pinOffsetY),
                showAddress = false,
                photoUrl = userPhotoUrl,
                profileSize = profileSize,
                pinHeight = pinHeight
            )
        }

        if (shouldCenterOnUserLocation) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Obteniendo tu ubicación...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun PermissionRequestScreen(
    onRequestPermission: () -> Unit,
    onMenuClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF4F5F6))
    ) {
        Box(
            modifier = Modifier
                .padding(top = 56.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            MenuIconButton(
                iconRes = com.example.android_passenger.R.drawable.feature_home_ic_menu,
                onClick = onMenuClick
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(android.R.drawable.ic_dialog_map),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = ColorPrimary
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Permisos de Ubicación Requeridos",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Para mostrar tu ubicación en el mapa y proporcionarte una mejor experiencia, necesitamos acceso a tu ubicación.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(
                    containerColor = ColorPrimary
                )
            ) {
                Text("Conceder Permisos")
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = {
            }) {
                Text("Continuar sin ubicación")
            }
        }
    }
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
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.White,
            contentColor = ColorPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
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
            modifier = Modifier.size(22.dp)
        )
    }
}

@Composable
fun AlertHomeBanner(
    alertHome: AlertHome,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = alertHome.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar alerta"
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = alertHome.body,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun rememberAlertHomeForPreview(): AlertHome? {
    return remember {
        AlertHome(
            title = "Alerta Importante",
            body = "Este es un mensaje de alerta de ejemplo para mostrar en el preview."
        )
    }
}

@Composable
private fun rememberShowAlertForPreview(): Boolean {
    return remember { true }
}

// Extensión para centrar
private suspend fun com.google.maps.android.compose.CameraPositionState.centerOn(
    latLng: LatLng,
    zoom: Float = 15f
) {
    this.animate(
        com.google.android.gms.maps.CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(latLng, zoom)
        ),
        durationMs = 1000
    )
}

@Preview
@Composable
fun PreviewHomeScreen() {
    MaterialTheme {
        HomeScreen(
            onBackPressed = {},
            onMenuClick = {}
        )
    }
}

@Preview
@Composable
fun PreviewPermissionRequestScreen() {
    MaterialTheme {
        PermissionRequestScreen(
            onRequestPermission = {},
            onMenuClick = {}
        )
    }
}
