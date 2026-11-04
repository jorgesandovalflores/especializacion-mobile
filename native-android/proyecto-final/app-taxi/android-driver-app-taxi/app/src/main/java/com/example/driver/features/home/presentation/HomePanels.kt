package com.example.driver.features.home.presentation

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.driver.R
import com.example.driver.commons.domain.model.Driver
import com.example.driver.commons.domain.model.Vehicle
import com.example.driver.commons.presentation.PrimaryButton
import com.example.driver.commons.presentation.formatPeruPhone
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.core.presentation.theme.ColorPrimary
import com.example.driver.features.home.domain.model.LocationError
import com.example.driver.features.home.domain.model.Place
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripPassenger
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.rules.FareRules
import com.example.driver.features.home.domain.usecase.RateTripUseCase

private const val PULSE_MS = 1_200
private val ColorStar = Color(0xFFF9A825)
private val ColorOriginDot = Color(0xFF2E7D32)

data class HomeActions(
    val onLogout: () -> Unit,
    val onToggleAvailability: () -> Unit,
    val onSelectRequest: (String) -> Unit,
    val onCloseRequest: () -> Unit,
    val onIncreasePrice: () -> Unit,
    val onDecreasePrice: () -> Unit,
    val onSendOffer: () -> Unit,
    val onAdvanceTrip: () -> Unit,
    val onCallPassenger: (String) -> Unit,
    val onRate: (Int) -> Unit,
    val onRatingCommentChange: (String) -> Unit,
    val onSubmitRating: () -> Unit,
    val onRequestPermission: () -> Unit,
    val onOpenAppSettings: () -> Unit,
    val onOpenLocationSettings: () -> Unit,
    val onRetryLocation: () -> Unit
) {
    companion object {
        val Empty = HomeActions({}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {}, {})
    }
}

private sealed interface PanelContent {
    data class Main(val panel: HomePanel) : PanelContent
    data class Location(val state: MapUiState) : PanelContent
}

@Composable
internal fun HomeBottomPanel(
    state: HomeUiState,
    mapUi: MapUiState,
    wide: Boolean,
    actions: HomeActions
) {
    val panel = state.panel
    val locationBlocks = (mapUi is MapUiState.Permission || mapUi is MapUiState.Error) &&
        (panel == HomePanel.Offline || panel == HomePanel.Requests)
    val content: PanelContent = if (locationBlocks) PanelContent.Location(mapUi) else PanelContent.Main(panel)

    val shape: Shape = if (wide) RoundedCornerShape(24.dp) else RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    val placement = if (wide) {
        Modifier
            .navigationBarsPadding()
            .padding(16.dp)
            .widthIn(max = 440.dp)
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
            targetState = content,
            contentKey = { current ->
                when (current) {
                    is PanelContent.Location -> current.state::class
                    is PanelContent.Main -> current.panel::class
                }
            },
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { it / 3 } + fadeOut())
            },
            modifier = Modifier
                .then(if (wide) Modifier else Modifier.windowInsetsPadding(WindowInsets.navigationBars))
                .heightIn(max = 460.dp)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            label = "home-panel"
        ) { current ->
            when (current) {
                is PanelContent.Location -> LocationPanel(current.state, actions)
                is PanelContent.Main -> when (val p = current.panel) {
                    HomePanel.Restoring -> RestoringPanel()
                    HomePanel.Offline -> AvailabilityRow(state, actions.onToggleAvailability)
                    HomePanel.Requests -> RequestsPanel(state, actions)
                    is HomePanel.OfferDraft -> OfferPanel(p.request, state, actions)
                    is HomePanel.ActiveTrip -> ActiveTripPanel(p.trip, state.updatingTrip, actions)
                    is HomePanel.Rating -> RatingPanel(p.trip, state, actions)
                }
            }
        }
    }
}

@Composable
private fun RestoringPanel() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        PanelBody(stringResource(R.string.home_restoring))
    }
}

@Composable
private fun AvailabilityRow(state: HomeUiState, onToggle: () -> Unit) {
    val actionLabel = stringResource(if (state.online) R.string.home_end_shift else R.string.home_start_shift)
    val statusLabel = stringResource(if (state.online) R.string.home_status_online else R.string.home_status_offline)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .toggleable(
                value = state.online,
                enabled = !state.togglingAvailability,
                role = Role.Switch,
                onValueChange = { onToggle() }
            )
            .semantics(mergeDescendants = true) {
                contentDescription = actionLabel
                stateDescription = statusLabel
            },
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            PanelTitle(statusLabel)
            PanelBody(
                stringResource(if (state.online) R.string.home_status_online_body else R.string.home_status_offline_body)
            )
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        if (state.togglingAvailability) {
            CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
        } else {
            Switch(checked = state.online, onCheckedChange = null)
        }
    }
}

