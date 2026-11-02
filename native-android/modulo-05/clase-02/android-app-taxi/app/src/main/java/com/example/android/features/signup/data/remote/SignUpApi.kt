package com.example.android.features.signup.data.remote

import com.example.android.commons.data.remote.dto.PassengerDto
import com.example.android.features.signup.data.remote.dto.SignUpRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface SignUpApi {
    @PUT("passenger/signup")
    suspend fun signUp(@Body body: SignUpRequest): PassengerDto
}
