package com.example.android.features.menu.domain.model

data class Menu(
    val key: String,
    val text: String,
    val iconUrl: String,
    val deeplink: String,
    val order: Int
)