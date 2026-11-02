package com.example.driver.features.home.presentation

import com.example.driver.commons.domain.model.Driver
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.Trip

data class TripRequest(val trip: Trip, val offer: Offer? = null)

sealed interface HomePanel {
    data object Restoring : HomePanel
    data object Offline : HomePanel
    data object Requests : HomePanel
    data class OfferDraft(val request: TripRequest) : HomePanel
    data class ActiveTrip(val trip: Trip) : HomePanel
    data class Rating(val trip: Trip) : HomePanel
}

sealed interface HomeMessage {
    data class Error(val text: String) : HomeMessage
    data object OfferSent : HomeMessage
    data object OfferAccepted : HomeMessage
    data object TripCancelled : HomeMessage
    data object RequestTaken : HomeMessage
    data object RequestCancelled : HomeMessage
    data object RatingSent : HomeMessage
    data object FinishTripFirst : HomeMessage
    data object FinishTripBeforeLogout : HomeMessage
    data object LocationRequired : HomeMessage
    data object ShiftStarted : HomeMessage
    data object ShiftEnded : HomeMessage
}

data class HomeUiState(
    val driver: Driver? = null,
    val online: Boolean = false,
    val restoring: Boolean = true,
    val togglingAvailability: Boolean = false,
    val connection: ConnectionState = ConnectionState.Disconnected,
    val requests: List<TripRequest> = emptyList(),
    val selectedTripId: String? = null,
    val proposedPrice: Double = 0.0,
    val sendingOffer: Boolean = false,
    val activeTrip: Trip? = null,
    val updatingTrip: Boolean = false,
    val rating: Int = 0,
    val ratingComment: String = "",
    val sendingRating: Boolean = false,
    val loggingOut: Boolean = false,
    val message: HomeMessage? = null
) {
    val selectedRequest: TripRequest? get() = requests.firstOrNull { it.trip.id == selectedTripId }

    val ongoingTrip: Trip? get() = activeTrip?.takeIf { it.status.isOngoing }

    val hasOngoingTrip: Boolean get() = ongoingTrip != null

    val tracking: Boolean get() = online || hasOngoingTrip

    val mapTrip: Trip? get() = ongoingTrip ?: selectedRequest?.trip

    val panel: HomePanel
        get() {
            val ongoing = ongoingTrip
            val toRate = activeTrip?.takeIf { it.needsDriverRating }
            val selected = selectedRequest
            return when {
                restoring -> HomePanel.Restoring
                ongoing != null -> HomePanel.ActiveTrip(ongoing)
                toRate != null -> HomePanel.Rating(toRate)
                !online -> HomePanel.Offline
                selected != null -> HomePanel.OfferDraft(selected)
                else -> HomePanel.Requests
            }
        }
}
