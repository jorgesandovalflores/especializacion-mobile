package com.example.android.core.data

import com.example.android.BuildConfig
import com.example.android.core.data.realtime.SocketIoRealtimeClient
import com.example.android.core.domain.SessionStore
import com.example.android.core.domain.realtime.RealtimeClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    @Provides @Singleton
    fun provideRealtimeClient(session: SessionStore, refresher: TokenRefresher): RealtimeClient =
        SocketIoRealtimeClient(
            baseUrl = BuildConfig.API_BASE_URL,
            session = session,
            refresher = refresher,
            scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        )
}
