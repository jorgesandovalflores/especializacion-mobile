package com.example.android.features.home.data.tracking

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.example.android.features.home.domain.repository.LocationTracker

class LocationTrackerImpl(
    private val context: Context
) : LocationTracker {

    override fun start(precise: Boolean) {
        try {
            ContextCompat.startForegroundService(context, LocationTrackingService.intent(context, precise))
        } catch (e: IllegalStateException) {
        }
    }

    override fun stop() {
        context.stopService(Intent(context, LocationTrackingService::class.java))
    }
}
