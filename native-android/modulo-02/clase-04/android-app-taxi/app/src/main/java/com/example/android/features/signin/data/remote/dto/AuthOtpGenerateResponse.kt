package com.example.android.features.signin.data.remote.dto

import com.example.android.features.signin.domain.repository.OtpGenerateResult

data class AuthOtpGenerateResponse(
    val success: Boolean,
    val expiresAt: String,
    val ttlSec: Int,
    val messageId: String
)

fun AuthOtpGenerateResponse.toDomain(): OtpGenerateResult = OtpGenerateResult(
    expiresAt = this.expiresAt
)