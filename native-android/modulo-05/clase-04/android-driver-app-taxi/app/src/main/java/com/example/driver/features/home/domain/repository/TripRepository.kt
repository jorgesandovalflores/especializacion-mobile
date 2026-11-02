package com.example.driver.features.home.domain.repository

import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripStatus

interface TripRepository {
    suspend fun setAvailability(online: Boolean, latitude: Double?, longitude: Double?): Boolean
    suspend fun requests(): List<Trip>
    suspend fun current(): Trip?
    suspend fun sendOffer(tripId: String, price: Double): Offer
    suspend fun updateStatus(tripId: String, status: TripStatus): Trip
    suspend fun rate(tripId: String, rating: Int, comment: String?): Trip
}
