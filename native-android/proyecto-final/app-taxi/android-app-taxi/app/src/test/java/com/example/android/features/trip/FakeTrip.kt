package com.example.android.features.trip

import com.example.android.core.domain.realtime.RealtimeClient
import com.example.android.core.domain.realtime.RealtimeConnection
import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.DriverPosition
import com.example.android.features.trip.domain.model.Fare
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripDriver
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripOffer
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.model.TripStatus
import com.example.android.features.trip.domain.model.Vehicle
import com.example.android.features.trip.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.emptyFlow

val ORIGIN = GeoPoint(-12.0464, -77.0428)
val DESTINATION = GeoPoint(-12.1211, -77.0297)

val driverCarlos = TripDriver("d1", "Carlos", "Quispe", "51900000001", 4.9, Vehicle("ABC-123", "Toyota", "Yaris", "Blanco"))
val driverMaria = TripDriver("d2", "María", "Rojas", "51900000002", 5.0, Vehicle("XYZ-789", "Kia", "Rio", "Gris"))

val sampleQuote = TripQuote(
    origin = Place(ORIGIN, "Plaza San Martín"),
    destination = Place(DESTINATION, "Parque Kennedy"),
    distanceMeters = 9919,
    durationSeconds = 790,
    route = listOf(ORIGIN, DESTINATION),
    fare = Fare(19.88, "PEN")
)

fun sampleTrip(status: TripStatus = TripStatus.REQUESTED, driver: TripDriver? = null) = Trip(
    id = "t1",
    status = status,
    origin = sampleQuote.origin,
    destination = sampleQuote.destination,
    distanceMeters = sampleQuote.distanceMeters,
    durationSeconds = sampleQuote.durationSeconds,
    route = sampleQuote.route,
    estimatedFare = sampleQuote.fare,
    finalFare = driver?.let { Fare(17.0, "PEN") },
    driver = driver,
    ratingByPassenger = null
)

fun sampleOffer(id: String, driver: TripDriver, price: Double, tripId: String = "t1") =
    TripOffer(id = id, tripId = tripId, price = Fare(price, "PEN"), etaSeconds = 300, driver = driver)

fun driverMoved(tripId: String?, lat: Double = -12.05) =
    TripEvent.DriverMoved(DriverPosition("d2", tripId, GeoPoint(lat, -77.04), 90f))

class FakeTripRepository(
    var currentTrip: CurrentTrip = CurrentTrip(null, emptyList()),
    var onQuote: suspend () -> TripQuote = { sampleQuote },
    var onRequest: suspend () -> Trip = { sampleTrip() },
    var onAccept: suspend (String) -> Trip = { sampleTrip(TripStatus.ACCEPTED, driverMaria) },
    var onCancel: suspend () -> Trip = { sampleTrip(TripStatus.CANCELLED) },
    var onRate: suspend (Int, String?) -> Trip = { rating, _ ->
        sampleTrip(TripStatus.COMPLETED, driverMaria).copy(ratingByPassenger = rating)
    }
) : TripRepository {
    val events = MutableSharedFlow<TripEvent>()
    var currentCalls = 0
    val ratings = mutableListOf<Pair<Int, String?>>()

    override suspend fun quote(origin: Place, destination: Place) = onQuote()
    override suspend fun request(origin: Place, destination: Place) = onRequest()
    override suspend fun current(): CurrentTrip {
        currentCalls++
        return currentTrip
    }
    override suspend fun acceptOffer(tripId: String, offerId: String) = onAccept(offerId)
    override suspend fun cancel(tripId: String) = onCancel()
    override suspend fun rate(tripId: String, rating: Int, comment: String?): Trip {
        ratings += rating to comment
        return onRate(rating, comment)
    }
    override fun events(): Flow<TripEvent> = events
}

class FakeRealtimeClient : RealtimeClient {
    private val state = MutableStateFlow(RealtimeConnection.Disconnected)
    override val connection: StateFlow<RealtimeConnection> = state
    var connectCalls = 0

    override fun connect() {
        connectCalls++
    }

    override fun disconnect() {
        state.value = RealtimeConnection.Disconnected
    }

    override fun on(event: String): Flow<String> = emptyFlow()

    fun emit(connection: RealtimeConnection) {
        state.value = connection
    }
}
