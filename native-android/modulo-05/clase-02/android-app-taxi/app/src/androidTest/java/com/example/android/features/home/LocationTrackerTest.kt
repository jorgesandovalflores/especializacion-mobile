package com.example.android.features.home

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import android.os.SystemClock
import android.service.notification.StatusBarNotification
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.core.presentation.activity.MainActivity
import com.example.android.features.home.data.tracking.LocationTrackerImpl
import com.example.android.features.home.data.tracking.TrackingNotification
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LocationTrackerTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)
    private val tracker = LocationTrackerImpl(context)
    private lateinit var scenario: ActivityScenario<MainActivity>

    @Before
    fun setUp() {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) add(Manifest.permission.POST_NOTIFICATIONS)
        }
        permissions.forEach { instrumentation.uiAutomation.grantRuntimePermission(context.packageName, it) }
        scenario = ActivityScenario.launch(MainActivity::class.java)
    }

    @After
    fun tearDown() {
        tracker.stop()
        scenario.close()
    }

    @Test
    fun start_publishesOngoingNotification_andStopRemovesIt() {
        tracker.start(precise = true)

        val notification = awaitNotification { it != null }
        assertTrue(notification!!.isOngoing)
        assertEquals("Compartiendo tu ubicación", notification.notification.extras.getString("android.title"))

        tracker.stop()

        assertNull(awaitNotification { it == null })
    }

    private fun awaitNotification(condition: (StatusBarNotification?) -> Boolean): StatusBarNotification? {
        val deadline = SystemClock.uptimeMillis() + TIMEOUT_MS
        var current = trackingNotification()
        while (!condition(current) && SystemClock.uptimeMillis() < deadline) {
            SystemClock.sleep(100)
            current = trackingNotification()
        }
        return current
    }

    private fun trackingNotification(): StatusBarNotification? =
        notifications.activeNotifications.firstOrNull { it.id == TrackingNotification.ID }

    private companion object {
        const val TIMEOUT_MS = 5_000L
    }
}
