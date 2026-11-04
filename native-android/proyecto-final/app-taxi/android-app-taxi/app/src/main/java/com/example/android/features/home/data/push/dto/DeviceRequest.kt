package com.example.android.features.home.data.push.dto

data class DeviceRequest(
    val token: String,
    val platform: String = "ANDROID"
)
