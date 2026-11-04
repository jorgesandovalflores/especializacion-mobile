package com.example.driver.commons.data

import android.content.Context
import com.example.driver.commons.data.local.DriverStorePrefs
import com.example.driver.commons.domain.store.DriverStore
import com.example.driver.core.domain.LocalCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CommonsModule {

    @Provides @Singleton
    fun provideDriverStore(@ApplicationContext context: Context): DriverStore =
        DriverStorePrefs(context)

    @Provides @Singleton
    fun provideLocalCache(store: DriverStore): LocalCache =
        LocalCache { store.clear() }
}
