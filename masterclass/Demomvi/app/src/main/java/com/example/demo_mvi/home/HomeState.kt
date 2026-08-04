package com.example.demo_mvi.home

/** Single immutable state for the whole Home screen — the "Model" in MVI. */
data class HomeState(
    val userName: String = "",
    val activityValues: List<Int> = emptyList(),
    val activityLabels: List<String> = emptyList(),
    val salesLastWeek: String = "",
    val revenueLastWeek: String = "",
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
