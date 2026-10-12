package com.example.android.core.data

import android.content.Context
import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionExpiration
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

    @Provides @Singleton
    fun provideSessionExpiration(session: SessionStore, localCache: LocalCache): SessionExpiration =
        SessionExpirationImpl(session, localCache)

    @Provides @Singleton
    fun provideTokenRefresher(session: SessionStore, api: RefreshApi): TokenRefresher =
        TokenRefresher(session, api)

    @Provides @Singleton
    fun provideTokenAuthenticator(
        refresher: TokenRefresher,
        expiration: SessionExpiration
    ): TokenAuthenticator = TokenAuthenticator(refresher, expiration)
}
