package com.example.driver.features.signin.domain.repository

import com.example.driver.features.signin.domain.model.LoginResult

interface DriverAuthRepository {
    suspend fun login(phone: String, password: String): LoginResult
}
