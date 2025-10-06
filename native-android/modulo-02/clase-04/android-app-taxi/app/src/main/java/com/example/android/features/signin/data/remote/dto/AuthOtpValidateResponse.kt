package com.example.android.features.signin.data.remote.dto

import com.example.android.commons.data.remote.dto.PassengerDto
import com.example.android.commons.data.remote.dto.toDomain
import com.example.android.features.signin.domain.model.SessionTokens
import com.example.android.features.signin.domain.repository.OtpValidateResult

data class AuthOtpValidateResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: PassengerDto
)

fun AuthOtpValidateResponse.toDomain(): OtpValidateResult = OtpValidateResult(
    tokens = SessionTokens(
        accessToken = this.accessToken,
        refreshToken = this.refreshToken
    ),
    user = user.toDomain()
)