package com.example.android.features.menu.domain.usecase

import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow

class ObserveMenuUseCase(
    private val repo: MenuRepository
) {
    operator fun invoke(): Flow<List<Menu>> = repo.observeMenu()
}
