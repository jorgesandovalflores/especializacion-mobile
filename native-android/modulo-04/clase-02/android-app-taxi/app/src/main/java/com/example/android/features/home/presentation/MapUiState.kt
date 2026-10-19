package com.example.android.features.home.presentation

import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.UserLocation
import com.example.android.features.home.domain.usecase.LocationState

sealed interface MapUiState {
    data object Idle : MapUiState
    data class Permission(val blocked: Boolean) : MapUiState
    data class Success(val location: UserLocation, val precise: Boolean) : MapUiState
    data class Error(val error: LocationError) : MapUiState
}

fun LocationState.toMapUi(precise: Boolean): MapUiState = when (this) {
    LocationState.Loading -> MapUiState.Idle
    is LocationState.Success -> MapUiState.Success(location, precise)
    is LocationState.Error -> MapUiState.Error(error)
}
