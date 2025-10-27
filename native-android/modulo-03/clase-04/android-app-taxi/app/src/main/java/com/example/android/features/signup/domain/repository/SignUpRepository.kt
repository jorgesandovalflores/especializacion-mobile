package com.example.android.features.signup.domain.repository

import com.example.android.commons.domain.usecase.AuthResult

interface SignUpRepository {
    suspend fun signUpRemote(givenName: String, familyName: String, photoUrl: String, email: String): AuthResult
}