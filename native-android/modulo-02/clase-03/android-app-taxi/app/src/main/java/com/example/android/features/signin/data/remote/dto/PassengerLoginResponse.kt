package com.example.android.features.signin.data.remote.dto

data class PassengerLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: PassengerDto
)

data class PassengerDto(
    val id: String,
    val phoneNumber: String,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val photoUrl: String?,
    val status: String
)