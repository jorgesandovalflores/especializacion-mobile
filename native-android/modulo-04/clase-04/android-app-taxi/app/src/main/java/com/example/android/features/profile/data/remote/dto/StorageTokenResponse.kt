package com.example.android.features.profile.data.remote.dto

data class StorageTokenResponse(
    val token: String,
    val bucket: String,
    val photoPath: String
)
