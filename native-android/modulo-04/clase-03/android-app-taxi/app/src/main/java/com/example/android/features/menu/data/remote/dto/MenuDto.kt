package com.example.android.features.menu.data.remote.dto

import com.example.android.features.menu.data.local.MenuEntity

data class MenuDto(
    val key: String,
    val text: String,
    val icon: String,
    val deeplink: String,
    val order: Int
)

fun MenuDto.toEntity(updatedAt: Long): MenuEntity = MenuEntity(
    id = key,
    text = text,
    icon = icon,
    deeplink = deeplink,
    position = order,
    updatedAt = updatedAt
)
