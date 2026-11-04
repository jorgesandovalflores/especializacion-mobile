package com.example.driver.features.signin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.driver.features.signin.domain.usecase.LoginState
import com.example.driver.features.signin.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val login: LoginUseCase
) : ViewModel() {

    private val _state = MutableStateFlow<LoginState>(LoginState.Idle)
    val state: StateFlow<LoginState> = _state.asStateFlow()

    fun submit(phone: String, password: String) {
        if (_state.value is LoginState.Loading || !LoginUseCase.canSubmit(phone, password)) return
        login(phone, password)
            .onEach { _state.value = it }
            .launchIn(viewModelScope)
    }

    fun clearState() {
        _state.value = LoginState.Idle
    }
}
