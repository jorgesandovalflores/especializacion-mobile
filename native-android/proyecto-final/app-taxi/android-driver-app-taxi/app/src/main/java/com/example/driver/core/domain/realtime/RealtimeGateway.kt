package com.example.driver.core.domain.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface RealtimeGateway {
    val connection: StateFlow<ConnectionState>
    fun connect()
    fun disconnect()
    fun on(event: String): Flow<String>
    fun emit(event: String, payload: Map<String, Any?>): Boolean
}
