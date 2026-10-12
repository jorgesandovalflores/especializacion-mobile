package com.example.android.features.signup.presentation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.usecase.GetSignUpDraftUseCase
import com.example.android.features.signup.domain.usecase.SaveSignUpDraftUseCase
import com.example.android.features.signup.domain.usecase.SignUpState
import com.example.android.features.signup.domain.usecase.SignUpUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val getSignUpDraftUseCase: GetSignUpDraftUseCase,
    private val saveSignUpDraftUseCase: SaveSignUpDraftUseCase,
    private val signUpUseCase: SignUpUseCase
) : ViewModel() {

    var draft by mutableStateOf<SignUpDraft?>(null)
        private set

    private val _signUpUi = MutableStateFlow<SignUpState>(SignUpState.Idle)
    val signUpUi: StateFlow<SignUpState> = _signUpUi.asStateFlow()

    init {
        viewModelScope.launch { draft = getSignUpDraftUseCase() }
    }

    fun onNamesChange(givenName: String, familyName: String) {
        draft = draft?.copy(givenName = givenName, familyName = familyName)
        viewModelScope.launch { saveSignUpDraftUseCase.personal(givenName, familyName) }
    }

    fun onEmailChange(email: String) {
        draft = draft?.copy(email = email)
        viewModelScope.launch { saveSignUpDraftUseCase.email(email) }
    }

    fun callSignUp() {
        if (_signUpUi.value is SignUpState.Loading) return
        signUpUseCase()
            .onEach { _signUpUi.value = it }
            .launchIn(viewModelScope)
    }

    fun clearSignUpState() { _signUpUi.value = SignUpState.Idle }
}
