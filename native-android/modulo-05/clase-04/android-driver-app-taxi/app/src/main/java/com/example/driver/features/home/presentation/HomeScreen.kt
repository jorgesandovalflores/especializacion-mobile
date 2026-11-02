package com.example.driver.features.home.presentation

import android.content.Intent
import android.content.res.Configuration
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.driver.R
import com.example.driver.commons.domain.model.Driver
import com.example.driver.commons.presentation.NavigationBarStyle
import com.example.driver.commons.presentation.ToastHost
import com.example.driver.commons.presentation.ToastMessage
import com.example.driver.commons.presentation.ToastType
import com.example.driver.commons.presentation.WidthClass
import com.example.driver.commons.presentation.widthClassOf
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.core.presentation.theme.AndroidTheme
import com.example.driver.core.presentation.theme.ColorPrimary
import com.example.driver.core.presentation.theme.ColorSuccess
import com.example.driver.features.home.domain.model.GeoPoint
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.rules.PolylineDecoder
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MESSAGE_DURATION_MS = 3_000L
private const val CAMERA_ANIMATION_MS = 800
private const val DEFAULT_ZOOM = 12f
private const val USER_ZOOM = 16f
private val LIMA = LatLng(-12.0464, -77.0428)
private val PANEL_MAX_WIDTH = 440.dp
private val ROUTE_PADDING = 56.dp
private val ColorMapLand = Color(0xFFEDEBE6)
private val ColorMapLandDark = Color(0xFF242F3E)
private val ColorOrigin = Color(0xFF2E7D32)

