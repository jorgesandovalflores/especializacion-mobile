package com.example.android.features.signin.domain.usecase

import com.example.android.core.domain.SessionStore
import com.example.android.features.signin.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

sealed interface SignInState {
    data object Idle : SignInState
    data object Loading : SignInState
    data class Success(val welcomeName: String) : SignInState
    data class Error(val message: String) : SignInState
}

class SignInWithPhoneUseCase(
    private val repo: AuthRepository,
    private val session: SessionStore
) {
    operator fun invoke(phoneReq: String): Flow<SignInState> = flow {
        val phone = "51$phoneReq"
        emit(SignInState.Loading)

        try {
            val result = repo.signInWithPhone(phone)
            session.saveTokens(
                result.tokens.accessToken,
                result.tokens.refreshToken
            )
            emit(SignInState.Success(result.user.givenName ?: result.user.phoneNumber))
        } catch (t: Throwable) {
            emit(SignInState.Error(t.message ?: "No se pudo iniciar sesión"))
        }
    }
}