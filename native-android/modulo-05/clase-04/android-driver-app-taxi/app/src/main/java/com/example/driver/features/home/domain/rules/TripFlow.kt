package com.example.driver.features.home.domain.rules

import com.example.driver.features.home.domain.model.TripStatus

object TripFlow {
    fun next(status: TripStatus): TripStatus? = when (status) {
        TripStatus.ACCEPTED -> TripStatus.ARRIVED
        TripStatus.ARRIVED -> TripStatus.IN_PROGRESS
        TripStatus.IN_PROGRESS -> TripStatus.COMPLETED
        else -> null
    }
}
