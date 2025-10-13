package com.example.android.core.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.android.features.menu.data.local.dao.MenuDao
import com.example.android.features.menu.data.local.table.MenuEntity

@Database(
    entities = [MenuEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun menuDao(): MenuDao
}
