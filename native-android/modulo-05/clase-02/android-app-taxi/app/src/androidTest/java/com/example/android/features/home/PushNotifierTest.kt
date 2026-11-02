package com.example.android.features.home

import android.Manifest
import android.app.NotificationManager
import android.os.Build
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.features.home.data.push.PushNotifier
import com.example.android.features.home.domain.model.PushMessage
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PushNotifierTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val notifications = context.getSystemService(NotificationManager::class.java)

    @Before
    fun setUp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            instrumentation.uiAutomation.grantRuntimePermission(context.packageName, Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    @After
    fun tearDown() = notifications.cancelAll()

    @Test
    fun show_publishesHighImportanceNotificationThatOpensTheApp() {
        val shown = PushNotifier(context).show(
            PushMessage("Tu taxi está llegando", "Carlos llega en 3 min", "app-taxi://passenger/menu")
        )

        assertTrue(shown)
        val channel = notifications.getNotificationChannel(PushNotifier.CHANNEL_ID)
        assertEquals(NotificationManager.IMPORTANCE_HIGH, channel.importance)
        val posted = notifications.activeNotifications.single { it.notification.channelId == PushNotifier.CHANNEL_ID }
        assertEquals("Tu taxi está llegando", posted.notification.extras.getString("android.title"))
        assertNotNull(posted.notification.contentIntent)
    }
}
