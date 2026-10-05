package com.example.android.features.menu.data.local

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.example.android.features.menu.domain.model.Menu

@Entity(tableName = "menu")
data class MenuEntity(
    @PrimaryKey val id: String,
    val text: String,
    val icon: String,
    val deeplink: String,
    val position: Int,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)

fun MenuEntity.toDomain(): Menu = Menu(
    key = id,
    text = text,
    icon = icon,
    deeplink = deeplink,
    order = position
)
