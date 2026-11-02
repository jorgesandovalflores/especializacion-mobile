package com.example.android.features.splash

import com.example.android.core.domain.SessionStore
import com.example.android.features.splash.domain.usecase.GetStartDestinationUseCase
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
