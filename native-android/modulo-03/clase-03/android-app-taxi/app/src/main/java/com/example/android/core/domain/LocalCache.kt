package com.example.android.core.domain

fun interface LocalCache {
    suspend fun clear()
}
