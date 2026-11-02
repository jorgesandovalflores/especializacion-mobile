package com.example.driver.features.home.domain.model

enum class TripStatus {
    REQUESTED, ACCEPTED, ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED;

    val isOngoing: Boolean get() = this == ACCEPTED || this == ARRIVED || this == IN_PROGRESS
}

data class GeoPoint(val latitude: Double, val longitude: Double)

data class Place(
    val latitude: Double,
    val longitude: Double,
    val address: String?
) {
    val point: GeoPoint get() = GeoPoint(latitude, longitude)
}

data class TripPassenger(
    val id: String,
    val givenName: String,
    val familyName: String,
    val phone: String,
    val photoUrl: String?
) {
    val fullName: String get() = "$givenName $familyName".trim()
}

data class Trip(
    val id: String,
    val status: TripStatus,
    val origin: Place,
    val destination: Place,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val polyline: String,
    val estimatedFare: Double,
    val finalFare: Double?,
    val currency: String,
    val passenger: TripPassenger?,
    val driverId: String?,
    val ratingByDriver: Int?
) {
    val needsDriverRating: Boolean get() = status == TripStatus.COMPLETED && ratingByDriver == null
}

enum class OfferStatus { PENDING, ACCEPTED, REJECTED }

data class Offer(
    val id: String,
    val tripId: String,
    val price: Double,
    val status: OfferStatus
)

enum class UnavailableReason { TAKEN, CANCELLED }

sealed interface TripEvent {
    data class Requested(val trip: Trip) : TripEvent
    data class OfferAccepted(val trip: Trip) : TripEvent
    data class Unavailable(val tripId: String, val reason: UnavailableReason) : TripEvent
    data class StatusChanged(val trip: Trip) : TripEvent
}
