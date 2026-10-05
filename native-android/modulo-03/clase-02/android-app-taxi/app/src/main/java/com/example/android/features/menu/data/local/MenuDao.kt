package com.example.android.features.menu.data.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuDao {

    @Query("SELECT * FROM menu ORDER BY position ASC")
    fun observeAll(): Flow<List<MenuEntity>>

    @Upsert
    suspend fun upsertAll(items: List<MenuEntity>)

    @Query("DELETE FROM menu")
    suspend fun clear()

    @Transaction
    suspend fun replaceAll(items: List<MenuEntity>) {
        clear()
        upsertAll(items)
    }
}
