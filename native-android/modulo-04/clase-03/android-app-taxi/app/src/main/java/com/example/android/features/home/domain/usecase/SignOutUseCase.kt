package com.example.android.features.home.domain.usecase

import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionStore
import com.example.android.features.home.domain.repository.PushTokenRepository

class SignOutUseCase(
    private val session: SessionStore,
    private val localCache: LocalCache,
    private val push: PushTokenRepository
) {
    suspend operator fun invoke() {
        push.delete()
        session.clear()
        localCache.clear()
    }
}
