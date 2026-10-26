package com.example.android.features.home.data.push

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.android.R
import com.example.android.core.presentation.activity.MainActivity
import com.example.android.features.home.domain.model.PushMessage

class PushNotifier(private val context: Context) {

    private val manager = NotificationManagerCompat.from(context)

    init {
        manager.createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_HIGH)
                .setName("Viajes y avisos")
                .setDescription("Llegada del conductor, estado del viaje y avisos de tu cuenta")
                .build()
        )
    }

    @SuppressLint("MissingPermission")
    fun show(message: PushMessage): Boolean {
        if (!manager.areNotificationsEnabled()) return false
        val id = SystemClock.elapsedRealtime().toInt()
        manager.notify(
            id,
            NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_push_taxi)
                .setContentTitle(message.title)
                .setContentText(message.body)
                .setStyle(NotificationCompat.BigTextStyle().bigText(message.body))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_STATUS)
                .setAutoCancel(true)
                .setContentIntent(open(id, message.deeplink))
                .build()
        )
        return true
    }

    private fun open(requestCode: Int, deeplink: String): PendingIntent =
        PendingIntent.getActivity(
            context,
            requestCode,
            Intent(context, MainActivity::class.java)
                .putExtra(MainActivity.EXTRA_DEEPLINK, deeplink)
                .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

    companion object {
        const val CHANNEL_ID = "trips"
    }
}
