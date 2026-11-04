package com.example.android.features.trip.presentation

import android.content.res.Configuration
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.android.R
import com.example.android.commons.presentation.PrimaryButton
import com.example.android.commons.presentation.TextInputField
import com.example.android.core.presentation.theme.AndroidTheme
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.trip.domain.model.Fare
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripDriver
import com.example.android.features.trip.domain.model.TripOffer
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.model.TripStatus
import com.example.android.features.trip.domain.model.Vehicle

private val PANEL_MAX_WIDTH = 420.dp
private val OFFERS_MAX_HEIGHT = 280.dp
private val ColorStar = Color(0xFFFFB300)

data class TripActions(
    val onRequest: () -> Unit = {},
    val onClearDestination: () -> Unit = {},
    val onAccept: (String) -> Unit = {},
    val onCancel: () -> Unit = {},
    val onRate: (Int, String) -> Unit = { _, _ -> },
    val onSkipRating: () -> Unit = {}
)

@Composable
fun TripPanel(
    stage: TripStage,
    wide: Boolean,
    actions: TripActions,
    modifier: Modifier = Modifier
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
        modifier = modifier
            .then(placement)
            .semantics { liveRegion = LiveRegionMode.Polite }
    ) {
        AnimatedContent(
            targetState = stage,
            contentKey = { it::class },
            transitionSpec = {
                (slideInVertically { it / 3 } + fadeIn()) togetherWith (slideOutVertically { it / 3 } + fadeOut())
            },
            modifier = Modifier
                .animateContentSize()
                .then(if (wide) Modifier else Modifier.navigationBarsPadding())
                .padding(24.dp),
            label = "tripPanel"
        ) { current ->
            when (current) {
                TripStage.Idle -> IdleContent()
                is TripStage.Quoting -> LoadingContent(stringResource(R.string.trip_quoting))
                is TripStage.Quoted -> QuotedContent(current, actions)
                is TripStage.Searching -> SearchingContent(current, actions)
                is TripStage.OnTrip -> OnTripContent(current.trip, actions)
                is TripStage.Rating -> RatingContent(current, actions)
            }
        }
    }
}

@Composable
private fun IdleContent() {
    PanelHeader(
        icon = Icons.Filled.Place,
        title = stringResource(R.string.trip_where_title),
        body = stringResource(R.string.trip_where_body)
    )
}

@Composable
private fun LoadingContent(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        CircularProgressIndicator(modifier = Modifier.size(28.dp), color = ColorPrimary, strokeWidth = 3.dp)
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}

@Composable
private fun QuotedContent(stage: TripStage.Quoted, actions: TripActions) {
    val quote = stage.quote
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        RouteSummary(origin = quote.origin, destination = quote.destination)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.trip_suggested_fare),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(
                        R.string.trip_summary,
                        formatDistance(quote.distanceMeters),
                        formatDuration(quote.durationSeconds)
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = quote.fare.formatted(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        PrimaryButton(text = stringResource(R.string.trip_request), onClick = actions.onRequest, loading = stage.requesting)
        SecondaryAction(text = stringResource(R.string.trip_change_destination), onClick = actions.onClearDestination, enabled = !stage.requesting)
    }
}

@Composable
private fun SearchingContent(stage: TripStage.Searching, actions: TripActions) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(
            icon = Icons.Filled.LocationOn,
            title = stringResource(R.string.trip_searching_title),
            body = if (stage.offers.isEmpty()) {
                stringResource(R.string.trip_searching_body)
            } else {
                pluralStringResource(R.plurals.trip_offers_count, stage.offers.size, stage.offers.size)
            },
            loading = true
        )
        if (stage.offers.isNotEmpty()) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.heightIn(max = OFFERS_MAX_HEIGHT)
            ) {
                items(stage.offers, key = { it.id }) { offer ->
                    OfferCard(
                        offer = offer,
                        accepting = stage.acceptingOfferId == offer.id,
                        enabled = stage.acceptingOfferId == null,
                        onAccept = { actions.onAccept(offer.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
        SecondaryAction(
            text = stringResource(R.string.trip_cancel_request),
            onClick = actions.onCancel,
            enabled = stage.acceptingOfferId == null
        )
    }
}

@Composable
private fun OfferCard(
    offer: TripOffer,
    accepting: Boolean,
    enabled: Boolean,
    onAccept: () -> Unit,
    modifier: Modifier = Modifier
) {
    val driver = offer.driver
    val acceptDescription = stringResource(R.string.trip_accept_offer_of, driver.givenName, offer.price.formatted())
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                DriverSummary(driver = driver, modifier = Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = offer.price.formatted(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    offer.etaSeconds?.let {
                        Text(
                            text = stringResource(R.string.trip_offer_eta, etaMinutes(it)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            PrimaryButton(
                text = stringResource(R.string.trip_accept),
                onClick = onAccept,
                enabled = enabled,
                loading = accepting,
                modifier = Modifier.semantics { contentDescription = acceptDescription }
            )
        }
    }
}

@Composable
private fun OnTripContent(trip: Trip, actions: TripActions) {
    val (title, body) = when (trip.status) {
        TripStatus.ARRIVED -> stringResource(R.string.trip_status_arrived) to stringResource(R.string.trip_status_arrived_body)
        TripStatus.IN_PROGRESS -> stringResource(R.string.trip_status_in_progress) to stringResource(
            R.string.trip_status_in_progress_body,
            trip.destination.address ?: stringResource(R.string.trip_point_on_map)
        )
        else -> stringResource(R.string.trip_status_accepted) to trip.driver?.vehicle?.plate.orEmpty()
    }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(icon = Icons.Filled.LocationOn, title = title, body = body)
        trip.driver?.let { driver ->
            Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(16.dp)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    DriverSummary(driver = driver, modifier = Modifier.weight(1f))
                    Text(
                        text = (trip.finalFare ?: trip.estimatedFare).formatted(),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
        if (trip.status == TripStatus.ACCEPTED || trip.status == TripStatus.ARRIVED) {
            SecondaryAction(text = stringResource(R.string.trip_cancel), onClick = actions.onCancel)
        }
    }
}

@Composable
private fun RatingContent(stage: TripStage.Rating, actions: TripActions) {
    var stars by rememberSaveable { mutableIntStateOf(5) }
    var comment by rememberSaveable { mutableStateOf("") }
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        PanelHeader(
            icon = Icons.Filled.Star,
            title = stringResource(R.string.trip_rating_title),
            body = stringResource(R.string.trip_rating_body, stage.trip.driver?.givenName.orEmpty())
        )
        Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
            (1..5).forEach { value ->
                val description = stringResource(R.string.trip_rating_star, value)
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .selectable(selected = stars == value, role = Role.RadioButton, onClick = { stars = value })
                        .semantics { contentDescription = description }
                ) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = null,
                        tint = if (value <= stars) ColorStar else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(36.dp)
                    )
                }
            }
        }
        TextInputField(
            value = comment,
            onValueChange = { comment = it },
            placeholder = stringResource(R.string.trip_rating_comment),
            maxLength = 255
        )
        PrimaryButton(
            text = stringResource(R.string.trip_rating_send),
            onClick = { actions.onRate(stars, comment) },
            loading = stage.sending
        )
        SecondaryAction(text = stringResource(R.string.trip_rating_skip), onClick = actions.onSkipRating, enabled = !stage.sending)
    }
}

@Composable
private fun RouteSummary(origin: Place, destination: Place) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        RouteLine(
            dot = ColorPrimary,
            label = stringResource(R.string.trip_origin_label),
            value = origin.address ?: stringResource(R.string.trip_your_location)
        )
        RouteLine(
            dot = MaterialTheme.colorScheme.onSurface,
            label = stringResource(R.string.trip_destination_label),
            value = destination.address ?: stringResource(R.string.trip_point_on_map)
        )
    }
}

