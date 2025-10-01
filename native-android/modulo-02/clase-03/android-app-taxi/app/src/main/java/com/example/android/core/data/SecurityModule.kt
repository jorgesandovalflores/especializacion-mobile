package com.example.android.core.data

import android.content.Context
import com.example.android.core.domain.SessionStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {

    @Provides @Singleton
    fun provideSessionStore(@ApplicationContext ctx: Context): SessionStore =
        SessionStoreEncryptedPrefs(ctx)

    @Provides @Singleton
    fun provideAuthInterceptor(session: SessionStore): AuthInterceptor =
        AuthInterceptor(session)
}