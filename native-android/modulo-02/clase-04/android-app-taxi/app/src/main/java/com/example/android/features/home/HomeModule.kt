package com.example.android.features.home

import com.example.android.core.domain.SessionStore
import com.example.android.features.home.domain.usecase.SignOutUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object HomeModule {

    @Provides
    fun provideSignOutUseCase(session: SessionStore): SignOutUseCase = SignOutUseCase(session)
}
