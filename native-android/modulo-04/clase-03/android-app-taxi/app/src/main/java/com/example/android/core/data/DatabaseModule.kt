package com.example.android.core.data

import android.content.Context
import androidx.room3.Room
import com.example.android.core.domain.LocalCache
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides @Singleton
    fun provideAppDatabase(@ApplicationContext ctx: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(ctx, AppDatabase.NAME).build()

    @Provides @Singleton
    fun provideLocalCache(db: AppDatabase): LocalCache =
        LocalCache { db.clearAllTables() }
}
