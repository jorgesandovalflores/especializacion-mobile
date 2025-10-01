package com.example.android.features.signin.domain.model

data class Passenger(
    val id: String,
    val phoneNumber: String,
    val givenName: String?,
    val familyName: String?,
    val email: String?,
    val photoUrl: String?,
    val status: String
)