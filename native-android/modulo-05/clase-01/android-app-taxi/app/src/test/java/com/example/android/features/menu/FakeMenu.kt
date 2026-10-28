package com.example.android.features.menu

import com.example.android.features.menu.data.local.MenuDao
import com.example.android.features.menu.data.local.MenuEntity
import com.example.android.features.menu.data.remote.MenuApi
import com.example.android.features.menu.data.remote.dto.MenuDto
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeMenuApi(
    private val onGetMenu: suspend () -> List<MenuDto> = { emptyList() }
) : MenuApi {
    var calls = 0

    override suspend fun getMenu(): List<MenuDto> {
        calls++
        return onGetMenu()
    }
}

class FakeMenuDao(initial: List<MenuEntity> = emptyList()) : MenuDao {
    private val table = MutableStateFlow(initial.associateBy { it.id })

    override fun observeAll(): Flow<List<MenuEntity>> =
        table.map { rows -> rows.values.sortedBy { it.position } }

    override suspend fun upsertAll(items: List<MenuEntity>) {
        table.value = table.value + items.associateBy { it.id }
    }

    override suspend fun clear() {
        table.value = emptyMap()
    }

    fun rows(): List<MenuEntity> = table.value.values.sortedBy { it.position }
}

class FakeMenuRepository(
    private val onRefresh: suspend () -> Unit = {}
) : MenuRepository {
    val menu = MutableStateFlow<List<Menu>>(emptyList())
    var refreshCalls = 0

    override fun observeMenu(): Flow<List<Menu>> = menu

    override suspend fun refreshMenu() {
        refreshCalls++
        onRefresh()
    }
}

fun menuDto(key: String, order: Int) = MenuDto(
    key = key,
    text = "Texto $key",
    icon = "home",
    deeplink = "app-taxi://passenger/$key",
    order = order
)

fun menuEntity(key: String, position: Int, updatedAt: Long = 0L) = MenuEntity(
    id = key,
    text = "Texto $key",
    icon = "home",
    deeplink = "app-taxi://passenger/$key",
    position = position,
    updatedAt = updatedAt
)
