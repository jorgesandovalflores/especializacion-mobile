package com.example.android.features.signin.data.repository

import com.example.android.core.data.ErrorMapper
import com.example.android.features.signin.data.remote.AuthApi
import com.example.android.features.signin.data.remote.dto.AuthOtpGenerateRequest
import com.example.android.features.signin.data.remote.dto.AuthOtpValidateRequest
import com.example.android.features.signin.data.remote.dto.toDomain
import com.example.android.features.signin.domain.model.OtpGenerateResult
import com.example.android.features.signin.domain.model.OtpValidateResult
import com.example.android.features.signin.domain.repository.AuthRepository
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun otpGenerate(phone: String): OtpGenerateResult = safeCall {
        api.otpGenerate(AuthOtpGenerateRequest(phone = phone)).toDomain()
    }

    override suspend fun otpValidate(phone: String, code: String): OtpValidateResult = safeCall {
        api.otpValidate(AuthOtpValidateRequest(phone = phone, code = code)).toDomain()
    }

    private inline fun <T> safeCall(block: () -> T): T = try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (t: Throwable) {
        throw ErrorMapper.map(t)
    }
}
