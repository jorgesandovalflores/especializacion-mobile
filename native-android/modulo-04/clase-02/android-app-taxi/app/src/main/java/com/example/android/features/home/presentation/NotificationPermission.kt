package com.example.android.features.home.presentation

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

const val NOTIFICATION_PERMISSION = Manifest.permission.POST_NOTIFICATIONS

fun Context.needsNotificationPermission(): Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
        ContextCompat.checkSelfPermission(this, NOTIFICATION_PERMISSION) != PackageManager.PERMISSION_GRANTED
