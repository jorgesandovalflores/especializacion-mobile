package com.example.android.features.home.presentation

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.android.R
import com.example.android.commons.presentation.MOTION_LONG_MS
import com.example.android.commons.presentation.NavigationBarStyle
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.commons.presentation.WidthClass
import com.example.android.commons.presentation.widthClassOf
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.UserLocation
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.presentation.TripActions
import com.example.android.features.trip.presentation.TripMapLayer
import com.example.android.features.trip.presentation.TripMapModel
import com.example.android.features.trip.presentation.TripMessage
import com.example.android.features.trip.presentation.TripPanel
import com.example.android.features.trip.presentation.TripUiState
import com.example.android.features.trip.presentation.TripViewModel
import com.example.android.features.trip.presentation.toMapModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
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
private const val PULSE_MS = 1_200
private val LIMA = LatLng(-12.0464, -77.0428)
private val PANEL_MAX_WIDTH = 420.dp
private val ROUTE_PADDING = 72.dp
private val ColorMapLand = Color(0xFFEDEBE6)
private val ColorMapLandDark = Color(0xFF242F3E)

@Composable
fun HomeRoute(
    onOpenMenu: () -> Unit,
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel(),
    tripVm: TripViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val mapUi by vm.mapUi.collectAsStateWithLifecycle()
    val trip by tripVm.ui.collectAsStateWithLifecycle()
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

    LaunchedEffect(trip.message) {
        if (trip.message != null) {
            delay(WELCOME_TOAST_MS)
            tripVm.messageShown()
        }
    }

    HomeScreen(
        state = mapUi,
        showWelcome = showWelcome,
        trip = trip,
        tripActions = TripActions(
            onRequest = tripVm::request,
            onClearDestination = tripVm::clearDestination,
            onAccept = tripVm::accept,
            onCancel = tripVm::cancel,
            onRate = tripVm::rate,
            onSkipRating = tripVm::skipRating
        ),
        onMapClick = { destination ->
            val origin = (mapUi as? MapUiState.Success)?.location?.let { GeoPoint(it.latitude, it.longitude) }
            tripVm.onMapTap(origin, destination)
        },
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
    modifier: Modifier = Modifier,
    trip: TripUiState = TripUiState(),
    tripActions: TripActions = TripActions(),
    onMapClick: (GeoPoint) -> Unit = {}
) {
    NavigationBarStyle()

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

    val route = trip.stage.toMapModel().route
    val inPreview = LocalInspectionMode.current
    val routePaddingPx = with(density) { ROUTE_PADDING.roundToPx() }
    LaunchedEffect(route) {
        if (route.size > 1 && !inPreview) camera.fitRoute(route, routePaddingPx)
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) ColorMapLandDark else ColorMapLand)
    ) {
        val wide = widthClassOf(maxWidth) != WidthClass.Compact

        HomeMap(
            camera = camera,
            location = success?.location,
            bottomPadding = panelHeight,
            trip = trip.stage.toMapModel(),
            onMapClick = onMapClick
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
                contentDescription = stringResource(R.string.home_open_menu),
                onClick = onOpenMenu,
                containerColor = ColorPrimary,
                contentColor = Color.White
            )
            MapCircleButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = stringResource(R.string.home_logout),
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
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                MapCircleButton(
                    icon = Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.home_center),
                    onClick = { success?.let { scope.launch { camera.centerOn(it.location) } } },
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                )
            }
            AnimatedVisibility(
                visible = success != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.Start)
            ) {
                TripPanel(stage = trip.stage, wide = wide, actions = tripActions)
            }
            AnimatedVisibility(
                visible = success == null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.Start)
            ) {
                LocationPanel(
                    state = state,
                    wide = wide,
                    onRequestPermission = onRequestPermission,
                    onOpenAppSettings = onOpenAppSettings,
                    onOpenLocationSettings = onOpenLocationSettings,
                    onRetry = onRetry
                )
            }
        }

        ToastHost(
            toast = trip.message?.toToast()
                ?: if (showWelcome) ToastMessage(ToastType.Success, stringResource(R.string.home_welcome)) else null,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun TripMessage.toToast(): ToastMessage = when (this) {
    is TripMessage.Error -> ToastMessage(ToastType.Error, text)
    TripMessage.Cancelled -> ToastMessage(ToastType.Error, stringResource(R.string.trip_cancelled))
    TripMessage.Rated -> ToastMessage(ToastType.Success, stringResource(R.string.trip_rated))
}

@Composable
private fun HomeMap(
    camera: CameraPositionState,
    location: UserLocation?,
    bottomPadding: Dp,
    trip: TripMapModel,
    onMapClick: (GeoPoint) -> Unit
) {
    if (LocalInspectionMode.current) {
        MapPreviewBackground(showUser = location != null)
        return
    }

    val accuracy by animateFloatAsState(
        targetValue = location?.accuracyMeters ?: 0f,
        animationSpec = tween(MOTION_LONG_MS),
        label = "accuracyRadius"
    )
    val markerDescription = stringResource(R.string.home_user_marker)

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = camera,
        uiSettings = MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            mapToolbarEnabled = false
        ),
        contentPadding = PaddingValues(bottom = bottomPadding),
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        onMapClick = { onMapClick(GeoPoint(it.latitude, it.longitude)) }
    ) {
        TripMapLayer(trip)
        if (location != null) {
            val position = LatLng(location.latitude, location.longitude)
            Circle(
                center = position,
                radius = accuracy.toDouble(),
                fillColor = ColorPrimary.copy(alpha = 0.15f),
                strokeColor = ColorPrimary.copy(alpha = 0.5f),
                strokeWidth = 2f
            )
            MarkerComposable(
                state = rememberUpdatedMarkerState(position),
                anchor = Offset(0.5f, 0.5f),
                contentDescription = markerDescription
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
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
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
    wide: Boolean,
    onRequestPermission: () -> Unit,
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit,
    onRetry: () -> Unit
) {
    val shape: Shape = if (wide) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val placement = if (wide) {
        Modifier
            .navigationBarsPadding()
            .padding(16.dp)
            .widthIn(max = PANEL_MAX_WIDTH)
    } else {
        Modifier.fillMaxWidth()
    }

    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = shape,
        shadowElevation = 12.dp,
        modifier = placement.semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        AnimatedContent(
            targetState = state,
            contentKey = { it::class },
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { it / 3 } + fadeOut())
            },
            modifier = Modifier
                .then(if (wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.navigationBars))
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
        SearchingPulse()
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PanelTitle(stringResource(R.string.home_searching_title))
            PanelBody(stringResource(R.string.home_searching_body))
        }
    }
}

