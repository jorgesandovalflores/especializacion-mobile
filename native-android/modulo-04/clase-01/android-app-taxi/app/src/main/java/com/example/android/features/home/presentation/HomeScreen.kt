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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.core.presentation.utils.LocationHelper
import com.example.android.core.presentation.utils.centerOn
import com.example.android.core.presentation.utils.rememberLocationPermissionState
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit = {},
    onMenuClick: () -> Unit = {}
) {
    val bg = Color(0xFFF4F5F6)
    NavigationBarStyle(color = bg, darkIcons = true)

    // Interceptar el botón back
    BackHandler { onBackPressed() }

    // Estado de permisos de ubicación
    val locationPermissionState = rememberLocationPermissionState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.LightGray)
    ) {
        if (locationPermissionState.hasPermission) {
            // Mostrar mapa si tenemos permisos
            MapContent(
                onMenuClick = onMenuClick
            )
        } else {
            // Mostrar pantalla de solicitud de permisos
            PermissionRequestScreen(
                onRequestPermission = locationPermissionState.requestPermission,
                onMenuClick = onMenuClick
            )
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
    onMenuClick: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = rememberCoroutineScope()

    val defaultLocation = LatLng(-12.0464, -77.0428) // Lima
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
            isMyLocationEnabled = true // ya validaste permisos arriba
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        )

        // Botón menú
        Box(
            modifier = Modifier
                .padding(top = 56.dp, start = 16.dp)
                .align(Alignment.TopStart)
        ) {
            MenuIconButton(
                iconRes = com.example.android.R.drawable.feature_home_ic_menu,
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
                        val latLng = LocationHelper
                            .getCurrentLatLng(context)

                        if (latLng != null) {
                            cameraPositionState.centerOn(latLng)
                        } else {
                            // TODO: opcional mostrar snackbar/toast indicando que no se obtuvo la ubicación
                        }
                    }
                }
            )
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
        // Botón de menú - margen top aumentado para respetar status bar
        Box(
            modifier = Modifier
                .padding(top = 56.dp, start = 16.dp) // Aumentado de 16dp a 56dp
                .align(Alignment.TopStart)
        ) {
            MenuIconButton(
                iconRes = com.example.android.R.drawable.feature_home_ic_menu,
                onClick = onMenuClick
            )
        }

        // Contenido de solicitud de permisos
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

            TextButton(onClick = { /* Usuario puede continuar sin permisos */ }) {
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