package com.example.android.core.data

import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response

internal const val HEADER_AUTHORIZATION = "Authorization"
internal const val BEARER_PREFIX = "Bearer "
private const val PUBLIC_AUTH_PATH = "/auth/"

internal fun Request.isPublicAuthEndpoint(): Boolean = url.encodedPath.startsWith(PUBLIC_AUTH_PATH)

class AuthInterceptor(
    private val session: SessionStore
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        if (original.isPublicAuthEndpoint()) return chain.proceed(original)

        val token = runBlocking { session.accessToken().first() }
        val request = if (!token.isNullOrBlank()) {
            original.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX$token")
                .build()
        } else original
        return chain.proceed(request)
    }
}
