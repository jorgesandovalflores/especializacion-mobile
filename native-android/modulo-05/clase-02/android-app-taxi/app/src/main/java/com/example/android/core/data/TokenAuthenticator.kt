package com.example.android.core.data

import com.example.android.core.domain.SessionExpiration
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val refresher: TokenRefresher,
    private val expiration: SessionExpiration
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        val request = response.request
        if (request.isPublicAuthEndpoint()) return null
        if (response.priorResponse != null) return null

        val staleAccessToken = request.header(HEADER_AUTHORIZATION)?.removePrefix(BEARER_PREFIX)

        return when (val result = runBlocking { refresher.refresh(staleAccessToken) }) {
            is RefreshResult.Refreshed -> request.newBuilder()
                .header(HEADER_AUTHORIZATION, "$BEARER_PREFIX${result.accessToken}")
                .build()

            RefreshResult.Rejected -> {
                runBlocking { expiration.expire() }
                null
            }
        }
    }
}
