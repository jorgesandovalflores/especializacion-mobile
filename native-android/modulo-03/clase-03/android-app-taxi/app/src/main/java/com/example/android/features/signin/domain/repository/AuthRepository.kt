package com.example.android.features.signin.domain.repository

import com.example.android.commons.domain.usecase.AuthResult

data class OtpGenerateResult(
    val expiresAt: String
)

interface AuthRepository {
    suspend fun otpGenerate(phone: String): OtpGenerateResult
    suspend fun otpValidate(phone: String, code: String): AuthResult
}