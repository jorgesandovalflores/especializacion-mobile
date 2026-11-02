package com.example.android.features.trip.data.repository

import com.example.android.core.data.safeCall
import com.example.android.core.domain.realtime.RealtimeClient
import com.example.android.features.trip.data.mapper.toDomain
import com.example.android.features.trip.data.mapper.toDto
import com.example.android.features.trip.data.remote.TripApi
import com.example.android.features.trip.data.remote.dto.RatingRequest
import com.example.android.features.trip.data.remote.dto.TripRouteRequest
import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge

class TripRepositoryImpl(
    private val api: TripApi,
    private val realtime: RealtimeClient,
    private val parser: TripEventParser = TripEventParser()
) : TripRepository {

    override suspend fun quote(origin: Place, destination: Place): TripQuote = safeCall {
        api.quote(TripRouteRequest(origin.toDto(), destination.toDto())).toDomain()
    }

    override suspend fun request(origin: Place, destination: Place): Trip = safeCall {
        api.request(TripRouteRequest(origin.toDto(), destination.toDto())).toDomain()
    }

    override suspend fun current(): CurrentTrip = safeCall {
        val current = api.current()
        CurrentTrip(
            trip = current.trip?.toDomain(),
            offers = current.offers.orEmpty().map { it.toDomain() }
        )
    }

    override suspend fun acceptOffer(tripId: String, offerId: String): Trip = safeCall {
        api.accept(tripId, offerId).toDomain()
    }

    override suspend fun cancel(tripId: String): Trip = safeCall {
        api.cancel(tripId).toDomain()
    }

    override suspend fun rate(tripId: String, rating: Int, comment: String?): Trip = safeCall {
        api.rate(tripId, RatingRequest(rating, comment)).toDomain()
    }

    override fun events(): Flow<TripEvent> = merge(
        realtime.on(TripEvents.OFFER).mapNotNull { parser.parse(TripEvents.OFFER, it) },
        realtime.on(TripEvents.STATUS).mapNotNull { parser.parse(TripEvents.STATUS, it) },
        realtime.on(TripEvents.DRIVER_LOCATION).mapNotNull { parser.parse(TripEvents.DRIVER_LOCATION, it) }
    )
}
