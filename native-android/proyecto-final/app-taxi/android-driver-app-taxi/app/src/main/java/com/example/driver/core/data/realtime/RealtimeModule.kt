package com.example.driver.core.data.realtime

import com.example.driver.BuildConfig
import com.example.driver.core.data.ApplicationScope
import com.example.driver.core.data.TokenRefresher
import com.example.driver.core.domain.SessionExpiration
import com.example.driver.core.domain.SessionStore
import com.example.driver.core.domain.realtime.RealtimeGateway
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RealtimeModule {

    private const val NAMESPACE = "realtime"
    private const val CONNECT_TIMEOUT_SECONDS = 10L

    @Provides @Singleton
    fun provideRealtimeGateway(
        session: SessionStore,
        refresher: TokenRefresher,
        expiration: SessionExpiration,
        @ApplicationScope scope: CoroutineScope
    ): RealtimeGateway = SocketIoRealtimeClient(
        url = "${BuildConfig.API_BASE_URL}$NAMESPACE",
        session = session,
        refresher = refresher,
        expiration = expiration,
        httpClient = OkHttpClient.Builder()
            .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.MILLISECONDS)
            .build(),
        scope = scope
    )
}
