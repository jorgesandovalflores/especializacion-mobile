package com.example.android.features.signin

import com.example.android.core.domain.SessionStore
import com.example.android.features.signin.data.remote.AuthApi
import com.example.android.features.signin.data.repository.AuthRepositoryImpl
import com.example.android.features.signin.domain.repository.AuthRepository
import com.example.android.features.signin.domain.usecase.SignInWithPhoneUseCase
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
    fun provideAuthApi(retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    @Provides
    @Singleton
    fun provideAuthRepository(api: AuthApi): AuthRepository =
        AuthRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideSignInUseCase(
        repo: AuthRepository,
        session: SessionStore
    ): SignInWithPhoneUseCase = SignInWithPhoneUseCase(repo, session)
}