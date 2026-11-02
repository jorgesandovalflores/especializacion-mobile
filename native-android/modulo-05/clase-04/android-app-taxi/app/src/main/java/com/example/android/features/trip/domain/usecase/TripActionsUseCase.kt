package com.example.android.features.trip.domain.usecase

import com.example.android.features.trip.domain.model.CurrentTrip
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.repository.TripRepository

class RequestTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(quote: TripQuote): Trip = repository.request(quote.origin, quote.destination)
}

class GetCurrentTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(): CurrentTrip = repository.current()
}

class AcceptOfferUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: String, offerId: String): Trip = repository.acceptOffer(tripId, offerId)
}

class CancelTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: String): Trip = repository.cancel(tripId)
}

class RateTripUseCase(private val repository: TripRepository) {
    suspend operator fun invoke(tripId: String, rating: Int, comment: String?): Trip {
        require(rating in MIN_RATING..MAX_RATING)
        return repository.rate(tripId, rating, comment?.trim()?.takeIf { it.isNotEmpty() })
    }

    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
