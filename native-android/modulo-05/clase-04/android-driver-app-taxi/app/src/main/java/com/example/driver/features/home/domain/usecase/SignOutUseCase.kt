package com.example.driver.features.home.domain.usecase

import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.LocalCache
import com.example.driver.core.domain.SessionStore
import com.example.driver.features.home.domain.repository.LocationTracker
import com.example.driver.features.home.domain.repository.TripEventsRepository
import com.example.driver.features.home.domain.repository.TripRepository
import kotlin.coroutines.cancellation.CancellationException

class SignOutUseCase(
    private val repo: TripRepository,
    private val store: DriverStore,
    private val tracker: LocationTracker,
    private val events: TripEventsRepository,
    private val session: SessionStore,
    private val localCache: LocalCache
) {
    suspend operator fun invoke() {
        if (store.isOnline()) {
            try {
                repo.setAvailability(online = false, latitude = null, longitude = null)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }
        tracker.stop()
        events.disconnect()
        session.clear()
        localCache.clear()
    }
}
