package com.example.driver.features.signin.domain.model

import com.example.driver.commons.domain.model.Driver

data class LoginResult(
    val accessToken: String,
    val refreshToken: String,
    val driver: Driver
)
