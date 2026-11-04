package com.example.android.features.trip.domain.repository

import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.model.TripQuote
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    suspend fun quote(origin: Place, destination: Place): TripQuote
    suspend fun request(origin: Place, destination: Place): Trip
    suspend fun current(): CurrentTrip
    suspend fun acceptOffer(tripId: String, offerId: String): Trip
    suspend fun cancel(tripId: String): Trip
    suspend fun rate(tripId: String, rating: Int, comment: String?): Trip
    fun events(): Flow<TripEvent>
}
