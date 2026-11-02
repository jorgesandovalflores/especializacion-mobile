package com.example.android.features.menu

import com.example.android.core.data.AppDatabase
import com.example.android.features.menu.data.local.MenuDao
import com.example.android.features.menu.data.remote.MenuApi
import com.example.android.features.menu.data.repository.MenuRepositoryImpl
import com.example.android.features.menu.domain.repository.MenuRepository
import com.example.android.features.menu.domain.usecase.ObserveMenuUseCase
import com.example.android.features.menu.domain.usecase.RefreshMenuUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MenuModule {

    @Provides
    @Singleton
    fun provideMenuApi(retrofit: Retrofit): MenuApi =
        retrofit.create(MenuApi::class.java)

    @Provides
    fun provideMenuDao(db: AppDatabase): MenuDao = db.menuDao()

    @Provides
    @Singleton
    fun provideMenuRepository(api: MenuApi, dao: MenuDao): MenuRepository =
        MenuRepositoryImpl(api, dao)

    @Provides
    @Singleton
    fun provideObserveMenuUseCase(repo: MenuRepository): ObserveMenuUseCase =
        ObserveMenuUseCase(repo)

    @Provides
    @Singleton
    fun provideRefreshMenuUseCase(repo: MenuRepository): RefreshMenuUseCase =
        RefreshMenuUseCase(repo)
}
