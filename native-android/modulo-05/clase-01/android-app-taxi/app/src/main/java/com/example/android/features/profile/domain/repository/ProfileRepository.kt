package com.example.android.features.profile.domain.repository

import com.example.android.commons.domain.model.Passenger

interface ProfileRepository {
    suspend fun me(): Passenger
    suspend fun savePhoto(url: String): Passenger
}
