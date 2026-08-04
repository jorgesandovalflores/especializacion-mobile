package com.example.demo_mvvm.home

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val data: HomeUiData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}
