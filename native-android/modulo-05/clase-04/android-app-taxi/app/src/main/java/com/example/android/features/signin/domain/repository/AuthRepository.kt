package com.example.android.features.signin.domain.repository

import com.example.android.features.signin.domain.model.OtpGenerateResult
import com.example.android.features.signin.domain.model.OtpValidateResult

interface AuthRepository {
    suspend fun otpGenerate(phone: String): OtpGenerateResult
    suspend fun otpValidate(phone: String, code: String): OtpValidateResult
}