@Composable
private fun RequestsPanel(state: HomeUiState, actions: HomeActions) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        AvailabilityRow(state, actions.onToggleAvailability)
        PanelTitle(stringResource(R.string.home_requests_title))
        if (state.requests.isEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchingPulse()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.home_requests_empty_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    PanelBody(stringResource(R.string.home_requests_empty_body))
                }
            }
        } else {
            state.requests.forEach { request ->
                RequestCard(request = request, onClick = { actions.onSelectRequest(request.trip.id) })
            }
        }
    }
}

@Composable
private fun RequestCard(request: TripRequest, onClick: () -> Unit) {
    val trip = request.trip
    val openLabel = stringResource(R.string.home_request_open)
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = openLabel, role = Role.Button, onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .semantics(mergeDescendants = true) { }
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            RouteLines(trip.origin, trip.destination)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.home_request_summary, formatDistance(trip.distanceMeters), formatDuration(trip.durationSeconds)),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = formatMoney(trip.estimatedFare),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            request.offer?.let {
                Text(
                    text = stringResource(R.string.home_request_offer_sent, formatMoney(it.price)),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RouteLines(origin: Place, destination: Place) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        RouteLine(
            dotColor = ColorOriginDot,
            label = stringResource(R.string.home_origin_label),
            address = origin.address ?: stringResource(R.string.home_address_unknown)
        )
        RouteLine(
            dotColor = ColorPrimary,
            label = stringResource(R.string.home_destination_label),
            address = destination.address ?: stringResource(R.string.home_address_unknown)
        )
    }
}

@Composable
private fun RouteLine(dotColor: Color, label: String, address: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.semantics(mergeDescendants = true) { }
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .border(3.dp, dotColor, CircleShape)
        )
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = address,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun OfferPanel(request: TripRequest, state: HomeUiState, actions: HomeActions) {
    val trip = request.trip
    val offer = request.offer
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MapCircleButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.home_back_to_requests),
                onClick = actions.onCloseRequest
            )
            PanelTitle(stringResource(R.string.home_offer_title))
        }
        RouteLines(trip.origin, trip.destination)
        PanelBody(
            stringResource(R.string.home_request_summary, formatDistance(trip.distanceMeters), formatDuration(trip.durationSeconds)) +
                " · " + stringResource(R.string.home_request_suggested, formatMoney(trip.estimatedFare))
        )
        if (offer == null) {
            PriceStepper(
                price = state.proposedPrice,
                enabled = !state.sendingOffer,
                onDecrease = actions.onDecreasePrice,
                onIncrease = actions.onIncreasePrice
            )
            PrimaryButton(
                text = stringResource(R.string.home_offer_send),
                onClick = actions.onSendOffer,
                enabled = FareRules.isValid(state.proposedPrice),
                loading = state.sendingOffer
            )
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SearchingPulse()
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = stringResource(R.string.home_offer_waiting_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    PanelBody(stringResource(R.string.home_offer_waiting_body, formatMoney(offer.price)))
                }
            }
            PrimaryButton(text = stringResource(R.string.home_back_to_requests), onClick = actions.onCloseRequest)
        }
    }
}

