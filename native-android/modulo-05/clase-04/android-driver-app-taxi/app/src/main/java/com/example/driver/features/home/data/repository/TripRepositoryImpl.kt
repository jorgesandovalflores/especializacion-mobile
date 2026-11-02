package com.example.driver.features.home.data.repository

import com.example.driver.core.data.ErrorMapper
import com.example.driver.core.data.safeCall
import com.example.driver.core.domain.DomainException
import com.example.driver.features.home.data.remote.DriverTripsApi
import com.example.driver.features.home.data.remote.dto.AvailabilityRequest
import com.example.driver.features.home.data.remote.dto.OfferRequest
import com.example.driver.features.home.data.remote.dto.RatingRequest
import com.example.driver.features.home.data.remote.dto.StatusRequest
import com.example.driver.features.home.data.remote.dto.toDomain
import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.repository.TripRepository

class TripRepositoryImpl(
    private val api: DriverTripsApi
) : TripRepository {

    override suspend fun setAvailability(online: Boolean, latitude: Double?, longitude: Double?): Boolean = safeCall {
        api.availability(AvailabilityRequest(online = online, lat = latitude, lng = longitude)).online ?: online
    }

    override suspend fun requests(): List<Trip> = safeCall {
        api.requests().mapNotNull { it.toDomain() }
    }

    override suspend fun current(): Trip? = safeCall {
        api.current().trip?.toDomain()
    }

    override suspend fun sendOffer(tripId: String, price: Double): Offer = safeCall {
        api.offer(tripId, OfferRequest(price)).toDomain(fallbackTripId = tripId)
    }

    override suspend fun updateStatus(tripId: String, status: TripStatus): Trip = safeCall {
        api.status(tripId, StatusRequest(status.name)).toDomain() ?: throw DomainException.ServerException(MESSAGE_PARSE)
    }

    override suspend fun rate(tripId: String, rating: Int, comment: String?): Trip = safeCall {
        api.rate(tripId, RatingRequest(rating, comment)).toDomain() ?: throw DomainException.ServerException(MESSAGE_PARSE)
    }

    private companion object {
        const val MESSAGE_PARSE = ErrorMapper.MESSAGE_PARSE
    }
}
