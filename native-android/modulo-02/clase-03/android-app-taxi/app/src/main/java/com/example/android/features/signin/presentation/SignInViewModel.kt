package com.example.android.features.signin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.signin.domain.usecase.SignInState
import com.example.android.features.signin.domain.usecase.SignInWithPhoneUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val signInWithPhone: SignInWithPhoneUseCase
) : ViewModel() {

    private val _ui = MutableStateFlow<SignInState>(SignInState.Idle)
    val ui: StateFlow<SignInState> = _ui

    fun submit(phone: String) {
        viewModelScope.launch {
            signInWithPhone(phone).collect { _ui.value = it }
        }
    }

    fun reset() { _ui.value = SignInState.Idle }
}