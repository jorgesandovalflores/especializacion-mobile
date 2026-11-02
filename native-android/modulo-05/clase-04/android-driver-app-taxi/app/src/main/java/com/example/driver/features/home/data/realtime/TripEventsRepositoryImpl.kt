package com.example.driver.features.home.data.realtime

import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.core.domain.realtime.RealtimeGateway
import com.example.driver.features.home.data.remote.dto.TripDto
import com.example.driver.features.home.data.remote.dto.TripUnavailableDto
import com.example.driver.features.home.data.remote.dto.reasonOrDefault
import com.example.driver.features.home.data.remote.dto.toDomain
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripEvent
import com.example.driver.features.home.domain.repository.TripEventsRepository
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

class TripEventsRepositoryImpl(
    private val gateway: RealtimeGateway,
    private val gson: Gson = Gson()
) : TripEventsRepository {

    override val connection: Flow<ConnectionState> = gateway.connection

    override fun events(): Flow<TripEvent> = merge(
        trips(RealtimeEvents.TRIP_REQUESTED).map(TripEvent::Requested),
        trips(RealtimeEvents.TRIP_OFFER_ACCEPTED).map(TripEvent::OfferAccepted),
        trips(RealtimeEvents.TRIP_STATUS).map(TripEvent::StatusChanged),
        gateway.on(RealtimeEvents.TRIP_UNAVAILABLE).mapNotNull { payload ->
            val dto = parse(payload, TripUnavailableDto::class.java)
            val tripId = dto?.tripId
            if (tripId.isNullOrBlank()) null else TripEvent.Unavailable(tripId, dto.reasonOrDefault())
        }
    )

    override fun connect() = gateway.connect()

    override fun disconnect() = gateway.disconnect()

    private fun trips(event: String): Flow<Trip> = gateway.on(event).mapNotNull { payload ->
        parse(payload, TripDto::class.java)?.toDomain()
    }

    private fun <T> parse(payload: String, type: Class<T>): T? =
        runCatching { gson.fromJson(payload, type) }.getOrNull()
}
