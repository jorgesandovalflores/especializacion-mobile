package com.example.driver.features.signin

import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.data.PublicClient
import com.example.driver.core.domain.SessionStore
import com.example.driver.features.signin.data.remote.DriverAuthApi
import com.example.driver.features.signin.data.repository.DriverAuthRepositoryImpl
import com.example.driver.features.signin.domain.repository.DriverAuthRepository
import com.example.driver.features.signin.domain.usecase.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SignInModule {

    @Provides
    @Singleton
    fun provideDriverAuthApi(@PublicClient retrofit: Retrofit): DriverAuthApi =
        retrofit.create(DriverAuthApi::class.java)

    @Provides
    @Singleton
    fun provideDriverAuthRepository(api: DriverAuthApi): DriverAuthRepository =
        DriverAuthRepositoryImpl(api)

    @Provides
    fun provideLoginUseCase(
        repo: DriverAuthRepository,
        session: SessionStore,
        driverStore: DriverStore
    ): LoginUseCase = LoginUseCase(repo, session, driverStore)
}
