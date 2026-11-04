package com.example.android.features.trip.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.android.R
import com.example.android.core.presentation.theme.ColorPrimary
import com.example.android.features.trip.domain.model.GeoPoint
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMapComposable
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberUpdatedMarkerState

private val ColorDriver = Color(0xFF0F0F0F)

data class TripMapModel(
    val route: List<GeoPoint> = emptyList(),
    val destination: GeoPoint? = null,
    val driver: GeoPoint? = null,
    val driverBearing: Float = 0f
)

fun TripStage.toMapModel(): TripMapModel = when (this) {
    TripStage.Idle -> TripMapModel()
    is TripStage.Quoting -> TripMapModel(destination = destination)
    is TripStage.Quoted -> TripMapModel(route = quote.route, destination = quote.destination.point)
    is TripStage.Searching -> TripMapModel(route = trip.route, destination = trip.destination.point)
    is TripStage.OnTrip -> TripMapModel(
        route = trip.route,
        destination = trip.destination.point,
        driver = driverPosition,
        driverBearing = driverBearing
    )
    is TripStage.Rating -> TripMapModel()
}

fun GeoPoint.toLatLng() = LatLng(lat, lng)

@Composable
@GoogleMapComposable
fun TripMapLayer(model: TripMapModel) {
    if (model.route.size > 1) {
        Polyline(points = model.route.map { it.toLatLng() }, color = ColorPrimary, width = 14f)
    }
    model.destination?.let { destination ->
        MarkerComposable(
            state = rememberUpdatedMarkerState(destination.toLatLng()),
            anchor = Offset(0.5f, 1f),
            contentDescription = stringResource(R.string.trip_destination_marker)
        ) {
            Icon(
                imageVector = Icons.Filled.Place,
                contentDescription = null,
                tint = ColorDriver,
                modifier = Modifier.size(40.dp)
            )
        }
    }
    model.driver?.let { driver ->
        MarkerComposable(
            model.driverBearing,
            state = rememberUpdatedMarkerState(driver.toLatLng()),
            anchor = Offset(0.5f, 0.5f),
            contentDescription = stringResource(R.string.trip_driver_marker)
        ) {
            DriverDot(bearing = model.driverBearing)
        }
    }
}

@Composable
private fun DriverDot(bearing: Float) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .background(ColorDriver, CircleShape)
            .border(3.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Filled.KeyboardArrowUp,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier
                .size(24.dp)
                .rotate(bearing)
        )
    }
}
