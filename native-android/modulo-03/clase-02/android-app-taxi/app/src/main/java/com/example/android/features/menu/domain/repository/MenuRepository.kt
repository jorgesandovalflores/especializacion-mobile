package com.example.android.features.menu.domain.repository

import com.example.android.features.menu.domain.model.Menu

interface MenuRepository {
    suspend fun getMenuRemote(): List<Menu>
    suspend fun getMenuLocal(): List<Menu>
    suspend fun saveMenuLocal(values: List<Menu>)
}