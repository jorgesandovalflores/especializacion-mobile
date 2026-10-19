package com.example.android.features.home.domain.usecase

import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionStore

class SignOutUseCase(
    private val session: SessionStore,
    private val localCache: LocalCache
) {
    suspend operator fun invoke() {
        session.clear()
        localCache.clear()
    }
}
