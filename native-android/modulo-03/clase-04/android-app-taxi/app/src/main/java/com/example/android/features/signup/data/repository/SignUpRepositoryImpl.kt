package com.example.android.features.signup.data.repository

import com.example.android.commons.data.remote.dto.toDomain
import com.example.android.commons.domain.model.Passenger
import com.example.android.core.data.safeCall
import com.example.android.features.signup.data.remote.SignUpApi
import com.example.android.features.signup.data.remote.dto.SignUpRequest
import com.example.android.features.signup.domain.repository.SignUpRepository

class SignUpRepositoryImpl(
    private val api: SignUpApi
) : SignUpRepository {

    override suspend fun signUp(givenName: String, familyName: String, email: String): Passenger = safeCall {
        api.signUp(SignUpRequest(givenName = givenName, familyName = familyName, email = email)).toDomain()
    }
}
