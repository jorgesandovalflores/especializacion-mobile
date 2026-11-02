package com.example.android.features.trip.domain.usecase

import com.example.android.core.domain.realtime.RealtimeClient
import com.example.android.core.domain.realtime.RealtimeConnection
import com.example.android.features.trip.domain.model.TripEvent
import com.example.android.features.trip.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

class ObserveTripEventsUseCase(
    private val repository: TripRepository,
    private val realtime: RealtimeClient
) {
    operator fun invoke(): Flow<TripEvent> {
        realtime.connect()
        return repository.events()
    }

    fun connected(): Flow<Unit> = realtime.connection
        .map { it == RealtimeConnection.Connected }
        .distinctUntilChanged()
        .filter { it }
        .map { }
}
