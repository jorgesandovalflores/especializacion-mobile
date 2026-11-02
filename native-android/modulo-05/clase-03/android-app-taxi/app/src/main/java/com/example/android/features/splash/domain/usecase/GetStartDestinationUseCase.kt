package com.example.android.features.splash.domain.usecase

import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.first

enum class StartDestination { SignIn, SignUp, Home }

class GetStartDestinationUseCase(
    private val session: SessionStore
) {
    suspend operator fun invoke(): StartDestination = when {
        session.accessToken().first().isNullOrBlank() -> StartDestination.SignIn
        session.registrationPending().first() -> StartDestination.SignUp
        else -> StartDestination.Home
    }
}
