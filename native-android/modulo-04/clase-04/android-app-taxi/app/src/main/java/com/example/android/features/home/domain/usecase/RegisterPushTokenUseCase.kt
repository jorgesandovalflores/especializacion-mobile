package com.example.android.features.home.domain.usecase

import com.example.android.core.domain.DomainException
import com.example.android.core.domain.SessionStore
import com.example.android.features.home.domain.repository.PushTokenRepository
import kotlinx.coroutines.flow.first

class RegisterPushTokenUseCase(
    private val repo: PushTokenRepository,
    private val session: SessionStore
) {
    suspend operator fun invoke(token: String? = null): Boolean {
        if (session.accessToken().first() == null) return false
        val current = token ?: repo.currentToken() ?: return false
        return try {
            repo.register(current)
            true
        } catch (e: DomainException) {
            false
        }
    }
}
