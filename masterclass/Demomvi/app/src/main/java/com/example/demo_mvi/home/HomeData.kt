package com.example.demo_mvi.home

/** Payload returned by [HomeModel.fetchHomeData] — the ViewModel folds this into [HomeState]. */
data class HomeData(
    val userName: String,
    val activityValues: List<Int>,
    val activityLabels: List<String>,
    val salesLastWeek: String,
    val revenueLastWeek: String,
    val transactions: List<Transaction>,
)
