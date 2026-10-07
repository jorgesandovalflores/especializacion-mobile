package com.example.android.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.home.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val signOut: SignOutUseCase
) : ViewModel() {

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            signOut()
            onDone()
        }
    }
}
