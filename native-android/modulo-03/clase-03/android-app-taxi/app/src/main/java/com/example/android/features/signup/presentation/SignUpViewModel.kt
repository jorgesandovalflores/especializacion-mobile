package com.example.android.features.signup.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.core.domain.ErrorMapper
import com.example.android.core.presentation.toReadableMessage
import com.example.android.features.signup.domain.model.SignUpModelStep1
import com.example.android.features.signup.domain.model.SignUpModelStep2
import com.example.android.features.signup.domain.usecase.GetSignUpStep1UseCase
import com.example.android.features.signup.domain.usecase.GetSignUpStep1UseCaseState
import com.example.android.features.signup.domain.usecase.GetSignUpStep2UseCase
import com.example.android.features.signup.domain.usecase.GetSignUpStep2UseCaseState
import com.example.android.features.signup.domain.usecase.SaveSignUpStep1UseCase
import com.example.android.features.signup.domain.usecase.SignUpUseCase
import com.example.android.features.signup.domain.usecase.SignUpUseCaseState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val getSignUpStep1UseCase: GetSignUpStep1UseCase,
    private val saveSignUpStep1UseCase: SaveSignUpStep1UseCase,
    private val getSignupStep2UseCase: GetSignUpStep2UseCase,
    private val signUpUseCase: SignUpUseCase
): ViewModel() {

    private val _getStep1 = MutableStateFlow<GetSignUpStep1UseCaseState>(GetSignUpStep1UseCaseState.Idle)
    val getStep1: StateFlow<GetSignUpStep1UseCaseState> = _getStep1
    fun callGetStep1() {
        viewModelScope.launch {
            _getStep1.value = GetSignUpStep1UseCaseState.Loading
            try {
                getSignUpStep1UseCase().collect {
                    _getStep1.value = it
                }
            } catch (_: Throwable) { }
        }
    }

    fun callSaveStep1(value: SignUpModelStep1) {
        viewModelScope.launch {
            try {
                saveSignUpStep1UseCase(value).collect {}
            } catch (_: Throwable) { }
        }
    }

    private val _getStep2 = MutableStateFlow<GetSignUpStep2UseCaseState>(GetSignUpStep2UseCaseState.Idle)
    val getStep2: StateFlow<GetSignUpStep2UseCaseState> = _getStep2
    fun callGetStep2() {
        viewModelScope.launch {
            _getStep2.value = GetSignUpStep2UseCaseState.Loading
            try {
                getSignupStep2UseCase().collect {
                    _getStep2.value = it
                }
            } catch (_: Throwable) { }
        }
    }

    private val _signUp = MutableStateFlow<SignUpUseCaseState>(SignUpUseCaseState.Idle)
    val signUp: StateFlow<SignUpUseCaseState> = _signUp
    fun callSignUp(step1: SignUpModelStep1, step2: SignUpModelStep2) {
        viewModelScope.launch {
            _signUp.value = SignUpUseCaseState.Loading
            try {
                signUpUseCase(signUpStep1 = step1, signUpStep2 = step2).collect {
                    _signUp.value = it
                }
            } catch (t: Throwable) {
                val mapped = ErrorMapper.map(t)
                _signUp.value = SignUpUseCaseState.Error(message = mapped.toReadableMessage())
            }
        }
    }

}