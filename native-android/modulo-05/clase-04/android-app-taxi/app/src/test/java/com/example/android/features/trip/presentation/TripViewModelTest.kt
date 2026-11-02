package com.example.android.features.trip.presentation

import com.example.android.core.domain.DomainException
import com.example.android.core.domain.realtime.RealtimeConnection
import com.example.android.features.trip.DESTINATION
import com.example.android.features.trip.FakeRealtimeClient
import com.example.android.features.trip.FakeTripRepository
import com.example.android.features.trip.ORIGIN
import com.example.android.features.trip.driverCarlos
import com.example.android.features.trip.driverMaria
import com.example.android.features.trip.driverMoved
import com.example.android.features.trip.sampleOffer
import com.example.android.features.trip.sampleQuote
import com.example.android.features.trip.sampleTrip
import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripStatus
import com.example.android.features.trip.domain.repository.AddressResolver
import com.example.android.features.trip.domain.usecase.AcceptOfferUseCase
import com.example.android.features.trip.domain.usecase.CancelTripUseCase
import com.example.android.features.trip.domain.usecase.GetCurrentTripUseCase
import com.example.android.features.trip.domain.usecase.ObserveTripEventsUseCase
import com.example.android.features.trip.domain.usecase.QuoteTripUseCase
import com.example.android.features.trip.domain.usecase.RateTripUseCase
import com.example.android.features.trip.domain.usecase.RequestTripUseCase
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TripViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val realtime = FakeRealtimeClient()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeTripRepository) = TripViewModel(
        quoteTrip = QuoteTripUseCase(repo, AddressResolver { null }),
        requestTrip = RequestTripUseCase(repo),
        getCurrentTrip = GetCurrentTripUseCase(repo),
        acceptOffer = AcceptOfferUseCase(repo),
        cancelTrip = CancelTripUseCase(repo),
        rateTrip = RateTripUseCase(repo),
        observeTripEvents = ObserveTripEventsUseCase(repo, realtime)
    )

    private fun TripViewModel.stage() = ui.value.stage

    private fun searchingViewModel(repo: FakeTripRepository = FakeTripRepository()): TripViewModel =
        viewModel(repo).apply {
            onMapTap(ORIGIN, DESTINATION)
            request()
        }

    @Test
    fun `al iniciar conecta el socket y sincroniza con el servidor`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        viewModel(repo)

        assertEquals(1, realtime.connectCalls)
        assertEquals(1, repo.currentCalls)
    }

    @Test
    fun `tocar el mapa cotiza la ruta y muestra la tarifa`() = runTest(dispatcher) {
        val quote = CompletableDeferred<com.example.android.features.trip.domain.model.TripQuote>()
        val vm = viewModel(FakeTripRepository(onQuote = { quote.await() }))

        vm.onMapTap(ORIGIN, DESTINATION)
        assertEquals(TripStage.Quoting(DESTINATION), vm.stage())

        quote.complete(sampleQuote)
        assertEquals(TripStage.Quoted(sampleQuote), vm.stage())
    }

    @Test
    fun `sin ubicacion del pasajero no cotiza`() = runTest(dispatcher) {
        val vm = viewModel(FakeTripRepository())

        vm.onMapTap(origin = null, destination = DESTINATION)

        assertEquals(TripStage.Idle, vm.stage())
    }

    @Test
    fun `solicitar pasa a Buscando sin ofertas`() = runTest(dispatcher) {
        val vm = searchingViewModel()

        assertEquals(TripStage.Searching(sampleTrip(), offers = emptyList()), vm.stage())
    }

    @Test
    fun `las ofertas del socket se agregan una sola vez y solo las del viaje actual`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val vm = searchingViewModel(repo)

        val first = sampleOffer("o1", driverCarlos, 18.5)
        repo.events.emit(TripEvent.OfferReceived(first))
        repo.events.emit(TripEvent.OfferReceived(first))
        repo.events.emit(TripEvent.OfferReceived(sampleOffer("o2", driverMaria, 17.0)))
        repo.events.emit(TripEvent.OfferReceived(sampleOffer("x", driverMaria, 10.0, tripId = "otro")))

        val stage = vm.stage() as TripStage.Searching
        assertEquals(listOf("o1", "o2"), stage.offers.map { it.id })
    }

    @Test
    fun `aceptar una oferta pasa a OnTrip con el conductor`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val vm = searchingViewModel(repo)
        repo.events.emit(TripEvent.OfferReceived(sampleOffer("o2", driverMaria, 17.0)))

        vm.accept("o2")

        val stage = vm.stage() as TripStage.OnTrip
        assertEquals(TripStatus.ACCEPTED, stage.trip.status)
        assertEquals("María", stage.trip.driver?.givenName)
    }

    @Test
    fun `si la oferta ya no esta disponible la quita y muestra el error`() = runTest(dispatcher) {
        val repo = FakeTripRepository(onAccept = { throw DomainException.ValidationException("La oferta ya no está disponible.") })
        val vm = searchingViewModel(repo)
        repo.events.emit(TripEvent.OfferReceived(sampleOffer("o1", driverCarlos, 18.5)))

        vm.accept("o1")

        val stage = vm.stage() as TripStage.Searching
        assertTrue(stage.offers.isEmpty())
        assertNull(stage.acceptingOfferId)
        assertEquals(TripMessage.Error("La oferta ya no está disponible."), vm.ui.value.message)
    }

    @Test
    fun `durante el viaje mueve al conductor y sigue los cambios de estado`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val vm = searchingViewModel(repo)
        vm.accept("o2")

        repo.events.emit(driverMoved(tripId = "t1"))
        repo.events.emit(driverMoved(tripId = "otro", lat = -13.0))
        assertEquals(GeoPoint(-12.05, -77.04), (vm.stage() as TripStage.OnTrip).driverPosition)

        repo.events.emit(TripEvent.StatusChanged(sampleTrip(TripStatus.ARRIVED, driverMaria)))
        val arrived = vm.stage() as TripStage.OnTrip
        assertEquals(TripStatus.ARRIVED, arrived.trip.status)
        assertEquals(GeoPoint(-12.05, -77.04), arrived.driverPosition)

        repo.events.emit(TripEvent.StatusChanged(sampleTrip(TripStatus.COMPLETED, driverMaria)))
        assertTrue(vm.stage() is TripStage.Rating)
    }

    @Test
    fun `calificar envia las estrellas y vuelve al inicio`() = runTest(dispatcher) {
        val repo = FakeTripRepository(currentTrip = CurrentTrip(sampleTrip(TripStatus.COMPLETED, driverMaria), emptyList()))
        val vm = viewModel(repo)
        assertTrue(vm.stage() is TripStage.Rating)

        vm.rate(4, "  Muy amable  ")

        assertEquals(listOf(4 to "Muy amable"), repo.ratings)
        assertEquals(TripUiState(TripStage.Idle, TripMessage.Rated), vm.ui.value)
    }

    @Test
    fun `si el viaje se cancela desde el servidor vuelve al inicio`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val vm = searchingViewModel(repo)

        repo.events.emit(TripEvent.StatusChanged(sampleTrip(TripStatus.CANCELLED)))

        assertEquals(TripUiState(TripStage.Idle, TripMessage.Cancelled), vm.ui.value)
    }

    @Test
    fun `al reconectar recupera las ofertas que llegaron sin conexion`() = runTest(dispatcher) {
        val repo = FakeTripRepository()
        val vm = searchingViewModel(repo)
        realtime.emit(RealtimeConnection.Connected)
        realtime.emit(RealtimeConnection.Connecting)

        repo.currentTrip = CurrentTrip(sampleTrip(), listOf(sampleOffer("o1", driverCarlos, 18.5)))
        realtime.emit(RealtimeConnection.Connected)

        assertEquals(listOf("o1"), (vm.stage() as TripStage.Searching).offers.map { it.id })
    }

    @Test
    fun `al abrir la app restaura un viaje en curso`() = runTest(dispatcher) {
        val repo = FakeTripRepository(currentTrip = CurrentTrip(sampleTrip(TripStatus.IN_PROGRESS, driverMaria), emptyList()))

        val vm = viewModel(repo)

        assertEquals(TripStatus.IN_PROGRESS, (vm.stage() as TripStage.OnTrip).trip.status)
    }
}
