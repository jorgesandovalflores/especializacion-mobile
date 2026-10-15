package com.example.android.features.home.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.android.features.home.domain.usecase.ObserveLocationUseCase
import com.example.android.features.home.domain.usecase.SignOutUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val signOut: SignOutUseCase,
    private val observeLocation: ObserveLocationUseCase
) : ViewModel() {

    private val permission = MutableStateFlow<LocationPermission>(LocationPermission.Unknown)
    private val attempt = MutableStateFlow(0)

    val mapUi: StateFlow<MapUiState> = combine(permission, attempt) { current, _ -> current }
        .flatMapLatest { it.toMapUi() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), MapUiState.Idle)

    fun onPermissionChecked(system: LocationPermission) {
        permission.update { current ->
            when {
                system is LocationPermission.Granted -> system
                current is LocationPermission.Denied -> current
                else -> system
            }
        }
        if (mapUi.value is MapUiState.Error) retry()
    }

    fun onPermissionResult(result: LocationPermission) {
        permission.value = result
    }

    fun retry() {
        attempt.update { it + 1 }
    }

    fun logout(onDone: () -> Unit) {
        viewModelScope.launch {
            signOut()
            onDone()
        }
    }

    private fun LocationPermission.toMapUi(): Flow<MapUiState> = when (this) {
        LocationPermission.Unknown -> flowOf(MapUiState.Idle)
        is LocationPermission.Denied -> flowOf(MapUiState.Permission(blocked))
        is LocationPermission.Granted -> observeLocation(precise).map { it.toMapUi(precise) }
    }

    private companion object {
        const val STOP_TIMEOUT_MS = 5_000L
    }
}
