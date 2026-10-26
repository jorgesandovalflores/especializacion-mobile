package com.example.android.features.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.menu.domain.model.Menu
import com.example.android.features.menu.domain.usecase.ObserveMenuUseCase
import com.example.android.features.menu.domain.usecase.RefreshMenuState
import com.example.android.features.menu.domain.usecase.RefreshMenuUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    observeMenuUseCase: ObserveMenuUseCase,
    private val refreshMenuUseCase: RefreshMenuUseCase
) : ViewModel() {

    val menu: StateFlow<List<Menu>> = observeMenuUseCase()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), emptyList())

    private val _refreshUi = MutableStateFlow<RefreshMenuState>(RefreshMenuState.Idle)
    val refreshUi: StateFlow<RefreshMenuState> = _refreshUi.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        if (_refreshUi.value is RefreshMenuState.Loading) return
        refreshMenuUseCase()
            .onEach { _refreshUi.value = it }
            .launchIn(viewModelScope)
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
