package com.example.driver.features.home

import com.example.driver.commons.domain.model.Driver
import com.example.driver.commons.domain.model.Vehicle
import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.SessionStore
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.OfferStatus
import com.example.driver.features.home.domain.model.Place
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripEvent
import com.example.driver.features.home.domain.model.TripPassenger
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.repository.LocationRepository
import com.example.driver.features.home.domain.repository.LocationTracker
import com.example.driver.features.home.domain.repository.TripEventsRepository
import com.example.driver.features.home.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

data class AvailabilityCall(val online: Boolean, val latitude: Double?, val longitude: Double?)
data class RatingCall(val tripId: String, val rating: Int, val comment: String?)

class FakeTripRepository : TripRepository {
    val availabilityCalls = mutableListOf<AvailabilityCall>()
    val offers = mutableListOf<Pair<String, Double>>()
    val statusCalls = mutableListOf<Pair<String, TripStatus>>()
    val ratings = mutableListOf<RatingCall>()
    var requestsResult: List<Trip> = emptyList()
    var currentResult: Trip? = null
    var requestsCalls = 0
        private set
    var failure: Throwable? = null

    override suspend fun setAvailability(online: Boolean, latitude: Double?, longitude: Double?): Boolean {
        failure?.let { throw it }
        availabilityCalls += AvailabilityCall(online, latitude, longitude)
        return online
    }

    override suspend fun requests(): List<Trip> {
        requestsCalls++
        return requestsResult
    }

    override suspend fun current(): Trip? = currentResult

    override suspend fun sendOffer(tripId: String, price: Double): Offer {
        failure?.let { throw it }
        offers += tripId to price
        return Offer(id = "offer-$tripId", tripId = tripId, price = price, status = OfferStatus.PENDING)
    }

    override suspend fun updateStatus(tripId: String, status: TripStatus): Trip {
        statusCalls += tripId to status
        return trip(id = tripId, status = status, finalFare = FINAL_FARE)
    }

    override suspend fun rate(tripId: String, rating: Int, comment: String?): Trip {
        ratings += RatingCall(tripId, rating, comment)
        return trip(id = tripId, status = TripStatus.COMPLETED, finalFare = FINAL_FARE, ratingByDriver = rating)
    }

    companion object {
        const val FINAL_FARE = 18.5
    }
}

class FakeTripEventsRepository : TripEventsRepository {
    val events = MutableSharedFlow<TripEvent>(extraBufferCapacity = 16)
    override val connection = MutableStateFlow(ConnectionState.Disconnected)
    var connects = 0
        private set
    var disconnects = 0
        private set

    override fun events(): Flow<TripEvent> = events

    override fun connect() {
        connects++
    }

    override fun disconnect() {
        disconnects++
    }
}

class FakeLocationRepository : LocationRepository {
    val updates = MutableSharedFlow<UserLocation>(replay = 1)

    override fun observeLocation(precise: Boolean): Flow<UserLocation> = flow {
        updates.collect { emit(it) }
    }
}

class FakeLocationTracker : LocationTracker {
    val calls = mutableListOf<String>()

    override fun start(precise: Boolean) {
        calls += "start(precise=$precise)"
    }

    override fun stop() {
        calls += "stop"
    }
}

class FakeDriverStore(online: Boolean = false, driver: Driver? = driver()) : DriverStore {
    private val current = MutableStateFlow(driver)
    private var onlineFlag = online

    override fun driver(): Flow<Driver?> = current
    override suspend fun saveDriver(driver: Driver) {
        current.value = driver
    }

    override fun isOnline(): Boolean = onlineFlag
    override suspend fun setOnline(online: Boolean) {
        onlineFlag = online
    }

    override suspend fun clear() {
        current.value = null
        onlineFlag = false
    }

    fun currentDriver(): Driver? = current.value
}

class FakeSessionStore : SessionStore {
    private val access = MutableStateFlow<String?>(null)
    private val refresh = MutableStateFlow<String?>(null)

    override suspend fun saveTokens(access: String, refresh: String) {
        this.access.value = access
        this.refresh.value = refresh
    }

    override fun accessToken(): Flow<String?> = access
    override fun refreshToken(): Flow<String?> = refresh

    override suspend fun clear() {
        access.value = null
        refresh.value = null
    }

    fun currentAccess(): String? = access.value
    fun currentRefresh(): String? = refresh.value
}

fun driver(id: String = "driver-1") = Driver(
    id = id,
    phone = "51900000001",
    givenName = "Carlos",
    familyName = "Quispe",
    email = "carlos@apptaxi.pe",
    photoUrl = null,
    status = "ACTIVE",
    rating = 4.9,
    ratingCount = 12,
    vehicle = Vehicle(plate = "ABC-123", brand = "Toyota", model = "Yaris", color = "Blanco")
)

fun trip(
    id: String = "trip-1",
    status: TripStatus = TripStatus.REQUESTED,
    estimatedFare: Double = 19.76,
    finalFare: Double? = null,
    driverId: String? = null,
    ratingByDriver: Int? = null
) = Trip(
    id = id,
    status = status,
    origin = Place(-12.0464, -77.0428, "Av. Arequipa 123"),
    destination = Place(-12.1211, -77.0297, "Larcomar"),
    distanceMeters = 9_838,
    durationSeconds = 787,
    polyline = "_p~iF~ps|U_ulLnnqC",
    estimatedFare = estimatedFare,
    finalFare = finalFare,
    currency = "PEN",
    passenger = TripPassenger("passenger-1", "Jorge", "Sandoval", "51980773166", null),
    driverId = driverId,
    ratingByDriver = ratingByDriver
)

fun userLocation(latitude: Double = -12.09713, longitude: Double = -77.03654) = UserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = 8f,
    timeMillis = 0L,
    isMock = false,
    bearing = 90f,
    speedMps = 8.3f
)
