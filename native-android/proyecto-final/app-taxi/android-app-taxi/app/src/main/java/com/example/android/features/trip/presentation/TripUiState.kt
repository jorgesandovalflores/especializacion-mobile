package com.example.android.features.trip.presentation

import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripOffer
import com.example.android.features.trip.domain.model.TripQuote

sealed interface TripStage {
    data object Idle : TripStage
    data class Quoting(val destination: GeoPoint) : TripStage
    data class Quoted(val quote: TripQuote, val requesting: Boolean = false) : TripStage
    data class Searching(
        val trip: Trip,
        val offers: List<TripOffer>,
        val acceptingOfferId: String? = null
    ) : TripStage
    data class OnTrip(val trip: Trip, val driverPosition: GeoPoint? = null, val driverBearing: Float = 0f) : TripStage
    data class Rating(val trip: Trip, val sending: Boolean = false) : TripStage
}

sealed interface TripMessage {
    data class Error(val text: String) : TripMessage
    data object Cancelled : TripMessage
    data object Rated : TripMessage
}

data class TripUiState(
    val stage: TripStage = TripStage.Idle,
    val message: TripMessage? = null
)
