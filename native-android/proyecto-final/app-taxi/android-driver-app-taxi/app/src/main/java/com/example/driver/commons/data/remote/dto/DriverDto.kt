package com.example.driver.commons.data.remote.dto

import com.example.driver.commons.domain.model.Driver
import com.example.driver.commons.domain.model.Vehicle

data class VehicleDto(
    val plate: String?,
    val brand: String?,
    val model: String?,
    val color: String?
)

data class DriverDto(
    val id: String,
    val phone: String?,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val photoUrl: String?,
    val status: String?,
    val rating: Double?,
    val ratingCount: Int?,
    val vehicle: VehicleDto?
)

fun VehicleDto?.toDomain(): Vehicle = Vehicle(
    plate = this?.plate.orEmpty(),
    brand = this?.brand.orEmpty(),
    model = this?.model.orEmpty(),
    color = this?.color.orEmpty()
)

fun DriverDto.toDomain(): Driver = Driver(
    id = id,
    phone = phone.orEmpty(),
    givenName = givenName.orEmpty(),
    familyName = familyName.orEmpty(),
    email = email,
    photoUrl = photoUrl,
    status = status.orEmpty(),
    rating = rating ?: 0.0,
    ratingCount = ratingCount ?: 0,
    vehicle = vehicle.toDomain()
)
