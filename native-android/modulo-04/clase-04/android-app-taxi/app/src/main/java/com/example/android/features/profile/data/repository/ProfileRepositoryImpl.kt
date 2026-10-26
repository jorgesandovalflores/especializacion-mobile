package com.example.android.features.profile.data.repository

import com.example.android.commons.data.remote.dto.toDomain
import com.example.android.commons.domain.model.Passenger
import com.example.android.core.data.safeCall
import com.example.android.features.profile.data.remote.ProfileApi
import com.example.android.features.profile.data.remote.dto.PhotoRequest
import com.example.android.features.profile.domain.repository.ProfileRepository

class ProfileRepositoryImpl(
    private val api: ProfileApi
) : ProfileRepository {

    override suspend fun me(): Passenger = safeCall { api.me().toDomain() }

    override suspend fun savePhoto(url: String): Passenger = safeCall {
        api.savePhoto(PhotoRequest(url)).toDomain()
    }
}
