package com.example.demo_mvi.home

/** Every possible user action on the Home screen — replaces the classic "Controller". */
sealed interface HomeIntent {
    data object LoadData : HomeIntent
    data class TransactionClicked(val id: String) : HomeIntent
    data object DismissError : HomeIntent
}
