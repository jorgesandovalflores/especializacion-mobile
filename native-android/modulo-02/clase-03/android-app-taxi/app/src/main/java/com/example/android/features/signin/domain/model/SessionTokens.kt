package com.example.android.features.signin.domain.model

data class SessionTokens(
    val accessToken: String,
    val refreshToken: String
)