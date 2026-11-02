package com.example.android.features.trip.domain.usecase

import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.repository.AddressResolver
import com.example.android.features.trip.domain.repository.TripRepository

class QuoteTripUseCase(
    private val repository: TripRepository,
    private val addresses: AddressResolver
) {
    suspend operator fun invoke(origin: GeoPoint, destination: GeoPoint): TripQuote =
        repository.quote(
            origin = Place(origin, addresses.addressOf(origin)),
            destination = Place(destination, addresses.addressOf(destination))
        )
}
