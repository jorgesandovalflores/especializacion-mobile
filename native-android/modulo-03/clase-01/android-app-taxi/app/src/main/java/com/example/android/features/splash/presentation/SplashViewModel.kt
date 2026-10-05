package com.example.android.features.splash.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.splash.domain.usecase.HasSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashDestination { SignIn, Home }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val hasSession: HasSessionUseCase
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination?>(null)
    val destination: StateFlow<SplashDestination?> = _destination.asStateFlow()

    init {
        viewModelScope.launch {
            delay(SPLASH_DURATION_MS)
            _destination.value = if (hasSession()) SplashDestination.Home else SplashDestination.SignIn
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1_500L
    }
}
