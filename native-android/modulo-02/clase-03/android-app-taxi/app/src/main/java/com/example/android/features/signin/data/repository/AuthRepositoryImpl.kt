package com.example.android.features.signin.data.repository

import com.example.android.features.signin.data.mapper.toDomain
import com.example.android.features.signin.data.remote.AuthApi
import com.example.android.features.signin.data.remote.dto.PassengerLoginRequest
import com.example.android.features.signin.domain.repository.AuthRepository
import com.example.android.features.signin.domain.repository.SignInResult
import retrofit2.HttpException

class AuthRepositoryImpl(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun signInWithPhone(phone: String): SignInResult {
        try {
            val res = api.login(PassengerLoginRequest(phone))
            return res.toDomain()
        } catch (e: HttpException) {
            val msg = e.response()?.errorBody()?.string()?.takeIf { it.isNotBlank() }
            throw RuntimeException(msg ?: e.message())
        } catch (t: Throwable) {
            throw RuntimeException(t.message ?: "Sign in failed")
        }
    }
}