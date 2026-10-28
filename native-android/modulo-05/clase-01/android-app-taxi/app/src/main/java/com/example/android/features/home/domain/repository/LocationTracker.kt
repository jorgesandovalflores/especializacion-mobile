package com.example.android.features.home.domain.repository

interface LocationTracker {
    fun start(precise: Boolean)
    fun stop()
}
