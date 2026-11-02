package com.example.driver.features.signin.domain.usecase

import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.SessionStore
import com.example.driver.core.domain.toDomainException
import com.example.driver.features.signin.domain.repository.DriverAuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

internal const val COUNTRY_CODE = "51"
internal const val PHONE_DIGITS = 9

sealed interface LoginState {
    data object Idle : LoginState
    data object Loading : LoginState
    data class Success(val givenName: String) : LoginState
    data class Error(val message: String) : LoginState
}

class LoginUseCase(
    private val repo: DriverAuthRepository,
    private val session: SessionStore,
    private val driverStore: DriverStore
) {
    operator fun invoke(phone: String, password: String): Flow<LoginState> = flow {
        emit(LoginState.Loading)
        val result = repo.login(phone = "$COUNTRY_CODE$phone", password = password)
        session.saveTokens(access = result.accessToken, refresh = result.refreshToken)
        driverStore.clear()
        driverStore.saveDriver(result.driver)
        emit(LoginState.Success(result.driver.givenName))
    }.catch { emit(LoginState.Error(it.toDomainException().message)) }

    companion object {
        fun canSubmit(phone: String, password: String): Boolean =
            phone.length == PHONE_DIGITS && phone.all(Char::isDigit) && password.isNotBlank()
    }
}
