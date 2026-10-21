package com.example.android.features.home.data.push

import android.content.Context
import com.example.android.core.data.safeCall
import com.example.android.features.home.data.push.dto.DeviceRequest
import com.example.android.features.home.domain.repository.PushTokenRepository
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

class PushTokenRepositoryImpl(
    private val context: Context,
    private val api: DeviceApi
) : PushTokenRepository {

    override suspend fun currentToken(): String? =
        messaging()?.let { safeCall { it.token.await() } }

    override suspend fun register(token: String) = safeCall {
        api.register(DeviceRequest(token))
    }

    override fun delete() {
        messaging()?.deleteToken()
    }

    private fun messaging(): FirebaseMessaging? =
        if (FirebaseApp.getApps(context).isEmpty()) null else FirebaseMessaging.getInstance()
}
