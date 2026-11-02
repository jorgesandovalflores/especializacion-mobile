package com.example.driver.core.domain

import kotlinx.coroutines.flow.Flow

interface SessionStore {
    suspend fun saveTokens(access: String, refresh: String)
    fun accessToken(): Flow<String?>
    fun refreshToken(): Flow<String?>
    suspend fun clear()
}
