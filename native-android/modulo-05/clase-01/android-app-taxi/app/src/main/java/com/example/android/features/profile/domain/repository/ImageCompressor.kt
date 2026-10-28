package com.example.android.features.profile.domain.repository

interface ImageCompressor {
    suspend fun compress(uri: String): ByteArray
}
