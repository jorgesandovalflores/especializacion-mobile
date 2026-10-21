package com.example.android.features.home.domain.repository

interface PushTokenRepository {
    suspend fun currentToken(): String?
    suspend fun register(token: String)
    fun delete()
}
