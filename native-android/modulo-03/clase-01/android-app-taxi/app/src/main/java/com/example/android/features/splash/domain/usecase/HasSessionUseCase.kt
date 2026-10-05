package com.example.android.features.splash.domain.usecase

import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.first

class HasSessionUseCase(
    private val session: SessionStore
) {
    suspend operator fun invoke(): Boolean = !session.accessToken().first().isNullOrBlank()
}
