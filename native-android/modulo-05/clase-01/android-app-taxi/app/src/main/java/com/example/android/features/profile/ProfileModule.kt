package com.example.android.features.profile

import android.content.Context
import com.example.android.features.profile.data.remote.ProfileApi
import com.example.android.features.profile.data.repository.ProfileRepositoryImpl
import com.example.android.features.profile.data.storage.BitmapImageCompressor
import com.example.android.features.profile.data.storage.FirebasePhotoStorage
import com.example.android.features.profile.domain.repository.ImageCompressor
import com.example.android.features.profile.domain.repository.PhotoStorage
import com.example.android.features.profile.domain.repository.ProfileRepository
import com.example.android.features.profile.domain.usecase.ChangePhotoUseCase
import com.example.android.features.profile.domain.usecase.GetProfileUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProfileModule {

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi =
        retrofit.create(ProfileApi::class.java)

    @Provides
    @Singleton
    fun provideProfileRepository(api: ProfileApi): ProfileRepository =
        ProfileRepositoryImpl(api)

    @Provides
    @Singleton
    fun providePhotoStorage(@ApplicationContext context: Context, api: ProfileApi): PhotoStorage =
        FirebasePhotoStorage(context, api)

    @Provides
    @Singleton
    fun provideImageCompressor(@ApplicationContext context: Context): ImageCompressor =
        BitmapImageCompressor(context.contentResolver)

    @Provides
    fun provideGetProfileUseCase(repo: ProfileRepository): GetProfileUseCase =
        GetProfileUseCase(repo)

    @Provides
    fun provideChangePhotoUseCase(
        compressor: ImageCompressor,
        storage: PhotoStorage,
        repo: ProfileRepository
    ): ChangePhotoUseCase = ChangePhotoUseCase(compressor, storage, repo)
}
