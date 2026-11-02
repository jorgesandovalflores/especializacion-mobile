package com.example.driver.features.home.domain.usecase

import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.features.home.domain.model.UserLocation
import com.example.driver.features.home.domain.repository.TripRepository

class AvailabilityUseCase(
    private val repo: TripRepository,
    private val store: DriverStore
) {
    fun current(): Boolean = store.isOnline()

    suspend operator fun invoke(online: Boolean, location: UserLocation?): Boolean {
        val confirmed = repo.setAvailability(online, location?.latitude, location?.longitude)
        store.setOnline(confirmed)
        return confirmed
    }
}
