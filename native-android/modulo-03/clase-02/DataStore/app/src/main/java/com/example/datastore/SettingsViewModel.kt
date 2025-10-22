package com.example.datastore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUiState(
    val darkMode: Boolean = false,
    val username: String = ""
)

class SettingsViewModel(
    private val repo: SettingsRepository
) : ViewModel() {

    private val _usernameState = MutableStateFlow("")
    val usernameState: StateFlow<String> = _usernameState.asStateFlow()

    val uiState: StateFlow<SettingsUiState> =
        combine(repo.darkModeFlow, repo.usernameFlow, _usernameState) { dark, user, localUser ->
            SettingsUiState(darkMode = dark, username = localUser.ifEmpty { user })
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUiState()
        )

    fun onDarkModeChange(value: Boolean) {
        viewModelScope.launch { repo.setDarkMode(value) }
    }

    fun onUsernameChange(value: String) {
        _usernameState.value = value
        viewModelScope.launch { repo.setUsername(value) }
    }
}