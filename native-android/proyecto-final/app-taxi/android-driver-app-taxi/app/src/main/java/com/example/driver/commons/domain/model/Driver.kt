package com.example.driver.commons.domain.model

data class Vehicle(
    val plate: String,
    val brand: String,
    val model: String,
    val color: String
)

data class Driver(
    val id: String,
    val phone: String,
    val givenName: String,
    val familyName: String,
    val email: String?,
    val photoUrl: String?,
    val status: String,
    val rating: Double,
    val ratingCount: Int,
    val vehicle: Vehicle
) {
    val fullName: String get() = "$givenName $familyName".trim()
}
