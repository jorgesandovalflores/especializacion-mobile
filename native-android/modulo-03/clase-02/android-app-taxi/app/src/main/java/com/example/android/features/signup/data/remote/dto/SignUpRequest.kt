package com.example.android.features.signup.data.remote.dto

data class SignUpRequest(
    val givenName: String,
    val familyName: String,
    val email: String,
    val photoUrl: String,
)