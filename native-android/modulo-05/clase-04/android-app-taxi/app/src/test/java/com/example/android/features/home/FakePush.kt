package com.example.android.features.home

import com.example.android.core.domain.DomainException
import com.example.android.features.home.domain.repository.PushTokenRepository

class FakePushTokenRepository(
    var token: String? = "fcm-token"
) : PushTokenRepository {
    val registered = mutableListOf<String>()
    var deletes = 0
        private set
    var failure: DomainException? = null

    override suspend fun currentToken(): String? = token

    override suspend fun register(token: String) {
        failure?.let { throw it }
        registered += token
    }

    override fun delete() {
        deletes++
    }
}