@Composable
private fun SearchingPulse() {
    val transition = rememberInfiniteTransition(label = "searching")
    val pulse by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(PULSE_MS, easing = LinearOutSlowInEasing), RepeatMode.Restart),
        label = "pulse"
    )

    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .graphicsLayer {
                    scaleX = 0.4f + pulse * 0.6f
                    scaleY = 0.4f + pulse * 0.6f
                    alpha = 1f - pulse
                }
                .background(ColorPrimary.copy(alpha = 0.35f), CircleShape)
        )
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(ColorPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
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
            tint = MaterialTheme.colorScheme.primary,
            title = stringResource(if (blocked) R.string.home_blocked_title else R.string.home_permission_title),
            body = stringResource(if (blocked) R.string.home_blocked_body else R.string.home_permission_body)
        )
        PrimaryButton(
            text = stringResource(if (blocked) R.string.home_blocked_action else R.string.home_permission_action),
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
    val (title, body, action) = errorTexts(error)
    val onAction = when (error) {
        LocationError.SettingsDisabled -> onOpenLocationSettings
        LocationError.PermissionMissing -> onRequestPermission
        LocationError.Unavailable -> onRetry
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(
            icon = Icons.Filled.Warning,
            tint = MaterialTheme.colorScheme.error,
            title = stringResource(title),
            body = stringResource(body)
        )
        PrimaryButton(text = stringResource(action), onClick = onAction)
    }
}

private fun errorTexts(error: LocationError): Triple<Int, Int, Int> = when (error) {
    LocationError.SettingsDisabled -> Triple(
        R.string.home_settings_off_title,
        R.string.home_settings_off_body,
        R.string.home_settings_off_action
    )
    LocationError.PermissionMissing -> Triple(
        R.string.home_missing_title,
        R.string.home_missing_body,
        R.string.home_permission_action
    )
    LocationError.Unavailable -> Triple(
        R.string.home_unavailable_title,
        R.string.home_unavailable_body,
        R.string.common_retry
    )
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
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.semantics { heading() }
    )
}

@Composable
private fun PanelBody(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

private suspend fun CameraPositionState.fitRoute(route: List<GeoPoint>, paddingPx: Int) {
    val bounds = LatLngBounds.builder().apply { route.forEach { include(LatLng(it.lat, it.lng)) } }.build()
    runCatching { animate(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx), CAMERA_ANIMATION_MS) }
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
    AndroidTheme {
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

@Preview(name = "Horizontal", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun HomeScreenPreviewLandscape() {
    HomeScreenPreview(MapUiState.Permission(blocked = false))
}

@Preview(name = "Oscuro", showBackground = true, heightDp = 780, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewDark() {
    HomeScreenPreview(MapUiState.Error(LocationError.Unavailable))
}
