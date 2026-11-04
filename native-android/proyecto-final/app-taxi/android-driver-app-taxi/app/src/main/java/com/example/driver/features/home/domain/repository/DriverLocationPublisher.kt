package com.example.driver.features.home.domain.repository

import com.example.driver.features.home.domain.model.UserLocation

interface DriverLocationPublisher {
    fun start()
    fun publish(location: UserLocation): Boolean
}
