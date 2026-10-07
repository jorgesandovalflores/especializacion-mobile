package com.example.android.core.data

import com.example.android.core.data.dto.RefreshRequest
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

sealed interface RefreshResult {
    data class Refreshed(val accessToken: String) : RefreshResult
    data object Rejected : RefreshResult
}

class TokenRefresher(
    private val session: SessionStore,
    private val api: RefreshApi
) {
    private val mutex = Mutex()

    suspend fun refresh(staleAccessToken: String?): RefreshResult = mutex.withLock {
        val current = session.accessToken().first()
        if (!current.isNullOrBlank() && current != staleAccessToken) {
            return@withLock RefreshResult.Refreshed(current)
        }

        val refreshToken = session.refreshToken().first()
        if (refreshToken.isNullOrBlank()) return@withLock RefreshResult.Rejected

        val tokens = try {
            api.refresh(RefreshRequest(refreshToken))
        } catch (e: HttpException) {
            if (e.code() in CLIENT_ERRORS) return@withLock RefreshResult.Rejected
            throw IOException("Refresh failed with HTTP ${e.code()}", e)
        }

        session.saveTokens(access = tokens.accessToken, refresh = tokens.refreshToken)
        RefreshResult.Refreshed(tokens.accessToken)
    }

    private companion object {
        val CLIENT_ERRORS = 400..499
    }
}
