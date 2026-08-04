package com.example.demo_mvp.home

import androidx.annotation.ColorInt
import androidx.annotation.DrawableRes

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val time: String,
    @param:DrawableRes val iconRes: Int,
    @param:ColorInt val badgeColor: Int,
    @param:DrawableRes val rowBackgroundRes: Int,
)

data class HomeUiData(
    val userName: String,
    val activityValues: List<Int>,
    val activityLabels: List<String>,
    val salesLastWeek: String,
    val revenueLastWeek: String,
    val transactions: List<Transaction>,
)
