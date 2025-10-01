package com.example.android.features.signin.domain.repository

import com.example.android.features.signin.domain.model.SessionTokens
import com.example.android.features.signin.domain.model.Passenger

data class SignInResult(
    val tokens: SessionTokens,
    val user: Passenger
)

interface AuthRepository {
    suspend fun signInWithPhone(phone: String): SignInResult
}