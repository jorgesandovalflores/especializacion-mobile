package com.example.android.features.signin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import com.example.android.features.signin.domain.usecase.OtpGenerateUseCase
import com.example.android.features.signin.domain.usecase.OtpValidateState
import com.example.android.features.signin.domain.usecase.OtpValidateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val otpGenerateUseCase: OtpGenerateUseCase,
    private val otpValidateUseCase: OtpValidateUseCase
) : ViewModel() {

    private val _generateOtpUi = MutableStateFlow<OtpGenerateState>(OtpGenerateState.Idle)
    val generateOtpUi: StateFlow<OtpGenerateState> = _generateOtpUi.asStateFlow()

    private val _validateOtpUi = MutableStateFlow<OtpValidateState>(OtpValidateState.Idle)
    val validateOtpUi: StateFlow<OtpValidateState> = _validateOtpUi.asStateFlow()

    fun callGenerateOtp(phone: String) {
        if (_generateOtpUi.value is OtpGenerateState.Loading) return
        otpGenerateUseCase(phone)
            .onEach { _generateOtpUi.value = it }
            .launchIn(viewModelScope)
    }

    fun callValidateOtp(phone: String, code: String) {
        if (_validateOtpUi.value is OtpValidateState.Loading) return
        otpValidateUseCase(phone, code)
            .onEach { _validateOtpUi.value = it }
            .launchIn(viewModelScope)
    }

    fun clearGenerateState() { _generateOtpUi.value = OtpGenerateState.Idle }
    fun clearValidateState() { _validateOtpUi.value = OtpValidateState.Idle }
}
