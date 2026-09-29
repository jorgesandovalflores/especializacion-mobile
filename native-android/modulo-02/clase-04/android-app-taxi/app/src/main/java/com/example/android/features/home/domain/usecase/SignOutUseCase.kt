package com.example.android.features.home.domain.usecase

import com.example.android.core.domain.SessionStore

class SignOutUseCase(
    private val session: SessionStore
) {
    suspend operator fun invoke() = session.clear()
}