@Composable
fun HomeRoute(
    onLoggedOut: () -> Unit,
    modifier: Modifier = Modifier,
    vm: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val state by vm.state.collectAsStateWithLifecycle()
    val mapUi by vm.mapUi.collectAsStateWithLifecycle()

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

    LaunchedEffect(state.tracking) {
        if (state.tracking && !askedNotifications && context.needsNotificationPermission()) {
            askedNotifications = true
            notificationLauncher.launch(NOTIFICATION_PERMISSION)
        }
    }

    LaunchedEffect(state.message) {
        if (state.message != null) {
            delay(MESSAGE_DURATION_MS)
            vm.consumeMessage()
        }
    }

    HomeScreen(
        state = state,
        mapUi = mapUi,
        actions = HomeActions(
            onLogout = { vm.logout(onLoggedOut) },
            onToggleAvailability = vm::toggleAvailability,
            onSelectRequest = vm::selectRequest,
            onCloseRequest = vm::closeRequest,
            onIncreasePrice = vm::increasePrice,
            onDecreasePrice = vm::decreasePrice,
            onSendOffer = vm::sendOffer,
            onAdvanceTrip = vm::advanceTrip,
            onCallPassenger = { phone ->
                context.startActivity(Intent(Intent.ACTION_DIAL, "tel:+$phone".toUri()))
            },
            onRate = vm::setRating,
            onRatingCommentChange = vm::setRatingComment,
            onSubmitRating = vm::submitRating,
            onRequestPermission = { permissionLauncher.launch(LOCATION_PERMISSIONS) },
            onOpenAppSettings = {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                )
            },
            onOpenLocationSettings = { context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            onRetryLocation = vm::retry
        ),
        modifier = modifier
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    mapUi: MapUiState,
    actions: HomeActions,
    modifier: Modifier = Modifier
) {
    NavigationBarStyle()

    val density = LocalDensity.current
    val scope = rememberCoroutineScope()
    val camera = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LIMA, DEFAULT_ZOOM)
    }
    var centered by rememberSaveable { mutableStateOf(false) }
    var panelHeight by remember { mutableStateOf(0.dp) }
    val location = (mapUi as? MapUiState.Success)?.location
    val trip = state.mapTrip

    LaunchedEffect(location != null) {
        if (location != null && !centered && trip == null) {
            camera.centerOn(location)
            centered = true
        }
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(if (isSystemInDarkTheme()) ColorMapLandDark else ColorMapLand)
    ) {
        val wide = widthClassOf(maxWidth) != WidthClass.Compact

        HomeMap(
            camera = camera,
            location = location,
            trip = trip,
            bottomPadding = panelHeight
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DriverHeader(
                driver = state.driver,
                connection = state.connection,
                modifier = Modifier
                    .weight(1f, fill = !wide)
                    .widthIn(max = PANEL_MAX_WIDTH)
            )
            if (wide) Box(modifier = Modifier.weight(1f))
            MapCircleButton(
                icon = Icons.AutoMirrored.Filled.ExitToApp,
                contentDescription = stringResource(R.string.home_logout),
                onClick = actions.onLogout
            )
        }

        Column(
            modifier = Modifier
                .align(if (wide) Alignment.BottomStart else Alignment.BottomCenter)
                .then(if (wide) Modifier.widthIn(max = PANEL_MAX_WIDTH + 32.dp) else Modifier.fillMaxWidth())
                .onSizeChanged { panelHeight = with(density) { it.height.toDp() } },
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(
                visible = location != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                MapCircleButton(
                    icon = Icons.Filled.LocationOn,
                    contentDescription = stringResource(R.string.home_center),
                    onClick = { location?.let { scope.launch { camera.centerOn(it) } } },
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                )
            }
            HomeBottomPanel(
                state = state,
                mapUi = mapUi,
                wide = wide,
                actions = actions
            )
        }

        ToastHost(
            toast = state.message?.let { toastFor(it) },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun toastFor(message: HomeMessage): ToastMessage = when (message) {
    is HomeMessage.Error -> ToastMessage(ToastType.Error, message.text)
    HomeMessage.OfferSent -> ToastMessage(ToastType.Success, stringResource(R.string.home_msg_offer_sent))
    HomeMessage.OfferAccepted -> ToastMessage(ToastType.Success, stringResource(R.string.home_msg_offer_accepted))
    HomeMessage.TripCancelled -> ToastMessage(ToastType.Error, stringResource(R.string.home_msg_trip_cancelled))
    HomeMessage.RequestTaken -> ToastMessage(ToastType.Error, stringResource(R.string.home_msg_request_taken))
    HomeMessage.RequestCancelled -> ToastMessage(ToastType.Error, stringResource(R.string.home_msg_request_cancelled))
    HomeMessage.RatingSent -> ToastMessage(ToastType.Success, stringResource(R.string.home_msg_rating_sent))
    HomeMessage.FinishTripFirst -> ToastMessage(ToastType.Error, stringResource(R.string.home_msg_finish_trip_first))
    HomeMessage.FinishTripBeforeLogout ->
        ToastMessage(ToastType.Error, stringResource(R.string.home_msg_finish_trip_before_logout))
    HomeMessage.LocationRequired -> ToastMessage(ToastType.Error, stringResource(R.string.home_msg_location_required))
    HomeMessage.ShiftStarted -> ToastMessage(ToastType.Success, stringResource(R.string.home_msg_shift_started))
    HomeMessage.ShiftEnded -> ToastMessage(ToastType.Success, stringResource(R.string.home_msg_shift_ended))
}

@Composable
private fun HomeMap(
    camera: CameraPositionState,
    location: UserLocation?,
    trip: Trip?,
    bottomPadding: Dp
) {
    if (LocalInspectionMode.current) {
        MapPreviewBackground(showRoute = trip != null)
        return
    }

    val density = LocalDensity.current
    var mapLoaded by remember { mutableStateOf(false) }
    val route = remember(trip?.id, trip?.polyline) { trip?.routePoints().orEmpty() }
    val driverDescription = stringResource(R.string.home_driver_marker)
    val originDescription = stringResource(R.string.home_origin_marker)
    val destinationDescription = stringResource(R.string.home_destination_marker)

    LaunchedEffect(trip?.id, mapLoaded) {
        if (!mapLoaded || route.size < 2) return@LaunchedEffect
        val bounds = LatLngBounds.builder().apply { route.forEach { include(it) } }.build()
        val padding = with(density) { ROUTE_PADDING.roundToPx() }
        try {
            camera.animate(CameraUpdateFactory.newLatLngBounds(bounds, padding), CAMERA_ANIMATION_MS)
        } catch (e: IllegalStateException) {
        }
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
        mapColorScheme = ComposeMapColorScheme.FOLLOW_SYSTEM,
        onMapLoaded = { mapLoaded = true }
    ) {
        if (trip != null && route.size >= 2) {
            Polyline(points = route, color = ColorPrimary, width = 12f)
            MarkerComposable(
                trip.id, "origin",
                state = rememberUpdatedMarkerState(route.first()),
                anchor = Offset(0.5f, 0.5f),
                contentDescription = originDescription
            ) {
                PlaceDot(color = ColorOrigin)
            }
            MarkerComposable(
                trip.id, "destination",
                state = rememberUpdatedMarkerState(route.last()),
                anchor = Offset(0.5f, 0.5f),
                contentDescription = destinationDescription
            ) {
                PlaceDot(color = ColorPrimary)
            }
        }
        if (location != null) {
            MarkerComposable(
                state = rememberUpdatedMarkerState(LatLng(location.latitude, location.longitude)),
                anchor = Offset(0.5f, 0.5f),
                contentDescription = driverDescription
            ) {
                DriverDot()
            }
        }
    }
}

private fun Trip.routePoints(): List<LatLng> {
    val decoded = if (polyline.isBlank()) emptyList() else PolylineDecoder.decode(polyline)
    val points = decoded.ifEmpty { listOf(origin.point, destination.point) }
    return points.map(GeoPoint::toLatLng)
}

private fun GeoPoint.toLatLng(): LatLng = LatLng(latitude, longitude)

@Composable
private fun DriverDot() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(ColorPrimary, CircleShape)
            .border(4.dp, Color.White, CircleShape)
    )
}

