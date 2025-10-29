package com.example.android.core.data

import com.example.android.core.data.dto.RefreshDto
import com.example.android.features.signin.data.remote.dto.AuthOtpValidateResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshDto): AuthOtpValidateResponse
}