package com.example.android.features.trip.domain.model

data class GeoPoint(val lat: Double, val lng: Double)

data class Place(val point: GeoPoint, val address: String?)

enum class TripStatus { REQUESTED, ACCEPTED, ARRIVED, IN_PROGRESS, COMPLETED, CANCELLED }

data class Vehicle(val plate: String, val brand: String, val model: String, val color: String)

data class TripDriver(
    val id: String,
    val givenName: String,
    val familyName: String,
    val phone: String,
    val rating: Double,
    val vehicle: Vehicle
)

data class Fare(val amount: Double, val currency: String)

data class TripQuote(
    val origin: Place,
    val destination: Place,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val route: List<GeoPoint>,
    val fare: Fare
)

data class Trip(
    val id: String,
    val status: TripStatus,
    val origin: Place,
    val destination: Place,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val route: List<GeoPoint>,
    val estimatedFare: Fare,
    val finalFare: Fare?,
    val driver: TripDriver?,
    val ratingByPassenger: Int?
)

data class TripOffer(
    val id: String,
    val tripId: String,
    val price: Fare,
    val etaSeconds: Int?,
    val driver: TripDriver
)

data class DriverPosition(
    val driverId: String,
    val tripId: String?,
    val point: GeoPoint,
    val bearing: Float
)

data class CurrentTrip(val trip: Trip?, val offers: List<TripOffer>)

sealed interface TripEvent {
    data class OfferReceived(val offer: TripOffer) : TripEvent
    data class StatusChanged(val trip: Trip) : TripEvent
    data class DriverMoved(val position: DriverPosition) : TripEvent
}
