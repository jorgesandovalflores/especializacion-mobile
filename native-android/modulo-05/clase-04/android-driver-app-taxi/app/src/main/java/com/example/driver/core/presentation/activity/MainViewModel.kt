package com.example.driver.core.presentation.activity

import androidx.lifecycle.ViewModel
import com.example.driver.core.domain.SessionExpiration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionExpiration: SessionExpiration
) : ViewModel() {
    val sessionExpired: Flow<Unit> = sessionExpiration.events
}
