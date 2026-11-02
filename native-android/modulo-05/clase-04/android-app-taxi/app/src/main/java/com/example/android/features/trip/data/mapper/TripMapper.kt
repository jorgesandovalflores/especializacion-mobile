package com.example.android.features.trip.data.mapper

import com.example.android.features.trip.data.remote.dto.DriverLocationDto
import com.example.android.features.trip.data.remote.dto.OfferDto
import com.example.android.features.trip.data.remote.dto.PlaceDto
import com.example.android.features.trip.data.remote.dto.QuoteDto
import com.example.android.features.trip.data.remote.dto.TripDriverDto
import com.example.android.features.trip.data.remote.dto.TripDto
import com.example.android.features.trip.domain.model.DriverPosition
import com.example.android.features.trip.domain.model.Fare
import com.example.android.features.trip.domain.model.GeoPoint
import com.example.android.features.trip.domain.model.Place
import com.example.android.features.trip.domain.model.Trip
import com.example.android.features.trip.domain.model.TripDriver
import com.example.android.features.trip.domain.model.TripOffer
import com.example.android.features.trip.domain.model.TripQuote
import com.example.android.features.trip.domain.model.TripStatus
import com.example.android.features.trip.domain.model.Vehicle

fun Place.toDto() = PlaceDto(lat = point.lat, lng = point.lng, address = address)

fun PlaceDto.toDomain() = Place(GeoPoint(lat, lng), address)

fun TripDriverDto.toDomain() = TripDriver(
    id = id,
    givenName = givenName,
    familyName = familyName,
    phone = phone,
    rating = rating,
    vehicle = Vehicle(vehicle.plate, vehicle.brand, vehicle.model, vehicle.color)
)

fun QuoteDto.toDomain() = TripQuote(
    origin = origin.toDomain(),
    destination = destination.toDomain(),
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    route = PolylineDecoder.decode(polyline),
    fare = Fare(estimatedFare, currency)
)

fun TripDto.toDomain() = Trip(
    id = id,
    status = runCatching { TripStatus.valueOf(status) }.getOrDefault(TripStatus.REQUESTED),
    origin = origin.toDomain(),
    destination = destination.toDomain(),
    distanceMeters = distanceMeters,
    durationSeconds = durationSeconds,
    route = PolylineDecoder.decode(polyline),
    estimatedFare = Fare(estimatedFare, currency),
    finalFare = finalFare?.let { Fare(it, currency) },
    driver = driver?.toDomain(),
    ratingByPassenger = ratingByPassenger
)

fun OfferDto.toDomain() = TripOffer(
    id = id,
    tripId = tripId,
    price = Fare(price, currency),
    etaSeconds = etaSeconds,
    driver = driver.toDomain()
)

fun DriverLocationDto.toDomain() = DriverPosition(
    driverId = driverId,
    tripId = tripId,
    point = GeoPoint(lat, lng),
    bearing = bearing?.toFloat() ?: 0f
)
