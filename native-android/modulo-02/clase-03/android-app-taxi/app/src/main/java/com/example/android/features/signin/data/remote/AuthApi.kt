package com.example.android.features.signin.data.remote

import com.example.android.features.signin.data.remote.dto.PassengerLoginRequest
import com.example.android.features.signin.data.remote.dto.PassengerLoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("passenger/login")
    suspend fun login(@Body body: PassengerLoginRequest): PassengerLoginResponse
}