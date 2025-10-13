package com.example.android.features.signin.data.repository

import com.example.android.core.domain.ErrorMapper
import com.example.android.features.signin.data.remote.AuthApi
import com.example.android.features.signin.data.remote.dto.AuthOtpGenerateRequest
import com.example.android.features.signin.data.remote.dto.AuthOtpValidateRequest
import com.example.android.features.signin.data.remote.dto.toDomain
import com.example.android.features.signin.domain.repository.AuthRepository
import com.example.android.features.signin.domain.repository.OtpGenerateResult
import com.example.android.features.signin.domain.repository.OtpValidateResult

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {


    override suspend fun otpGenerate(phone: String): OtpGenerateResult {
        return runCatching {
            api.otpGenerate(AuthOtpGenerateRequest(phone = phone)).toDomain()
        }.getOrElse { throw ErrorMapper.map(it) }
    }

    override suspend fun otpValidate(phone: String, code: String): OtpValidateResult {
        return runCatching {
            api.otpValidate(AuthOtpValidateRequest(phone = phone, code = code)).toDomain()
        }.getOrElse { throw ErrorMapper.map(it) }
    }
}