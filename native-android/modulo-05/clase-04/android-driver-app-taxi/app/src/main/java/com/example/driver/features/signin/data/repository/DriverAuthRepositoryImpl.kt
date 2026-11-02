package com.example.driver.features.signin.data.repository

import com.example.driver.core.data.safeCall
import com.example.driver.features.signin.data.remote.DriverAuthApi
import com.example.driver.features.signin.data.remote.dto.LoginRequest
import com.example.driver.features.signin.data.remote.dto.toDomain
import com.example.driver.features.signin.domain.model.LoginResult
import com.example.driver.features.signin.domain.repository.DriverAuthRepository

class DriverAuthRepositoryImpl(
    private val api: DriverAuthApi
) : DriverAuthRepository {

    override suspend fun login(phone: String, password: String): LoginResult = safeCall {
        api.login(LoginRequest(phone = phone, password = password)).toDomain()
    }
}
