package com.example.android.features.menu.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.commons.domain.usecase.GetPassengerLocalState
import com.example.android.commons.domain.usecase.GetPassengerLocalUseCase
import com.example.android.core.domain.ErrorMapper
import com.example.android.core.presentation.toReadableMessage
import com.example.android.features.menu.domain.usecase.GetMenuCacheState
import com.example.android.features.menu.domain.usecase.GetMenuCacheUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MenuViewModel @Inject constructor(
    private val getPassengerLocalUseCase: GetPassengerLocalUseCase,
    private val getMenuCacheUseCase: GetMenuCacheUseCase
) : ViewModel() {

    private val _userUi = MutableStateFlow<GetPassengerLocalState>(GetPassengerLocalState.Idle)
    val userUi: StateFlow<GetPassengerLocalState> = _userUi
    fun callGetUser() {
        viewModelScope.launch {
            try {
                getPassengerLocalUseCase().collect {
                    _userUi.value = it
                }
            } catch (t: Throwable) {
                val mapped = ErrorMapper.map(t)
                _userUi.value = GetPassengerLocalState.Error(message = mapped.toReadableMessage())
            }
        }
    }

    private val _menuUi = MutableStateFlow<GetMenuCacheState>(GetMenuCacheState.Idle)
    val menuUi: StateFlow<GetMenuCacheState> = _menuUi
    fun callGetMenu() {
        viewModelScope.launch {
            try {
                getMenuCacheUseCase().collect {
                    _menuUi.value = it
                }
            } catch (t: Throwable) {
                val mapped = ErrorMapper.map(t)
                _menuUi.value = GetMenuCacheState.Error(message = mapped.toReadableMessage())
            }
        }
    }

}