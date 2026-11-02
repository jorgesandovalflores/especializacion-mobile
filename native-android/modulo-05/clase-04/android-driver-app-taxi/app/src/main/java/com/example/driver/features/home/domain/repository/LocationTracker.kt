package com.example.driver.features.home.domain.repository

interface LocationTracker {
    fun start(precise: Boolean)
    fun stop()
}
