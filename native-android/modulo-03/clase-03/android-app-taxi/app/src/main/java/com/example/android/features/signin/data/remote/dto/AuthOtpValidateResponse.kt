package com.example.android.features.signin.data.remote.dto

import com.example.android.commons.data.local.dto.PassengerDto
import com.example.android.commons.data.local.dto.toDomain
import com.example.android.commons.domain.usecase.AuthResult
import com.example.android.features.signin.domain.model.SessionTokens

data class AuthOtpValidateResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: PassengerDto
)

fun AuthOtpValidateResponse.toDomain(): AuthResult = AuthResult(
    tokens = SessionTokens(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    ),
    user = user.toDomain()
)