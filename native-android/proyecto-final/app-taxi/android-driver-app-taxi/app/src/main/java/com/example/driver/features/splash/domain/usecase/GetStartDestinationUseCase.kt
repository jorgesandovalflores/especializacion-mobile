package com.example.driver.features.splash.domain.usecase

import com.example.driver.core.domain.SessionStore
import kotlinx.coroutines.flow.first

enum class StartDestination { SignIn, Home }

class GetStartDestinationUseCase(
    private val session: SessionStore
) {
    suspend operator fun invoke(): StartDestination =
        if (session.accessToken().first().isNullOrBlank()) StartDestination.SignIn else StartDestination.Home
}