@Composable
private fun PlaceDot(color: Color) {
    Box(
        modifier = Modifier
            .size(18.dp)
            .background(Color.White, CircleShape)
            .border(5.dp, color, CircleShape)
    )
}

@Composable
private fun MapPreviewBackground(showRoute: Boolean) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        drawRect(ColorMapLand)
        val road = Color.White
        drawLine(road, Offset(0f, size.height * 0.30f), Offset(size.width, size.height * 0.22f), strokeWidth = 28f)
        drawLine(road, Offset(0f, size.height * 0.55f), Offset(size.width, size.height * 0.62f), strokeWidth = 20f)
        drawLine(road, Offset(size.width * 0.28f, 0f), Offset(size.width * 0.36f, size.height), strokeWidth = 22f)
        drawLine(road, Offset(size.width * 0.74f, 0f), Offset(size.width * 0.66f, size.height), strokeWidth = 16f)
        if (showRoute) {
            drawLine(ColorPrimary, Offset(size.width * 0.3f, size.height * 0.28f), Offset(size.width * 0.68f, size.height * 0.5f), strokeWidth = 12f)
        }
    }
}

@Composable
private fun DriverHeader(
    driver: Driver?,
    connection: ConnectionState,
    modifier: Modifier = Modifier
) {
    val connectionLabel = stringResource(
        when (connection) {
            ConnectionState.Connected -> R.string.home_connection_connected
            ConnectionState.Connecting -> R.string.home_connection_connecting
            ConnectionState.Disconnected -> R.string.home_connection_disconnected
        }
    )
    val connectionColor = when (connection) {
        ConnectionState.Connected -> ColorSuccess
        ConnectionState.Connecting -> Color(0xFFF9A825)
        ConnectionState.Disconnected -> MaterialTheme.colorScheme.error
    }
    val ratingDescription = driver?.let { stringResource(R.string.home_driver_rating, formatRating(it.rating)) }.orEmpty()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(ColorPrimary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = driver?.givenName?.firstOrNull()?.uppercase().orEmpty(),
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .size(12.dp)
                        .background(connectionColor, CircleShape)
                        .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
                        .semantics { contentDescription = connectionLabel }
                )
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = driver?.fullName.orEmpty(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.semantics { heading() }
                )
                if (driver != null) {
                    Text(
                        text = stringResource(R.string.home_vehicle, driver.vehicle.plate, driver.vehicle.brand, driver.vehicle.model),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (driver != null) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.semantics(mergeDescendants = true) { contentDescription = ratingDescription }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFF9A825),
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = formatRating(driver.rating),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
internal fun MapCircleButton(
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

private suspend fun CameraPositionState.centerOn(location: UserLocation) {
    animate(
        CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), USER_ZOOM),
        CAMERA_ANIMATION_MS
    )
}

@Composable
private fun HomeScreenPreview(state: HomeUiState, mapUi: MapUiState = MapUiState.Success(previewLocation, true)) {
    AndroidTheme {
        HomeScreen(state = state, mapUi = mapUi, actions = HomeActions.Empty)
    }
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewOffline() {
    HomeScreenPreview(previewState.copy(online = false))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewRequests() {
    HomeScreenPreview(previewState.copy(requests = listOf(TripRequest(previewTrip))))
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewOffer() {
    HomeScreenPreview(
        previewState.copy(
            requests = listOf(TripRequest(previewTrip)),
            selectedTripId = previewTrip.id,
            proposedPrice = previewTrip.estimatedFare
        )
    )
}

@Preview(showBackground = true, heightDp = 780)
@Composable
private fun HomeScreenPreviewActiveTrip() {
    HomeScreenPreview(previewState.copy(activeTrip = previewTrip.copy(status = TripStatus.ACCEPTED, finalFare = 18.5)))
}

@Preview(name = "Oscuro", showBackground = true, heightDp = 780, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun HomeScreenPreviewRating() {
    HomeScreenPreview(
        previewState.copy(
            activeTrip = previewTrip.copy(status = TripStatus.COMPLETED, finalFare = 18.5),
            rating = 4
        )
    )
}

@Preview(name = "Horizontal", showBackground = true, widthDp = 800, heightDp = 360)
@Composable
private fun HomeScreenPreviewLandscape() {
    HomeScreenPreview(previewState.copy(online = false), MapUiState.Permission(blocked = false))
}
