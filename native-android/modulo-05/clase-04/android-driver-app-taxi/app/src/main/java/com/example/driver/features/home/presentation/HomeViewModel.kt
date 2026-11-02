package com.example.driver.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.core.domain.toDomainException
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripEvent
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UnavailableReason
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.repository.LocationTracker
import com.example.driver.features.home.domain.repository.TripEventsRepository
import com.example.driver.features.home.domain.rules.FareRules
import com.example.driver.features.home.domain.usecase.AdvanceTripUseCase
import com.example.driver.features.home.domain.usecase.AvailabilityUseCase
import com.example.driver.features.home.domain.usecase.GetCurrentTripUseCase
import com.example.driver.features.home.domain.usecase.GetTripRequestsUseCase
import com.example.driver.features.home.domain.usecase.ObserveLocationUseCase
import com.example.driver.features.home.domain.usecase.RateTripUseCase
import com.example.driver.features.home.domain.usecase.SendOfferUseCase
import com.example.driver.features.home.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

private sealed interface TrackingCommand {
    data object Stop : TrackingCommand
    data class Start(val precise: Boolean) : TrackingCommand
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val observeLocation: ObserveLocationUseCase,
    private val tracker: LocationTracker,
    private val events: TripEventsRepository,
    private val availability: AvailabilityUseCase,
    private val getRequests: GetTripRequestsUseCase,
    private val getCurrentTrip: GetCurrentTripUseCase,
    private val sendOffer: SendOfferUseCase,
    private val advanceTrip: AdvanceTripUseCase,
    private val rateTrip: RateTripUseCase,
    private val signOut: SignOutUseCase,
    driverStore: DriverStore
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(online = availability.current()))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val permission = MutableStateFlow<LocationPermission>(LocationPermission.Unknown)
    private val attempt = MutableStateFlow(0)
    private var lastLocation: UserLocation? = null

    val mapUi: StateFlow<MapUiState> = combine(permission, attempt) { current, _ -> current }
        .flatMapLatest { it.toMapUi() }
        .onEach { if (it is MapUiState.Success) lastLocation = it.location }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MapUiState.Idle)

    init {
        driverStore.driver()
            .onEach { driver -> _state.update { it.copy(driver = driver) } }
            .launchIn(viewModelScope)

        events.events()
            .onEach(::onEvent)
            .launchIn(viewModelScope)

        events.connection
            .onEach(::onConnection)
            .launchIn(viewModelScope)

        combine(permission, _state) { current, ui -> trackingCommand(current, ui) }
            .filterNotNull()
            .distinctUntilChanged()
            .onEach { command ->
                when (command) {
                    is TrackingCommand.Start -> tracker.start(command.precise)
                    TrackingCommand.Stop -> tracker.stop()
                }
            }
            .launchIn(viewModelScope)

        events.connect()
        restore()
    }

    fun onPermissionChecked(system: LocationPermission) {
        permission.update { current ->
            when {
                system is LocationPermission.Granted -> system
                current is LocationPermission.Denied -> current
                else -> system
            }
        }
        if (mapUi.value is MapUiState.Error) retry()
    }

    fun onPermissionResult(result: LocationPermission) {
        permission.value = result
    }

    fun retry() {
        attempt.update { it + 1 }
    }

    fun toggleAvailability() {
        val current = _state.value
        if (current.togglingAvailability || current.restoring) return
        val target = !current.online
        if (!target && current.hasOngoingTrip) {
            show(HomeMessage.FinishTripFirst)
            return
        }
        if (target && permission.value !is LocationPermission.Granted) {
            show(HomeMessage.LocationRequired)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(togglingAvailability = true) }
            runSafely { availability(target, lastLocation) }
                .onSuccess { confirmed ->
                    _state.update {
                        it.copy(
                            online = confirmed,
                            togglingAvailability = false,
                            requests = if (confirmed) it.requests else emptyList(),
                            selectedTripId = if (confirmed) it.selectedTripId else null,
                            message = if (confirmed) HomeMessage.ShiftStarted else HomeMessage.ShiftEnded
                        )
                    }
                    if (confirmed) refreshRequests()
                }
                .onFailure { error ->
                    _state.update { it.copy(togglingAvailability = false) }
                    showError(error)
                }
        }
    }

    fun refreshRequests() {
        viewModelScope.launch {
            runSafely { getRequests() }
                .onSuccess { trips -> _state.update { it.mergeRequests(trips) } }
                .onFailure(::showError)
        }
    }

    fun selectRequest(tripId: String) {
        _state.update { current ->
            val request = current.requests.firstOrNull { it.trip.id == tripId } ?: return@update current
            current.copy(
                selectedTripId = tripId,
                proposedPrice = request.offer?.price ?: FareRules.initial(request.trip.estimatedFare)
            )
        }
    }

    fun closeRequest() {
        _state.update { it.copy(selectedTripId = null) }
    }

    fun increasePrice() {
        _state.update { it.copy(proposedPrice = FareRules.increase(it.proposedPrice)) }
    }

    fun decreasePrice() {
        _state.update { it.copy(proposedPrice = FareRules.decrease(it.proposedPrice)) }
    }

    fun sendOffer() {
        val current = _state.value
        val request = current.selectedRequest ?: return
        if (current.sendingOffer || request.offer != null) return
        viewModelScope.launch {
            _state.update { it.copy(sendingOffer = true) }
            runSafely { sendOffer(request.trip.id, current.proposedPrice) }
                .onSuccess { offer ->
                    _state.update { ui ->
                        ui.copy(
                            sendingOffer = false,
                            requests = ui.requests.map { if (it.trip.id == request.trip.id) it.copy(offer = offer) else it },
                            message = HomeMessage.OfferSent
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(sendingOffer = false) }
                    showError(error)
                }
        }
    }

    fun advanceTrip() {
        val current = _state.value
        val trip = current.ongoingTrip ?: return
        if (current.updatingTrip) return
        viewModelScope.launch {
            _state.update { it.copy(updatingTrip = true) }
            runSafely { advanceTrip(trip) }
                .onSuccess { updated ->
                    _state.update {
                        it.copy(
                            activeTrip = updated.takeIf { t -> t.status != TripStatus.CANCELLED },
                            updatingTrip = false,
                            rating = 0,
                            ratingComment = ""
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { it.copy(updatingTrip = false) }
                    showError(error)
                }
        }
    }

    fun setRating(value: Int) {
        _state.update { it.copy(rating = value.coerceIn(RateTripUseCase.MIN_RATING, RateTripUseCase.MAX_RATING)) }
    }

    fun setRatingComment(text: String) {
        _state.update { it.copy(ratingComment = text.take(MAX_COMMENT_LENGTH)) }
    }

    fun submitRating() {
        val current = _state.value
        val trip = current.activeTrip?.takeIf { it.needsDriverRating } ?: return
        if (current.sendingRating || current.rating == 0) return
        viewModelScope.launch {
            _state.update { it.copy(sendingRating = true) }
            runSafely { rateTrip(trip.id, current.rating, current.ratingComment) }
                .onSuccess {
                    _state.update {
                        it.copy(
                            activeTrip = null,
                            sendingRating = false,
                            rating = 0,
                            ratingComment = "",
                            message = HomeMessage.RatingSent
                        )
                    }
                    if (_state.value.online) refreshRequests()
                }
                .onFailure { error ->
                    _state.update { it.copy(sendingRating = false) }
                    showError(error)
                }
        }
    }

    fun logout(onDone: () -> Unit) {
        val current = _state.value
        if (current.loggingOut) return
        if (current.hasOngoingTrip) {
            show(HomeMessage.FinishTripBeforeLogout)
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(loggingOut = true) }
            signOut()
            onDone()
        }
    }

    fun consumeMessage() {
        _state.update { it.copy(message = null) }
    }

    private fun restore() {
        viewModelScope.launch {
            val trip = runSafely { getCurrentTrip() }
                .onFailure(::showError)
                .getOrNull()
            _state.update { it.copy(activeTrip = trip, restoring = false) }
            if (_state.value.online) {
                runSafely { availability(true, lastLocation) }
                    .onSuccess { confirmed -> _state.update { it.copy(online = confirmed) } }
                if (_state.value.online && !_state.value.hasOngoingTrip) refreshRequests()
            }
        }
    }

    private fun onConnection(connection: ConnectionState) {
        val previous = _state.value.connection
        _state.update { it.copy(connection = connection) }
        val ui = _state.value
        if (connection == ConnectionState.Connected && previous != ConnectionState.Connected &&
            ui.online && !ui.restoring && !ui.hasOngoingTrip
        ) {
            refreshRequests()
        }
    }

    private fun onEvent(event: TripEvent) {
        when (event) {
            is TripEvent.Requested -> onRequested(event.trip)
            is TripEvent.Unavailable -> onUnavailable(event.tripId, event.reason)
            is TripEvent.OfferAccepted -> onAccepted(event.trip)
            is TripEvent.StatusChanged -> onStatusChanged(event.trip)
        }
    }

    private fun onRequested(trip: Trip) {
        _state.update { ui ->
            val ignore = !ui.online || ui.activeTrip != null || trip.status != TripStatus.REQUESTED ||
                ui.requests.any { it.trip.id == trip.id }
            if (ignore) ui else ui.copy(requests = ui.requests + TripRequest(trip))
        }
    }

    private fun onUnavailable(tripId: String, reason: UnavailableReason) {
        _state.update { ui ->
            val removed = ui.requests.firstOrNull { it.trip.id == tripId } ?: return@update ui
            val notify = removed.offer != null || ui.selectedTripId == tripId
            ui.copy(
                requests = ui.requests - removed,
                selectedTripId = ui.selectedTripId.takeIf { it != tripId },
                message = when {
                    !notify -> ui.message
                    reason == UnavailableReason.CANCELLED -> HomeMessage.RequestCancelled
                    else -> HomeMessage.RequestTaken
                }
            )
        }
    }

    private fun onAccepted(trip: Trip) {
        _state.update {
            it.copy(
                activeTrip = trip,
                requests = emptyList(),
                selectedTripId = null,
                rating = 0,
                ratingComment = "",
                message = HomeMessage.OfferAccepted
            )
        }
    }

    private fun onStatusChanged(trip: Trip) {
        val ui = _state.value
        val active = ui.activeTrip
        when {
            active != null && active.id == trip.id -> {
                if (trip.status == TripStatus.CANCELLED) {
                    _state.update { it.copy(activeTrip = null, updatingTrip = false, message = HomeMessage.TripCancelled) }
                    if (_state.value.online) refreshRequests()
                } else {
                    _state.update { it.copy(activeTrip = trip) }
                }
            }
            active == null && trip.status.isOngoing && trip.driverId != null && trip.driverId == ui.driver?.id ->
                onAccepted(trip)
            trip.status != TripStatus.REQUESTED -> onUnavailable(
                trip.id,
                if (trip.status == TripStatus.CANCELLED) UnavailableReason.CANCELLED else UnavailableReason.TAKEN
            )
        }
    }

    private fun HomeUiState.mergeRequests(trips: List<Trip>): HomeUiState {
        if (!online || activeTrip != null) return this
        val serverIds = trips.map { it.id }.toSet()
        val offers = requests.associate { it.trip.id to it.offer }
        val kept = requests.filter { it.offer != null && it.trip.id !in serverIds }
        val merged = trips.filter { it.status == TripStatus.REQUESTED }.map { TripRequest(it, offers[it.id]) } + kept
        return copy(
            requests = merged,
            selectedTripId = selectedTripId.takeIf { id -> merged.any { it.trip.id == id } }
        )
    }

    private fun trackingCommand(current: LocationPermission, ui: HomeUiState): TrackingCommand? = when {
        current == LocationPermission.Unknown || ui.restoring -> null
        current is LocationPermission.Granted && ui.tracking -> TrackingCommand.Start(current.precise)
        else -> TrackingCommand.Stop
    }

    private fun show(message: HomeMessage) {
        _state.update { it.copy(message = message) }
    }

    private fun showError(error: Throwable) {
        show(HomeMessage.Error(error.toDomainException().message))
    }

    private suspend fun <T> runSafely(block: suspend () -> T): Result<T> = try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun LocationPermission.toMapUi(): Flow<MapUiState> = when (this) {
        LocationPermission.Unknown -> flowOf(MapUiState.Idle)
        is LocationPermission.Denied -> flowOf(MapUiState.Permission(blocked))
        is LocationPermission.Granted -> observeLocation(precise).map { it.toMapUi(precise) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
        const val MAX_COMMENT_LENGTH = 280
    }
}
