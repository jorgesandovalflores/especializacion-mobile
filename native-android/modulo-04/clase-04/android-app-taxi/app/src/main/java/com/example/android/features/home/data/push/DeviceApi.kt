package com.example.android.features.home.data.push

import com.example.android.features.home.data.push.dto.DeviceRequest
import retrofit2.http.Body
import retrofit2.http.PUT

interface DeviceApi {
    @PUT("passenger/device")
    suspend fun register(@Body body: DeviceRequest)
}
