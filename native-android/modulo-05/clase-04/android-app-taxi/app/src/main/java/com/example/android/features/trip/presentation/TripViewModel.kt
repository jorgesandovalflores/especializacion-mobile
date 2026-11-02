package com.example.android.features.trip.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.core.domain.toDomainException
import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripStatus
import com.example.android.features.trip.domain.usecase.AcceptOfferUseCase
import com.example.android.features.trip.domain.usecase.CancelTripUseCase
import com.example.android.features.trip.domain.usecase.GetCurrentTripUseCase
import com.example.android.features.trip.domain.usecase.ObserveTripEventsUseCase
import com.example.android.features.trip.domain.usecase.QuoteTripUseCase
import com.example.android.features.trip.domain.usecase.RateTripUseCase
import com.example.android.features.trip.domain.usecase.RequestTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripViewModel @Inject constructor(
    private val quoteTrip: QuoteTripUseCase,
    private val requestTrip: RequestTripUseCase,
    private val getCurrentTrip: GetCurrentTripUseCase,
    private val acceptOffer: AcceptOfferUseCase,
    private val cancelTrip: CancelTripUseCase,
    private val rateTrip: RateTripUseCase,
    private val observeTripEvents: ObserveTripEventsUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow(TripUiState())
    val ui: StateFlow<TripUiState> = _ui.asStateFlow()

    init {
        observeTripEvents().onEach(::onEvent).launchIn(viewModelScope)
        observeTripEvents.connected().onEach { syncWithServer() }.launchIn(viewModelScope)
        syncWithServer()
    }

    fun onMapTap(origin: GeoPoint?, destination: GeoPoint) {
        val stage = _ui.value.stage
        if (origin == null || (stage !is TripStage.Idle && stage !is TripStage.Quoted)) return
        if (stage is TripStage.Quoted && stage.requesting) return
        setStage(TripStage.Quoting(destination))
        viewModelScope.launch {
            runCatching { quoteTrip(origin, destination) }
                .onSuccess { quote ->
                    if (_ui.value.stage == TripStage.Quoting(destination)) setStage(TripStage.Quoted(quote))
                }
                .onFailure { fail(it, TripStage.Idle) }
        }
    }

    fun clearDestination() {
        if (_ui.value.stage is TripStage.Quoted) setStage(TripStage.Idle)
    }

    fun request() {
        val stage = _ui.value.stage as? TripStage.Quoted ?: return
        if (stage.requesting) return
        setStage(stage.copy(requesting = true))
        viewModelScope.launch {
            runCatching { requestTrip(stage.quote) }
                .onSuccess { trip -> setStage(TripStage.Searching(trip, offers = emptyList())) }
                .onFailure { fail(it, stage) }
        }
    }

    fun accept(offerId: String) {
        val stage = _ui.value.stage as? TripStage.Searching ?: return
        if (stage.acceptingOfferId != null) return
        setStage(stage.copy(acceptingOfferId = offerId))
        viewModelScope.launch {
            runCatching { acceptOffer(stage.trip.id, offerId) }
                .onSuccess { trip -> setStage(TripStage.OnTrip(trip)) }
                .onFailure { error ->
                    val current = _ui.value.stage as? TripStage.Searching
                    fail(error, current?.copy(acceptingOfferId = null, offers = current.offers.filterNot { it.id == offerId }) ?: _ui.value.stage)
                }
        }
    }

    fun cancel() {
        val trip = when (val stage = _ui.value.stage) {
            is TripStage.Searching -> stage.trip
            is TripStage.OnTrip -> stage.trip.takeIf { it.status in CANCELLABLE }
            else -> null
        } ?: return
        viewModelScope.launch {
            runCatching { cancelTrip(trip.id) }
                .onSuccess { _ui.value = TripUiState(TripStage.Idle, TripMessage.Cancelled) }
                .onFailure { fail(it, _ui.value.stage) }
        }
    }

    fun rate(stars: Int, comment: String) {
        val stage = _ui.value.stage as? TripStage.Rating ?: return
        if (stage.sending) return
        setStage(stage.copy(sending = true))
        viewModelScope.launch {
            runCatching { rateTrip(stage.trip.id, stars, comment) }
                .onSuccess { _ui.value = TripUiState(TripStage.Idle, TripMessage.Rated) }
                .onFailure { fail(it, stage) }
        }
    }

    fun skipRating() {
        if (_ui.value.stage is TripStage.Rating) setStage(TripStage.Idle)
    }

    fun messageShown() {
        _ui.update { it.copy(message = null) }
    }

    private fun onEvent(event: TripEvent) {
        val stage = _ui.value.stage
        when (event) {
            is TripEvent.OfferReceived -> if (stage is TripStage.Searching && stage.trip.id == event.offer.tripId) {
                if (stage.offers.none { it.id == event.offer.id }) setStage(stage.copy(offers = stage.offers + event.offer))
            }
            is TripEvent.StatusChanged -> if (stage.tripId() == event.trip.id) onStatus(event.trip, stage)
            is TripEvent.DriverMoved -> if (stage is TripStage.OnTrip && stage.trip.id == event.position.tripId) {
                setStage(stage.copy(driverPosition = event.position.point, driverBearing = event.position.bearing))
            }
        }
    }

    private fun onStatus(trip: Trip, stage: TripStage) {
        when (trip.status) {
            TripStatus.CANCELLED -> _ui.value = TripUiState(TripStage.Idle, TripMessage.Cancelled)
            TripStatus.COMPLETED -> setStage(TripStage.Rating(trip))
            TripStatus.REQUESTED -> Unit
            else -> setStage(
                TripStage.OnTrip(
                    trip = trip,
                    driverPosition = (stage as? TripStage.OnTrip)?.driverPosition,
                    driverBearing = (stage as? TripStage.OnTrip)?.driverBearing ?: 0f
                )
            )
        }
    }

    private fun syncWithServer() {
        viewModelScope.launch {
            runCatching { getCurrentTrip() }.onSuccess(::restore)
        }
    }

    private fun restore(current: CurrentTrip) {
        val trip = current.trip
        val stage = _ui.value.stage
        val restored = when {
            trip == null -> if (stage is TripStage.Searching || stage is TripStage.OnTrip) TripStage.Idle else null
            trip.status == TripStatus.REQUESTED -> TripStage.Searching(
                trip = trip,
                offers = current.offers,
                acceptingOfferId = (stage as? TripStage.Searching)?.acceptingOfferId
            )
            trip.status == TripStatus.COMPLETED -> if (trip.ratingByPassenger == null && stage !is TripStage.Rating) TripStage.Rating(trip) else null
            trip.status in ACTIVE -> TripStage.OnTrip(
                trip = trip,
                driverPosition = (stage as? TripStage.OnTrip)?.driverPosition,
                driverBearing = (stage as? TripStage.OnTrip)?.driverBearing ?: 0f
            )
            else -> null
        }
        if (restored != null) setStage(restored)
    }

    private fun setStage(stage: TripStage) {
        _ui.update { it.copy(stage = stage) }
    }

    private fun fail(error: Throwable, fallback: TripStage) {
        _ui.value = TripUiState(fallback, TripMessage.Error(error.toDomainException().message))
    }

    private fun TripStage.tripId(): String? = when (this) {
        is TripStage.Searching -> trip.id
        is TripStage.OnTrip -> trip.id
        is TripStage.Rating -> trip.id
        else -> null
    }

    private companion object {
        val ACTIVE = setOf(TripStatus.ACCEPTED, TripStatus.ARRIVED, TripStatus.IN_PROGRESS)
        val CANCELLABLE = setOf(TripStatus.ACCEPTED, TripStatus.ARRIVED)
    }
}
