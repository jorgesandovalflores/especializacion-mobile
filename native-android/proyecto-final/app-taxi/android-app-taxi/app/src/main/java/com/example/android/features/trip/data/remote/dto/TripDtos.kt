package com.example.android.features.trip.data.remote.dto

data class PlaceDto(val lat: Double, val lng: Double, val address: String?)

data class TripRouteRequest(val origin: PlaceDto, val destination: PlaceDto)

data class RatingRequest(val rating: Int, val comment: String?)

data class VehicleDto(val plate: String, val brand: String, val model: String, val color: String)

data class TripDriverDto(
    val id: String,
    val givenName: String,
    val familyName: String,
    val phone: String,
    val rating: Double,
    val vehicle: VehicleDto
)

data class QuoteDto(
    val origin: PlaceDto,
    val destination: PlaceDto,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val polyline: String,
    val estimatedFare: Double,
    val currency: String
)

data class TripDto(
    val id: String,
    val status: String,
    val origin: PlaceDto,
    val destination: PlaceDto,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val polyline: String,
    val estimatedFare: Double,
    val finalFare: Double?,
    val currency: String,
    val driver: TripDriverDto?,
    val ratingByPassenger: Int?
)

data class OfferDto(
    val id: String,
    val tripId: String,
    val price: Double,
    val currency: String,
    val etaSeconds: Int?,
    val driver: TripDriverDto
)

data class CurrentTripDto(val trip: TripDto?, val offers: List<OfferDto>?)

data class DriverLocationDto(
    val driverId: String,
    val tripId: String?,
    val lat: Double,
    val lng: Double,
    val bearing: Double?
)
