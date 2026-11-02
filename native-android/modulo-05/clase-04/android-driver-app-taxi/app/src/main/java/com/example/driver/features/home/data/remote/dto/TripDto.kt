package com.example.driver.features.home.data.remote.dto

import com.example.driver.features.home.domain.model.Offer
import com.example.driver.features.home.domain.model.OfferStatus
import com.example.driver.features.home.domain.model.Place
import com.example.driver.features.home.domain.model.Trip
import com.example.driver.features.home.domain.model.TripPassenger
import com.example.driver.features.home.domain.model.TripStatus
import com.example.driver.features.home.domain.model.UnavailableReason

data class PlaceDto(
    val lat: Double?,
    val lng: Double?,
    val address: String?
)

data class TripPassengerDto(
    val id: String?,
    val givenName: String?,
    val familyName: String?,
    val phone: String?,
    val photoUrl: String?
)

data class TripDriverDto(
    val id: String?
)

data class TripDto(
    val id: String?,
    val status: String?,
    val origin: PlaceDto?,
    val destination: PlaceDto?,
    val distanceMeters: Double?,
    val durationSeconds: Double?,
    val polyline: String?,
    val estimatedFare: Double?,
    val finalFare: Double?,
    val currency: String?,
    val passenger: TripPassengerDto?,
    val driver: TripDriverDto?,
    val ratingByDriver: Int?
)

data class OfferDto(
    val id: String?,
    val tripId: String?,
    val price: Double?,
    val status: String?
)

data class CurrentTripResponse(
    val trip: TripDto?
)

data class TripUnavailableDto(
    val tripId: String?,
    val reason: String?
)

data class AvailabilityRequest(
    val online: Boolean,
    val lat: Double?,
    val lng: Double?
)

data class AvailabilityResponse(
    val online: Boolean?
)

data class OfferRequest(
    val price: Double
)

data class StatusRequest(
    val status: String
)

data class RatingRequest(
    val rating: Int,
    val comment: String?
)

fun PlaceDto?.toDomain(): Place = Place(
    latitude = this?.lat ?: 0.0,
    longitude = this?.lng ?: 0.0,
    address = this?.address
)

fun TripPassengerDto.toDomain(): TripPassenger = TripPassenger(
    id = id.orEmpty(),
    givenName = givenName.orEmpty(),
    familyName = familyName.orEmpty(),
    phone = phone.orEmpty(),
    photoUrl = photoUrl
)

fun TripDto.toDomain(): Trip? = id?.takeIf { it.isNotBlank() }?.let { tripId ->
    Trip(
        id = tripId,
        status = status.toTripStatus(),
        origin = origin.toDomain(),
        destination = destination.toDomain(),
        distanceMeters = distanceMeters?.toInt() ?: 0,
        durationSeconds = durationSeconds?.toInt() ?: 0,
        polyline = polyline.orEmpty(),
        estimatedFare = estimatedFare ?: 0.0,
        finalFare = finalFare,
        currency = currency ?: DEFAULT_CURRENCY,
        passenger = passenger?.toDomain(),
        driverId = driver?.id,
        ratingByDriver = ratingByDriver
    )
}

fun OfferDto.toDomain(fallbackTripId: String): Offer = Offer(
    id = id.orEmpty(),
    tripId = tripId ?: fallbackTripId,
    price = price ?: 0.0,
    status = OfferStatus.entries.firstOrNull { it.name == status } ?: OfferStatus.PENDING
)

fun TripUnavailableDto.reasonOrDefault(): UnavailableReason =
    UnavailableReason.entries.firstOrNull { it.name == reason } ?: UnavailableReason.TAKEN

private fun String?.toTripStatus(): TripStatus =
    TripStatus.entries.firstOrNull { it.name == this } ?: TripStatus.REQUESTED

private const val DEFAULT_CURRENCY = "PEN"
