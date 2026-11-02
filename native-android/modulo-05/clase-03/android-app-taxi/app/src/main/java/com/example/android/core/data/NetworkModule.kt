package com.example.android.core.data

import com.example.android.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class PublicClient

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val TIMEOUT_SECONDS = 10L

    @Provides @Singleton
    fun provideLogging(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
            redactHeader(HEADER_AUTHORIZATION)
        }

    @Provides @Singleton
    fun provideHeadersInterceptor(): HeadersInterceptor =
        HeadersInterceptor(appVersion = BuildConfig.VERSION_NAME)

    @Provides @Singleton @PublicClient
    fun providePublicOkHttp(
        headers: HeadersInterceptor,
        logging: HttpLoggingInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(headers)
        .addInterceptor(logging)
        .build()

    @Provides @Singleton
    fun provideOkHttp(
        @PublicClient publicClient: OkHttpClient,
        auth: AuthInterceptor,
        authenticator: TokenAuthenticator
    ): OkHttpClient = publicClient.newBuilder()
        .addInterceptor(auth)
        .authenticator(authenticator)
        .build()

    @Provides @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit = retrofit(client)

    @Provides @Singleton @PublicClient
    fun providePublicRetrofit(@PublicClient client: OkHttpClient): Retrofit = retrofit(client)

    @Provides @Singleton
    fun provideRefreshApi(@PublicClient retrofit: Retrofit): RefreshApi =
        retrofit.create(RefreshApi::class.java)

    private fun retrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}
