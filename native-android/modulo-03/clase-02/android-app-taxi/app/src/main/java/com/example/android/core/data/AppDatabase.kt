package com.example.android.core.data

import androidx.room3.Database
import androidx.room3.RoomDatabase
import com.example.android.features.menu.data.local.MenuDao
import com.example.android.features.menu.data.local.MenuEntity

@Database(
    entities = [MenuEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao

    companion object {
        const val NAME = "app_taxi.db"
    }
}
