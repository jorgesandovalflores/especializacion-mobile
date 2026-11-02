package com.example.driver.features.home.data.tracking

import android.annotation.SuppressLint
import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.driver.R
import com.example.driver.features.home.domain.model.UserLocation
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TrackingNotification(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    init {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(context.getString(R.string.home_tracking_channel_name))
                .setDescription(context.getString(R.string.home_tracking_channel_description))
                .build()
        )
    }

    fun build(location: UserLocation?): Notification =
        NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_tracking_location)
            .setContentTitle(context.getString(R.string.home_tracking_title))
            .setContentText(trackingText(location))
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

    private fun trackingText(location: UserLocation?): String =
        if (location == null) {
            context.getString(R.string.home_tracking_searching)
        } else {
            context.getString(
                R.string.home_tracking_text,
                location.accuracyMeters.toInt(),
                location.timeMillis.toClockTime(ZoneId.systemDefault())
            )
        }

    private fun openApp(): PendingIntent? =
        context.packageManager.getLaunchIntentForPackage(context.packageName)?.let {
            PendingIntent.getActivity(context, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }

    companion object {
        const val ID = 2001
        private const val CHANNEL_ID = "driver_shift"
    }
}

private val TIME = DateTimeFormatter.ofPattern("H:mm")

fun Long.toClockTime(zone: ZoneId): String = TIME.format(Instant.ofEpochMilli(this).atZone(zone))
