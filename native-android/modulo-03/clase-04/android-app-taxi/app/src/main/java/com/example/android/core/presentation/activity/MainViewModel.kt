package com.example.android.core.presentation.activity

import androidx.lifecycle.ViewModel
import com.example.android.core.domain.SessionExpiration
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    sessionExpiration: SessionExpiration
) : ViewModel() {
    val sessionExpired: Flow<Unit> = sessionExpiration.events
}
