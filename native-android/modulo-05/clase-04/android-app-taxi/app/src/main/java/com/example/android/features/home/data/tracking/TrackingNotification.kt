package com.example.android.features.home.data.tracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.android.R
import com.example.android.features.home.domain.model.UserLocation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TrackingNotification(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    init {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName("Ubicación en segundo plano")
                .setDescription("Se muestra mientras el mapa de inicio está abierto")
                .build()
        )
    }

    fun build(location: UserLocation?): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tracking_location)
            .setContentTitle("Compartiendo tu ubicación")
            .setContentText(location.toTrackingText(ZoneId.systemDefault()))
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(openApp())
            .build()

    @SuppressLint("MissingPermission")
    fun update(location: UserLocation) {
        if (manager.areNotificationsEnabled()) manager.notify(ID, build(location))
    }

    private fun openApp(): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

    companion object {
        const val ID = 1001
        private const val CHANNEL_ID = "location_tracking"
    }
}

private val TIME = DateTimeFormatter.ofPattern("H:mm")

fun UserLocation?.toTrackingText(zone: ZoneId): String =
    if (this == null) {
        "Buscando tu ubicación…"
    } else {
        "Precisión ±${accuracyMeters.toInt()} m · ${TIME.format(Instant.ofEpochMilli(timeMillis).atZone(zone))}"
    }
