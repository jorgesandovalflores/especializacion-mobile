package com.example.android.core.data

import okhttp3.Interceptor
import okhttp3.Response
import java.util.UUID

class HeadersInterceptor(
    private val appVersion: String,
    private val newRequestId: () -> String = { UUID.randomUUID().toString() }
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .header(HEADER_ACCEPT, "application/json")
            .header(HEADER_ACCEPT_LANGUAGE, LANGUAGE)
            .header(HEADER_USER_AGENT, "AppTaxi-Android/$appVersion")
            .header(HEADER_REQUEST_ID, newRequestId())
            .build()
        return chain.proceed(request)
    }

    companion object {
        const val HEADER_ACCEPT = "Accept"
        const val HEADER_ACCEPT_LANGUAGE = "Accept-Language"
        const val HEADER_USER_AGENT = "User-Agent"
        const val HEADER_REQUEST_ID = "X-Request-Id"
        private const val LANGUAGE = "es"
    }
}
