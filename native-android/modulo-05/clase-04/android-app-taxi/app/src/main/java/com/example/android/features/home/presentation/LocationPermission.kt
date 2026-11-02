package com.example.android.features.home.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

sealed interface LocationPermission {
    data object Unknown : LocationPermission
    data class Granted(val precise: Boolean) : LocationPermission
    data class Denied(val blocked: Boolean) : LocationPermission
}

fun Context.currentLocationPermission(): LocationPermission {
    val fine = isGranted(Manifest.permission.ACCESS_FINE_LOCATION)
    val coarse = isGranted(Manifest.permission.ACCESS_COARSE_LOCATION)
    return when {
        fine -> LocationPermission.Granted(precise = true)
        coarse -> LocationPermission.Granted(precise = false)
        else -> LocationPermission.Denied(blocked = false)
    }
}

fun Map<String, Boolean>.toLocationPermission(canAskAgain: Boolean): LocationPermission = when {
    this[Manifest.permission.ACCESS_FINE_LOCATION] == true -> LocationPermission.Granted(precise = true)
    this[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> LocationPermission.Granted(precise = false)
    else -> LocationPermission.Denied(blocked = !canAskAgain)
}

private fun Context.isGranted(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
