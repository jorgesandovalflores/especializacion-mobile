package com.example.android.features.menu.data.remote

import com.example.android.features.menu.data.remote.dto.MenuDto
import retrofit2.http.GET

interface MenuApi {
    @GET("menu/active/PASSENGER")
    suspend fun getMenu(): List<MenuDto>
}