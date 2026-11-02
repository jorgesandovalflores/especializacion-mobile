package com.example.driver.features.signin.data.remote.dto

import com.example.driver.commons.data.remote.dto.DriverDto
import com.example.driver.commons.data.remote.dto.toDomain
import com.example.driver.features.signin.domain.model.LoginResult

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val driver: DriverDto
)

fun LoginResponse.toDomain(): LoginResult = LoginResult(
    accessToken = accessToken,
    refreshToken = refreshToken,
    driver = driver.toDomain()
)
