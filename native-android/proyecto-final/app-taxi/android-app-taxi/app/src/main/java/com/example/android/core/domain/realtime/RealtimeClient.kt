package com.example.android.core.domain.realtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

enum class RealtimeConnection { Disconnected, Connecting, Connected }

interface RealtimeClient {
    val connection: StateFlow<RealtimeConnection>
    fun connect()
    fun disconnect()
    fun on(event: String): Flow<String>
}
