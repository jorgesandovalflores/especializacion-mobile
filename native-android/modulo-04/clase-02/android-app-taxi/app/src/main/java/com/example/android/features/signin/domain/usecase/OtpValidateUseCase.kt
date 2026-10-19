package com.example.android.features.signin.domain.usecase

import com.example.android.commons.domain.enum.PassengerStatusEnum
import com.example.android.core.domain.SessionStore
import com.example.android.core.domain.toDomainException
import com.example.android.features.signin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed interface OtpValidateState {
    data object Idle : OtpValidateState
    data object Loading : OtpValidateState
    data class Success(val showRegister: Boolean) : OtpValidateState
    data class Error(val message: String) : OtpValidateState
}

class OtpValidateUseCase(
    private val repo: AuthRepository,
    private val session: SessionStore
) {
    operator fun invoke(phoneReq: String, code: String): Flow<OtpValidateState> = flow {
        emit(OtpValidateState.Loading)
        val result = repo.otpValidate(phone = "$COUNTRY_CODE$phoneReq", code = code)
        session.saveTokens(
            access = result.tokens.accessToken,
            refresh = result.tokens.refreshToken
        )
        val showRegister = result.user.status == PassengerStatusEnum.INACTIVE_REGISTER.value
        session.setRegistrationPending(showRegister)
        emit(OtpValidateState.Success(showRegister = showRegister))
    }.catch { emit(OtpValidateState.Error(it.toDomainException().message)) }
}
