package com.example.driver.features.home.presentation

import com.example.driver.core.domain.LocalCache
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.features.home.AvailabilityCall
import com.example.driver.features.home.FakeDriverStore
import com.example.driver.features.home.FakeLocationRepository
import com.example.driver.features.home.FakeLocationTracker
import com.example.driver.features.home.FakeSessionStore
import com.example.driver.features.home.FakeTripEventsRepository
import com.example.driver.features.home.FakeTripRepository
import com.example.driver.features.home.RatingCall
import com.example.driver.features.home.domain.model.TripEvent
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UnavailableReason
import com.example.driver.features.home.domain.usecase.AdvanceTripUseCase
import com.example.driver.features.home.domain.usecase.AvailabilityUseCase
import com.example.driver.features.home.domain.usecase.GetCurrentTripUseCase
import com.example.driver.features.home.domain.usecase.GetTripRequestsUseCase
import com.example.driver.features.home.domain.usecase.ObserveLocationUseCase
import com.example.driver.features.home.domain.usecase.RateTripUseCase
import com.example.driver.features.home.domain.usecase.SendOfferUseCase
import com.example.driver.features.home.domain.usecase.SignOutUseCase
import com.example.driver.features.home.trip
import com.example.driver.features.home.userLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val trips = FakeTripRepository()
    private val events = FakeTripEventsRepository()
    private val locations = FakeLocationRepository()
    private val tracker = FakeLocationTracker()
    private val session = FakeSessionStore()
    private lateinit var store: FakeDriverStore

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(online: Boolean = false): HomeViewModel {
        store = FakeDriverStore(online = online)
        return HomeViewModel(
            observeLocation = ObserveLocationUseCase(locations),
            tracker = tracker,
            events = events,
            availability = AvailabilityUseCase(trips, store),
            getRequests = GetTripRequestsUseCase(trips),
            getCurrentTrip = GetCurrentTripUseCase(trips),
            sendOffer = SendOfferUseCase(trips),
            advanceTrip = AdvanceTripUseCase(trips),
            rateTrip = RateTripUseCase(trips),
            signOut = SignOutUseCase(trips, store, tracker, events, session, LocalCache { store.clear() }),
            driverStore = store
        )
    }

    private fun TestScope.withLocation(vm: HomeViewModel) {
        backgroundScope.launch(dispatcher) { vm.mapUi.collect { } }
        vm.onPermissionResult(LocationPermission.Granted(precise = true))
        locations.updates.tryEmit(userLocation())
    }

    @Test
    fun `al abrir Home conecta el socket y muestra el perfil guardado`() = runTest(dispatcher) {
        val vm = viewModel()

        assertEquals(1, events.connects)
        assertEquals("Carlos", vm.state.value.driver?.givenName)
        assertEquals(HomePanel.Offline, vm.state.value.panel)
    }

    @Test
    fun `iniciar labores envia la ubicacion, carga solicitudes y arranca el servicio`() = runTest(dispatcher) {
        trips.requestsResult = listOf(trip("t1"))
        val vm = viewModel()
        withLocation(vm)

        vm.toggleAvailability()

        val state = vm.state.value
        assertEquals(listOf(AvailabilityCall(true, -12.09713, -77.03654)), trips.availabilityCalls)
        assertTrue(state.online)
        assertTrue(store.isOnline())
        assertEquals(listOf("t1"), state.requests.map { it.trip.id })
        assertEquals(HomePanel.Requests, state.panel)
        assertEquals(HomeMessage.ShiftStarted, state.message)
        assertEquals("start(precise=true)", tracker.calls.last())
    }

    @Test
    fun `sin permiso de ubicacion no inicia labores`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.toggleAvailability()

        assertEquals(HomeMessage.LocationRequired, vm.state.value.message)
        assertTrue(trips.availabilityCalls.isEmpty())
        assertFalse(vm.state.value.online)
    }

    @Test
    fun `terminar labores detiene el servicio y limpia las solicitudes`() = runTest(dispatcher) {
        trips.requestsResult = listOf(trip("t1"))
        val vm = viewModel(online = true)
        withLocation(vm)

        vm.toggleAvailability()

        assertFalse(vm.state.value.online)
        assertTrue(vm.state.value.requests.isEmpty())
        assertEquals(AvailabilityCall(false, -12.09713, -77.03654), trips.availabilityCalls.last())
        assertEquals("stop", tracker.calls.last())
    }

    @Test
    fun `trip requested agrega la solicitud solo si esta en labores`() = runTest(dispatcher) {
        val offline = viewModel()
        events.events.emit(TripEvent.Requested(trip("t1")))
        assertTrue(offline.state.value.requests.isEmpty())

        val online = viewModel(online = true)
        events.events.emit(TripEvent.Requested(trip("t2")))
        events.events.emit(TripEvent.Requested(trip("t2")))

        assertEquals(listOf("t2"), online.state.value.requests.map { it.trip.id })
    }

    @Test
    fun `al reconectar el socket vuelve a pedir las solicitudes`() = runTest(dispatcher) {
        viewModel(online = true)
        val before = trips.requestsCalls

        events.connection.value = ConnectionState.Connecting
        events.connection.value = ConnectionState.Connected

        assertEquals(before + 1, trips.requestsCalls)
    }

    @Test
    fun `ofertar envia el precio ajustado y queda esperando al pasajero`() = runTest(dispatcher) {
        trips.requestsResult = listOf(trip("t1", estimatedFare = 19.76))
        val vm = viewModel(online = true)

        vm.selectRequest("t1")
        assertEquals(19.76, vm.state.value.proposedPrice, 0.0)
        vm.increasePrice()
        vm.increasePrice()
        vm.decreasePrice()
        vm.sendOffer()

        val state = vm.state.value
        assertEquals(listOf("t1" to 20.26), trips.offers)
        assertEquals(20.26, state.selectedRequest?.offer?.price ?: 0.0, 0.0)
        assertEquals(HomeMessage.OfferSent, state.message)
        assertTrue(state.panel is HomePanel.OfferDraft)

        trips.requestsResult = emptyList()
        vm.refreshRequests()
        assertEquals(listOf("t1"), vm.state.value.requests.map { it.trip.id })

        vm.sendOffer()
        assertEquals(1, trips.offers.size)
    }

    @Test
    fun `trip unavailable quita la solicitud ofertada y avisa`() = runTest(dispatcher) {
        trips.requestsResult = listOf(trip("t1"), trip("t2"))
        val vm = viewModel(online = true)
        vm.selectRequest("t1")
        vm.sendOffer()

        events.events.emit(TripEvent.Unavailable("t1", UnavailableReason.TAKEN))

        val state = vm.state.value
        assertEquals(listOf("t2"), state.requests.map { it.trip.id })
        assertNull(state.selectedTripId)
        assertEquals(HomeMessage.RequestTaken, state.message)
    }

    @Test
    fun `oferta aceptada abre el viaje y los botones avanzan hasta calificar`() = runTest(dispatcher) {
        trips.requestsResult = listOf(trip("t1"))
        val vm = viewModel(online = true)
        withLocation(vm)
        vm.selectRequest("t1")
        vm.sendOffer()

        events.events.emit(TripEvent.OfferAccepted(trip("t1", TripStatus.ACCEPTED, finalFare = 20.0)))

        assertEquals(HomeMessage.OfferAccepted, vm.state.value.message)
        assertTrue(vm.state.value.requests.isEmpty())
        assertEquals(TripStatus.ACCEPTED, (vm.state.value.panel as HomePanel.ActiveTrip).trip.status)

        vm.toggleAvailability()
        assertEquals(HomeMessage.FinishTripFirst, vm.state.value.message)
        assertTrue(vm.state.value.online)

        vm.advanceTrip()
        assertEquals(TripStatus.ARRIVED, (vm.state.value.panel as HomePanel.ActiveTrip).trip.status)
        vm.advanceTrip()
        assertEquals(TripStatus.IN_PROGRESS, (vm.state.value.panel as HomePanel.ActiveTrip).trip.status)
        vm.advanceTrip()

        assertEquals(
            listOf("t1" to TripStatus.ARRIVED, "t1" to TripStatus.IN_PROGRESS, "t1" to TripStatus.COMPLETED),
            trips.statusCalls
        )
        assertTrue(vm.state.value.panel is HomePanel.Rating)
        assertEquals("start(precise=true)", tracker.calls.last())
    }

    @Test
    fun `calificar al pasajero envia estrellas y comentario y vuelve a disponible`() = runTest(dispatcher) {
        trips.currentResult = trip("t1", TripStatus.COMPLETED, finalFare = 18.5)
        val vm = viewModel(online = true)
        assertTrue(vm.state.value.panel is HomePanel.Rating)

        vm.submitRating()
        assertTrue(trips.ratings.isEmpty())

        vm.setRating(9)
        vm.setRatingComment("  Muy amable  ")
        vm.submitRating()

        val state = vm.state.value
        assertEquals(listOf(RatingCall("t1", 5, "Muy amable")), trips.ratings)
        assertNull(state.activeTrip)
        assertEquals(HomePanel.Requests, state.panel)
        assertEquals(HomeMessage.RatingSent, state.message)
    }

    @Test
    fun `si el pasajero cancela el viaje vuelve a disponible`() = runTest(dispatcher) {
        trips.currentResult = trip("t1", TripStatus.ACCEPTED)
        val vm = viewModel(online = true)
        assertTrue(vm.state.value.panel is HomePanel.ActiveTrip)

        events.events.emit(TripEvent.StatusChanged(trip("t1", TripStatus.CANCELLED)))

        assertNull(vm.state.value.activeTrip)
        assertEquals(HomeMessage.TripCancelled, vm.state.value.message)
        assertEquals(HomePanel.Requests, vm.state.value.panel)
    }

    @Test
    fun `con un viaje restaurado el servicio sigue aunque no este en labores`() = runTest(dispatcher) {
        trips.currentResult = trip("t1", TripStatus.IN_PROGRESS)
        val vm = viewModel(online = false)

        vm.onPermissionChecked(LocationPermission.Granted(precise = true))

        assertEquals(listOf("start(precise=true)"), tracker.calls)
        assertTrue(vm.state.value.panel is HomePanel.ActiveTrip)
    }

    @Test
    fun `trip status del ganador sin offer-accepted tambien abre el viaje`() = runTest(dispatcher) {
        val vm = viewModel(online = true)

        events.events.emit(TripEvent.StatusChanged(trip("t9", TripStatus.ACCEPTED, driverId = "driver-1")))

        assertEquals("t9", vm.state.value.activeTrip?.id)
    }

    @Test
    fun `cerrar sesion en labores apaga la disponibilidad y desconecta el socket`() = runTest(dispatcher) {
        session.saveTokens("access", "refresh")
        val vm = viewModel(online = true)
        var done = false

        vm.logout { done = true }

        assertTrue(done)
        assertEquals(AvailabilityCall(false, null, null), trips.availabilityCalls.last())
        assertEquals(1, events.disconnects)
        assertEquals("stop", tracker.calls.last())
        assertNull(session.currentAccess())
        assertFalse(store.isOnline())
    }

    @Test
    fun `no se puede cerrar sesion con un viaje activo`() = runTest(dispatcher) {
        trips.currentResult = trip("t1", TripStatus.ARRIVED)
        val vm = viewModel(online = true)
        var done = false

        vm.logout { done = true }

        assertFalse(done)
        assertEquals(HomeMessage.FinishTripBeforeLogout, vm.state.value.message)
        assertEquals(0, events.disconnects)
    }
}
