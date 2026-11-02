package com.example.driver.core.domain

fun interface LocalCache {
    suspend fun clear()
}
