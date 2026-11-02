package com.example.android.features.home.data.tracking

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.example.android.features.home.domain.repository.LocationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@AndroidEntryPoint
class LocationTrackingService : Service() {

    @Inject
    lateinit var repository: LocationRepository

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val notification by lazy { TrackingNotification(this) }
    private var tracking: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        try {
            ServiceCompat.startForeground(
                this,
                TrackingNotification.ID,
                notification.build(location = null),
                ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            )
        } catch (e: SecurityException) {
            stopSelf()
            return START_NOT_STICKY
        }

        val precise = intent?.getBooleanExtra(EXTRA_PRECISE, true) ?: true
        tracking?.cancel()
        tracking = repository.observeLocation(precise)
            .onEach(notification::update)
            .catch { stopSelf() }
            .launchIn(scope)

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_PRECISE = "precise"

        fun intent(context: Context, precise: Boolean): Intent =
            Intent(context, LocationTrackingService::class.java).putExtra(EXTRA_PRECISE, precise)
    }
}
