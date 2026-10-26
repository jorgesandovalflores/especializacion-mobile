package com.example.android.features.profile.data.remote

import com.example.android.commons.data.remote.dto.PassengerDto
import com.example.android.features.profile.data.remote.dto.PhotoRequest
import com.example.android.features.profile.data.remote.dto.StorageTokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface ProfileApi {
    @GET("passenger/me")
    suspend fun me(): PassengerDto

    @POST("passenger/storage-token")
    suspend fun storageToken(): StorageTokenResponse

    @PUT("passenger/photo")
    suspend fun savePhoto(@Body body: PhotoRequest): PassengerDto
}
