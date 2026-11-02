package com.example.driver.features.signin.data.remote

import com.example.driver.features.signin.data.remote.dto.LoginRequest
import com.example.driver.features.signin.data.remote.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface DriverAuthApi {
    @POST("driver/auth/login")
    suspend fun login(@Body body: LoginRequest): LoginResponse
}
