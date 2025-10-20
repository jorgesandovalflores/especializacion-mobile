package com.example.android.commons.domain.usecase

import com.example.android.commons.domain.model.Passenger
import com.example.android.features.signin.domain.model.SessionTokens

data class AuthResult(
    val tokens: SessionTokens,
    val user: Passenger
)
