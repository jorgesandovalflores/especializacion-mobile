package com.example.android.features.travels.data.repository

import com.example.android.features.travels.data.api.TravelApi
import com.example.android.features.travels.domain.model.Trip
import com.example.android.features.travels.domain.repository.TravelRepository
import javax.inject.Inject

class TravelRepositoryImpl @Inject constructor(
    private val api: TravelApi
) : TravelRepository {

    override suspend fun getPendingTrips(): List<Trip> {
        val dtos = api.getPendingTrips()
        return dtos.map { dto ->
            Trip(
                id = dto.id,
                passengerName = dto.passengerName,
                pickupAddress = dto.pickup.address,
                dropoffAddress = dto.dropoff.address,
                requestedAtIso = dto.requestedAt
            )
        }
    }
}