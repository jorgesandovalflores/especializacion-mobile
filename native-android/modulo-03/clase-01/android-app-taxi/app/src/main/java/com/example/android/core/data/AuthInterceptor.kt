package com.example.android.core.data

import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val session: SessionStore
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        // 1) Adjunta Authorization si hay token
        val original = chain.request()
        val token = runBlocking { session.accessToken().first() }
        val requestWithAuth = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            original
        }

        // 2) Ejecuta
        val response = chain.proceed(requestWithAuth)
        if (response.code == 401) {
            runBlocking {
                session.clear()
            }
        }

        return response
    }
}
