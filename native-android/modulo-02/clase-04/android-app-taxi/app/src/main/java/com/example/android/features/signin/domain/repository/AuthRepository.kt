package com.example.android.features.signin.domain.repository

import com.example.android.features.signin.domain.model.SessionTokens
import com.example.android.commons.domain.model.Passenger

data class OtpGenerateResult(
    val expiresAt: String
)

data class OtpValidateResult(
    val tokens: SessionTokens,
    val user: Passenger
)

interface AuthRepository {
    suspend fun otpGenerate(phone: String): OtpGenerateResult
    suspend fun otpValidate(phone: String, code: String): OtpValidateResult
}