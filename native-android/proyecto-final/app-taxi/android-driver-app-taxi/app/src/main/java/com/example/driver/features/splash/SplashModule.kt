package com.example.driver.features.splash

import com.example.driver.core.domain.SessionStore
import com.example.driver.features.splash.domain.usecase.GetStartDestinationUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SplashModule {

    @Provides
    fun provideGetStartDestinationUseCase(session: SessionStore): GetStartDestinationUseCase =
        GetStartDestinationUseCase(session)
}
