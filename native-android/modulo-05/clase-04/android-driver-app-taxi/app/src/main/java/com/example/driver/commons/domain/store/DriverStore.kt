package com.example.driver.commons.domain.store

import com.example.driver.commons.domain.model.Driver
import kotlinx.coroutines.flow.Flow

interface DriverStore {
    fun driver(): Flow<Driver?>
    suspend fun saveDriver(driver: Driver)
    fun isOnline(): Boolean
    suspend fun setOnline(online: Boolean)
    suspend fun clear()
}
