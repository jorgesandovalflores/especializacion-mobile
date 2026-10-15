package com.example.android.features.home

import com.example.android.features.home.domain.model.UserLocation
import com.example.android.features.home.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow

class FakeLocationRepository : LocationRepository {
    val updates = MutableSharedFlow<UserLocation>(replay = 1)
    var failure: Throwable? = null
    val requests = mutableListOf<Boolean>()
    var activeCollectors = 0
        private set

    override fun observeLocation(precise: Boolean): Flow<UserLocation> = flow {
        requests += precise
        failure?.let { throw it }
        activeCollectors++
        try {
            updates.collect { emit(it) }
        } finally {
            activeCollectors--
        }
    }
}

fun userLocation(
    latitude: Double = -12.09713,
    longitude: Double = -77.03654,
    accuracyMeters: Float = 12f
) = UserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracyMeters,
    timeMillis = 0L,
    isMock = false
)
