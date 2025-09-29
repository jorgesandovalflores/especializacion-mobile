package com.example.android.features.travels.domain.usecase

import com.example.android.features.travels.domain.model.Trip
import com.example.android.features.travels.domain.repository.TravelRepository
import javax.inject.Inject

class GetPendingTripsUseCase @Inject constructor(
    private val repository: TravelRepository
) {
    suspend operator fun invoke(): List<Trip> = repository.getPendingTrips()
}