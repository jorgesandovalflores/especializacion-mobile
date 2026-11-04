package com.example.driver.features.home.domain.usecase

import com.example.driver.core.domain.DomainException
import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.repository.TripRepository
import com.example.driver.features.home.domain.rules.FareRules

class GetTripRequestsUseCase(private val repo: TripRepository) {
    suspend operator fun invoke(): List<Trip> = repo.requests()
}

class GetCurrentTripUseCase(private val repo: TripRepository) {
    suspend operator fun invoke(): Trip? =
        repo.current()?.takeIf { it.status.isOngoing || it.needsDriverRating }
}

class SendOfferUseCase(private val repo: TripRepository) {
    suspend operator fun invoke(tripId: String, price: Double): Offer {
        if (!FareRules.isValid(price)) throw DomainException.ValidationException(MESSAGE_MIN_PRICE)
        return repo.sendOffer(tripId, price)
    }

    private companion object {
        const val MESSAGE_MIN_PRICE = "El precio mínimo es S/ 1.00"
    }
}
