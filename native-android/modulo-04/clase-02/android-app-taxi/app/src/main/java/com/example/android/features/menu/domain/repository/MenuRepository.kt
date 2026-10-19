package com.example.android.features.menu.domain.repository

import com.example.android.features.menu.domain.model.Menu
import kotlinx.coroutines.flow.Flow

interface MenuRepository {
    fun observeMenu(): Flow<List<Menu>>
    suspend fun refreshMenu()
}
