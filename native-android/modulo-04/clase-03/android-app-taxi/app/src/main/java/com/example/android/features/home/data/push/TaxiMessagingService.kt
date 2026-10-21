package com.example.android.features.home.data.push

import com.example.android.features.home.domain.usecase.RegisterPushTokenUseCase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaxiMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var registerPushToken: RegisterPushTokenUseCase

    @Inject
    lateinit var notifier: PushNotifier

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        scope.launch { registerPushToken(token) }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        message.data.toPushMessage()?.let(notifier::show)
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }
}
