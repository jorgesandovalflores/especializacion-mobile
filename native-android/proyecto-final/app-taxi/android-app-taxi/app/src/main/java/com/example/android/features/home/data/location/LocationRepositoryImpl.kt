package com.example.android.features.home.data.location

import android.annotation.SuppressLint
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Looper
import androidx.core.location.LocationManagerCompat
import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.LocationException
import com.example.android.features.home.domain.model.UserLocation
import com.example.android.features.home.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LocationRepositoryImpl(
    private val client: FusedLocationProviderClient,
    private val locationManager: LocationManager,
    private val now: () -> Long = System::currentTimeMillis
) : LocationRepository {

    @SuppressLint("MissingPermission")
    override fun observeLocation(precise: Boolean): Flow<UserLocation> = callbackFlow {
        if (!LocationManagerCompat.isLocationEnabled(locationManager)) {
            throw LocationException(LocationError.SettingsDisabled)
        }

        val request = LocationRequest.Builder(priorityFor(precise), UPDATE_INTERVAL_MS)
            .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL_MS)
            .setMinUpdateDistanceMeters(MIN_UPDATE_DISTANCE_M)
            .build()

        val firstFixTimeout = launch {
            delay(FIRST_FIX_TIMEOUT_MS)
            close(LocationException(LocationError.Unavailable))
        }

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                firstFixTimeout.cancel()
                trySend(location.toDomain())
            }
        }

        try {
            client.lastLocation.await()
                ?.takeIf { now() - it.time <= LAST_KNOWN_MAX_AGE_MS }
                ?.let {
                    firstFixTimeout.cancel()
                    send(it.toDomain())
                }
            client.requestLocationUpdates(request, callback, Looper.getMainLooper()).await()
        } catch (e: SecurityException) {
            throw LocationException(LocationError.PermissionMissing)
        }

        awaitClose { client.removeLocationUpdates(callback) }
    }

    private fun priorityFor(precise: Boolean): Int =
        if (precise) Priority.PRIORITY_HIGH_ACCURACY else Priority.PRIORITY_BALANCED_POWER_ACCURACY

    private companion object {
        const val UPDATE_INTERVAL_MS = 5_000L
        const val MIN_UPDATE_INTERVAL_MS = 2_000L
        const val MIN_UPDATE_DISTANCE_M = 5f
        const val FIRST_FIX_TIMEOUT_MS = 15_000L
        const val LAST_KNOWN_MAX_AGE_MS = 120_000L
    }
}

fun Location.toDomain(): UserLocation = UserLocation(
    latitude = latitude,
    longitude = longitude,
    accuracyMeters = accuracy,
    timeMillis = time,
    isMock = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) isMock else @Suppress("DEPRECATION") isFromMockProvider
)
