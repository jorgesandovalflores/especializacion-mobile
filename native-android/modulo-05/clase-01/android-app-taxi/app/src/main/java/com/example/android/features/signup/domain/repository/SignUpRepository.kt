package com.example.android.features.signup.domain.repository

import com.example.android.commons.domain.model.Passenger

interface SignUpRepository {
    suspend fun signUp(givenName: String, familyName: String, email: String): Passenger
}
