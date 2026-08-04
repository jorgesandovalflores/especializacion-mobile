package com.example.demo_mvi.home

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Transaction(
    val id: String,
    val title: String,
    val category: String,
    val amount: String,
    val time: String,
    val icon: ImageVector,
    val badgeColor: Color,
    val rowColor: Color,
)
