package com.example.driver.features.home.domain.usecase

import com.example.driver.core.domain.DomainException
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.repository.TripRepository
import com.example.driver.features.home.domain.rules.TripFlow

class AdvanceTripUseCase(private val repo: TripRepository) {
    suspend operator fun invoke(trip: Trip): Trip {
        val next = TripFlow.next(trip.status)
            ?: throw DomainException.ValidationException(MESSAGE_INVALID_STATUS)
        return repo.updateStatus(trip.id, next)
    }

    private companion object {
        const val MESSAGE_INVALID_STATUS = "El viaje no admite más cambios de estado"
    }
}

class RateTripUseCase(private val repo: TripRepository) {
    suspend operator fun invoke(tripId: String, rating: Int, comment: String): Trip {
        if (rating !in MIN_RATING..MAX_RATING) throw DomainException.ValidationException(MESSAGE_INVALID_RATING)
        return repo.rate(tripId, rating, comment.trim().ifBlank { null })
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
        private const val MESSAGE_INVALID_RATING = "Elige de 1 a 5 estrellas"
    }
}
