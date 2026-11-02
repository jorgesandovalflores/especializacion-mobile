package com.example.driver.core.domain

import kotlinx.coroutines.flow.Flow

interface SessionExpiration {
    val events: Flow<Unit>
    suspend fun expire()
}
