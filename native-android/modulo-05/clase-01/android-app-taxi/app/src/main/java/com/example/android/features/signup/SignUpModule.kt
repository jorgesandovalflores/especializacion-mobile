package com.example.android.features.signup

import android.content.Context
import android.content.SharedPreferences
import com.example.android.core.domain.SessionStore
import com.example.android.features.signup.data.local.SignUpDraftStorePrefs
import com.example.android.features.signup.data.remote.SignUpApi
import com.example.android.features.signup.data.repository.SignUpRepositoryImpl
import com.example.android.features.signup.domain.repository.SignUpRepository
import com.example.android.features.signup.domain.store.SignUpDraftStore
import com.example.android.features.signup.domain.usecase.GetSignUpDraftUseCase
import com.example.android.features.signup.domain.usecase.SaveSignUpDraftUseCase
import com.example.android.features.signup.domain.usecase.SignUpUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SignUpDraftPrefs

@Module
@InstallIn(SingletonComponent::class)
object SignUpModule {

    @Provides
    @Singleton
    @SignUpDraftPrefs
    fun provideSignUpDraftPrefs(@ApplicationContext ctx: Context): SharedPreferences =
        ctx.getSharedPreferences(SignUpDraftStorePrefs.PREFS_NAME, Context.MODE_PRIVATE)

    @Provides
    @Singleton
    fun provideSignUpDraftStore(@SignUpDraftPrefs prefs: SharedPreferences): SignUpDraftStore =
        SignUpDraftStorePrefs(prefs)

    @Provides
    @Singleton
    fun provideSignUpApi(retrofit: Retrofit): SignUpApi =
        retrofit.create(SignUpApi::class.java)

    @Provides
    @Singleton
    fun provideSignUpRepository(api: SignUpApi): SignUpRepository =
        SignUpRepositoryImpl(api)

    @Provides
    @Singleton
    fun provideGetSignUpDraftUseCase(draftStore: SignUpDraftStore): GetSignUpDraftUseCase =
        GetSignUpDraftUseCase(draftStore)

    @Provides
    @Singleton
    fun provideSaveSignUpDraftUseCase(draftStore: SignUpDraftStore): SaveSignUpDraftUseCase =
        SaveSignUpDraftUseCase(draftStore)

    @Provides
    @Singleton
    fun provideSignUpUseCase(
        repo: SignUpRepository,
        draftStore: SignUpDraftStore,
        sessionStore: SessionStore
    ): SignUpUseCase = SignUpUseCase(repo, draftStore, sessionStore)
}