@Composable
private fun RouteLine(dot: Color, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(modifier = Modifier.size(10.dp).background(dot, CircleShape))
        Column {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun DriverSummary(driver: TripDriver, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Box(
            modifier = Modifier.size(44.dp).background(ColorPrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = Icons.Filled.Person, contentDescription = null, tint = ColorPrimary)
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "${driver.givenName} ${driver.familyName}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(imageVector = Icons.Filled.Star, contentDescription = null, tint = ColorStar, modifier = Modifier.size(14.dp))
                Text(
                    text = stringResource(R.string.trip_rating_value, driver.rating),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = stringResource(R.string.trip_vehicle, driver.vehicle.brand, driver.vehicle.model, driver.vehicle.plate),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PanelHeader(icon: ImageVector, title: String, body: String, loading: Boolean = false) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier.size(48.dp).background(ColorPrimary.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            if (loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = ColorPrimary, strokeWidth = 3.dp)
            } else {
                Icon(imageVector = icon, contentDescription = null, tint = ColorPrimary)
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.semantics { heading() }
            )
            Text(text = body, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SecondaryAction(text: String, onClick: () -> Unit, enabled: Boolean = true) {
    TextButton(onClick = onClick, enabled = enabled, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
        Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
    }
}

private val previewDriver = TripDriver(
    id = "d1",
    givenName = "Carlos",
    familyName = "Quispe",
    phone = "51900000001",
    rating = 4.9,
    vehicle = Vehicle("ABC-123", "Toyota", "Yaris", "Blanco")
)

private val previewOrigin = Place(GeoPoint(-12.0464, -77.0428), "Jirón de la Unión 300")
private val previewDestination = Place(GeoPoint(-12.1211, -77.0297), "Parque Kennedy")

private val previewTrip = Trip(
    id = "t1",
    status = TripStatus.ACCEPTED,
    origin = previewOrigin,
    destination = previewDestination,
    distanceMeters = 9919,
    durationSeconds = 790,
    route = emptyList(),
    estimatedFare = Fare(19.88, "PEN"),
    finalFare = Fare(17.0, "PEN"),
    driver = previewDriver,
    ratingByPassenger = null
)

@Composable
private fun TripPanelPreview(stage: TripStage) {
    AndroidTheme {
        Box(modifier = Modifier.background(MaterialTheme.colorScheme.background)) {
            TripPanel(stage = stage, wide = false, actions = TripActions())
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TripPanelIdlePreview() {
    TripPanelPreview(TripStage.Idle)
}

@Preview(showBackground = true)
@Composable
private fun TripPanelQuotedPreview() {
    TripPanelPreview(
        TripStage.Quoted(
            TripQuote(previewOrigin, previewDestination, 9919, 790, emptyList(), Fare(19.88, "PEN"))
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun TripPanelSearchingPreview() {
    TripPanelPreview(
        TripStage.Searching(
            trip = previewTrip.copy(status = TripStatus.REQUESTED, driver = null),
            offers = listOf(
                TripOffer("o1", "t1", Fare(18.5, "PEN"), 353, previewDriver),
                TripOffer("o2", "t1", Fare(17.0, "PEN"), 465, previewDriver.copy(id = "d2", givenName = "María", familyName = "Rojas"))
            )
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun TripPanelOnTripPreview() {
    TripPanelPreview(TripStage.OnTrip(previewTrip))
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun TripPanelRatingPreview() {
    TripPanelPreview(TripStage.Rating(previewTrip.copy(status = TripStatus.COMPLETED)))
}
