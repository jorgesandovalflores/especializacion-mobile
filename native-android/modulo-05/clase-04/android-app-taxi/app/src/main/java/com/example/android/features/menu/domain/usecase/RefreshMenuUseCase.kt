package com.example.android.features.menu.domain.usecase

import com.example.android.core.domain.toDomainException
import com.example.android.features.menu.domain.repository.MenuRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed interface RefreshMenuState {
    data object Idle : RefreshMenuState
    data object Loading : RefreshMenuState
    data object Success : RefreshMenuState
    data class Error(val message: String) : RefreshMenuState
}

class RefreshMenuUseCase(
    private val repo: MenuRepository
) {
    operator fun invoke(): Flow<RefreshMenuState> = flow {
        emit(RefreshMenuState.Loading)
        repo.refreshMenu()
        emit(RefreshMenuState.Success)
    }.catch { emit(RefreshMenuState.Error(it.toDomainException().message)) }
}
