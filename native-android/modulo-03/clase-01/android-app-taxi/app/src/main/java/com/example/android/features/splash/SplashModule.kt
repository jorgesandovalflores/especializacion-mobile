package com.example.android.features.splash

import com.example.android.core.domain.SessionStore
import com.example.android.features.splash.domain.usecase.HasSessionUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object SplashModule {

    @Provides
    fun provideHasSessionUseCase(session: SessionStore): HasSessionUseCase = HasSessionUseCase(session)
}
