package com.example.driver.features.home.domain.repository

import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.features.home.domain.model.TripEvent
import kotlinx.coroutines.flow.Flow

interface TripEventsRepository {
    val connection: Flow<ConnectionState>
    fun events(): Flow<TripEvent>
    fun connect()
    fun disconnect()
}