@Composable
private fun PriceStepper(
    price: Double,
    enabled: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = stringResource(R.string.home_offer_price_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StepButton(
                symbol = "−",
                contentDescription = stringResource(R.string.home_offer_decrease),
                enabled = enabled && FareRules.canDecrease(price),
                onClick = onDecrease
            )
            Text(
                text = formatMoney(price),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
            )
            StepButton(
                symbol = "+",
                contentDescription = stringResource(R.string.home_offer_increase),
                enabled = enabled,
                onClick = onIncrease
            )
        }
        Text(
            text = stringResource(R.string.home_offer_min),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun StepButton(symbol: String, contentDescription: String, enabled: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier
            .size(48.dp)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = symbol, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun ActiveTripPanel(trip: Trip, updating: Boolean, actions: HomeActions) {
    val (title, action) = when (trip.status) {
        TripStatus.ARRIVED -> R.string.home_trip_arrived_title to R.string.home_trip_action_start
        TripStatus.IN_PROGRESS -> R.string.home_trip_in_progress_title to R.string.home_trip_action_finish
        else -> R.string.home_trip_accepted_title to R.string.home_trip_action_arrived
    }
    val passenger = trip.passenger
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelTitle(stringResource(title))
        if (passenger != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .semantics(mergeDescendants = true) { }
                ) {
                    Text(
                        text = stringResource(R.string.home_trip_passenger),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = passenger.fullName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formatPeruPhone(passenger.phone.removePrefix("51")),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (passenger.phone.isNotBlank()) {
                    MapCircleButton(
                        icon = Icons.Filled.Call,
                        contentDescription = stringResource(R.string.home_trip_call, passenger.givenName),
                        onClick = { actions.onCallPassenger(passenger.phone) },
                        containerColor = ColorPrimary,
                        contentColor = Color.White
                    )
                }
            }
        }
        RouteLines(trip.origin, trip.destination)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics(mergeDescendants = true) { }
        ) {
            Text(
                text = stringResource(R.string.home_trip_final_fare),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatMoney(trip.finalFare ?: trip.estimatedFare),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        PrimaryButton(text = stringResource(action), onClick = actions.onAdvanceTrip, loading = updating)
    }
}

@Composable
private fun RatingPanel(trip: Trip, state: HomeUiState, actions: HomeActions) {
    val name = trip.passenger?.givenName.orEmpty()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelTitle(stringResource(R.string.home_rating_title, name))
        PanelBody(stringResource(R.string.home_rating_body))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            (RateTripUseCase.MIN_RATING..RateTripUseCase.MAX_RATING).forEach { value ->
                StarButton(
                    value = value,
                    selected = value <= state.rating,
                    current = value == state.rating,
                    enabled = !state.sendingRating,
                    onClick = { actions.onRate(value) }
                )
            }
        }
        OutlinedTextField(
            value = state.ratingComment,
            onValueChange = actions.onRatingCommentChange,
            label = { Text(stringResource(R.string.home_rating_comment)) },
            enabled = !state.sendingRating,
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            shape = RoundedCornerShape(16.dp),
            maxLines = 3,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
        PrimaryButton(
            text = stringResource(R.string.home_rating_submit),
            onClick = actions.onSubmitRating,
            enabled = state.rating > 0,
            loading = state.sendingRating
        )
    }
}

@Composable
private fun StarButton(value: Int, selected: Boolean, current: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val label = pluralStringResource(R.plurals.home_rating_star, value, value)
    val status = stringResource(if (current) R.string.home_rating_selected else R.string.home_rating_not_selected)
    Box(
        modifier = Modifier
            .size(48.dp)
            .selectable(selected = current, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics {
                contentDescription = label
                stateDescription = status
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.Star,
            contentDescription = null,
            tint = if (selected) ColorStar else MaterialTheme.colorScheme.outline,
            modifier = Modifier.size(36.dp)
        )
    }
}

@Composable
private fun LocationPanel(state: MapUiState, actions: HomeActions) {
    when (state) {
        is MapUiState.Permission -> PanelWithAction(
            icon = if (state.blocked) Icons.Filled.Lock else Icons.Filled.LocationOn,
            tint = MaterialTheme.colorScheme.primary,
            title = stringResource(if (state.blocked) R.string.home_blocked_title else R.string.home_permission_title),
            body = stringResource(if (state.blocked) R.string.home_blocked_body else R.string.home_permission_body),
            action = stringResource(if (state.blocked) R.string.home_blocked_action else R.string.home_permission_action),
            onAction = if (state.blocked) actions.onOpenAppSettings else actions.onRequestPermission
        )
        is MapUiState.Error -> {
            val (title, body, action) = errorTexts(state.error)
            PanelWithAction(
                icon = Icons.Filled.Warning,
                tint = MaterialTheme.colorScheme.error,
                title = stringResource(title),
                body = stringResource(body),
                action = stringResource(action),
                onAction = when (state.error) {
                    LocationError.SettingsDisabled -> actions.onOpenLocationSettings
                    LocationError.PermissionMissing -> actions.onRequestPermission
                    LocationError.Unavailable -> actions.onRetryLocation
                }
            )
        }
        else -> Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchingPulse()
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PanelTitle(stringResource(R.string.home_searching_title))
                PanelBody(stringResource(R.string.home_searching_body))
            }
        }
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
private fun PanelWithAction(
    icon: ImageVector,
    tint: Color,
    title: String,
    body: String,
    action: String,
    onAction: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
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
        PrimaryButton(text = action, onClick = onAction)
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

internal val previewLocation = UserLocation(
    latitude = -12.09713,
    longitude = -77.03654,
    accuracyMeters = 12f,
    timeMillis = 0L,
    isMock = false
)

internal val previewTrip = Trip(
    id = "trip-1",
    status = TripStatus.REQUESTED,
    origin = Place(-12.0464, -77.0428, "Av. Arequipa 123"),
    destination = Place(-12.1211, -77.0297, "Larcomar, Miraflores"),
    distanceMeters = 9_838,
    durationSeconds = 787,
    polyline = "",
    estimatedFare = 19.76,
    finalFare = null,
    currency = "PEN",
    passenger = TripPassenger("p-1", "Jorge", "Sandoval", "51980773166", null),
    driverId = null,
    ratingByDriver = null
)

internal val previewState = HomeUiState(
    driver = Driver(
        id = "d-1",
        phone = "51900000001",
        givenName = "Carlos",
        familyName = "Quispe",
        email = null,
        photoUrl = null,
        status = "ACTIVE",
        rating = 4.9,
        ratingCount = 12,
        vehicle = Vehicle("ABC-123", "Toyota", "Yaris", "Blanco")
    ),
    online = true,
    restoring = false,
    connection = ConnectionState.Connected
)
