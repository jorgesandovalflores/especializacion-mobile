package com.example.android.features.signin.domain.usecase

import com.example.android.core.domain.toDomainException
import com.example.android.features.signin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed interface OtpGenerateState {
    data object Idle : OtpGenerateState
    data object Loading : OtpGenerateState
    data class Success(val phone: String, val expiresAt: String) : OtpGenerateState
    data class Error(val message: String) : OtpGenerateState
}

class OtpGenerateUseCase(
    private val repo: AuthRepository
) {
    operator fun invoke(phoneReq: String): Flow<OtpGenerateState> = flow {
        emit(OtpGenerateState.Loading)
        val result = repo.otpGenerate(phone = "$COUNTRY_CODE$phoneReq")
        emit(OtpGenerateState.Success(phone = phoneReq, expiresAt = result.expiresAt))
    }.catch { emit(OtpGenerateState.Error(it.toDomainException().message)) }
}
