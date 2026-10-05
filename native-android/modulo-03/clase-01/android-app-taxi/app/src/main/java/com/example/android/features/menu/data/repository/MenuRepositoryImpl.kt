package com.example.android.features.menu.data.repository

import com.example.android.core.data.safeCall
import com.example.android.features.menu.data.local.MenuDao
import com.example.android.features.menu.data.local.toDomain
import com.example.android.features.menu.data.remote.MenuApi
import com.example.android.features.menu.data.remote.dto.toEntity
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MenuRepositoryImpl(
    private val api: MenuApi,
    private val dao: MenuDao,
    private val now: () -> Long = System::currentTimeMillis
) : MenuRepository {

    override fun observeMenu(): Flow<List<Menu>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun refreshMenu() = safeCall {
        val updatedAt = now()
        val remote = api.getMenu()
        dao.replaceAll(remote.map { it.toEntity(updatedAt) })
    }
}
