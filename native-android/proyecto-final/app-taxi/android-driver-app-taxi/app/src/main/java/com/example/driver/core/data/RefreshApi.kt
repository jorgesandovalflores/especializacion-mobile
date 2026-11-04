package com.example.driver.core.data

import com.example.driver.core.data.dto.RefreshRequest
import com.example.driver.core.data.dto.RefreshResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RefreshApi {
    @POST("driver/auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): RefreshResponse
}
