package com.example.android.core.data

import android.app.Activity
import android.content.Context
import android.content.Intent
import com.example.android.core.data.dto.RefreshDto
import com.example.android.core.domain.SessionStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthInterceptor @Inject constructor(
    private val session: SessionStore,
    private val refreshApi: RefreshApi,
    private val context: Context
) : Interceptor {

    // Mecanismo de de-duplicación para evitar múltiples refresh simultáneos
    private val refreshLocks = ConcurrentHashMap<String, Any>()

    // Flag global para controlar intentos de refresh
    private val refreshAttempted = AtomicBoolean(false)

    // Flag para evitar múltiples cierres de app
    private val appClosing = AtomicBoolean(false)

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val accessToken = runBlocking { session.accessToken().first() }

        val authenticatedRequest = if (!accessToken.isNullOrBlank()) {
            originalRequest.withAuthHeader(accessToken)
        } else {
            originalRequest
        }

        // Ejecutar request original
        val response = chain.proceed(authenticatedRequest)
        if (response.code == 401 && shouldRefreshToken(originalRequest)) {
            response.close()
            if (refreshAttempted.get()) {
                handleRefreshFailure()
                return response
            }
            return handleTokenRefresh(chain, originalRequest)
        }

        return response
    }

    private fun Request.withAuthHeader(token: String): Request {
        return newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun shouldRefreshToken(request: Request): Boolean {
        val path = request.url.encodedPath
        return !path.contains("auth/refresh") &&
                !path.contains("auth/otp-generate") &&
                !path.contains("auth/otp-validate")
    }

    private fun handleTokenRefresh(chain: Interceptor.Chain, originalRequest: Request): Response {
        val refreshToken = runBlocking { session.refreshToken().first() }

        if (refreshToken.isNullOrBlank()) {
            runBlocking { session.clear() }
            handleRefreshFailure()
            return chain.proceed(originalRequest) // Fallará con 401
        }

        val tokenLock = refreshLocks.computeIfAbsent(refreshToken) { Any() }

        return synchronized(tokenLock) {
            try {
                refreshAttempted.set(true)
                val currentAccessToken = runBlocking { session.accessToken().first() }
                val currentRefreshToken = runBlocking { session.refreshToken().first() }
                if (currentAccessToken != null && currentRefreshToken != refreshToken) {
                    val newRequest = originalRequest.withAuthHeader(currentAccessToken)
                    return@synchronized chain.proceed(newRequest)
                }

                val refreshSuccess = performTokenRefresh(refreshToken)

                if (refreshSuccess) {
                    refreshAttempted.set(false)
                    val newAccessToken = runBlocking { session.accessToken().first() }
                    if (!newAccessToken.isNullOrBlank()) {
                        val newRequest = originalRequest.withAuthHeader(newAccessToken)
                        chain.proceed(newRequest)
                    } else {
                        runBlocking { session.clear() }
                        handleRefreshFailure()
                        chain.proceed(originalRequest)
                    }
                } else {
                    runBlocking { session.clear() }
                    handleRefreshFailure()
                    chain.proceed(originalRequest)
                }
            } finally {
                refreshLocks.remove(refreshToken)
            }
        }
    }

    private fun performTokenRefresh(refreshToken: String): Boolean {
        return try {
            val response = runBlocking {
                refreshApi.refresh(RefreshDto(refreshToken))
            }

            runBlocking {
                session.saveTokensAndUser(
                    access = response.accessToken,
                    refresh = response.refreshToken,
                    user = Gson().toJson(response.user)
                )
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun handleRefreshFailure() {
        runBlocking { session.clear() }
        if (appClosing.compareAndSet(false, true)) {
            forceAppClose()
        }
    }

    private fun forceAppClose() {
        try {
            if (context is Activity) {
                context.finishAffinity()
            }
            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(0)

        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_MAIN)
                intent.addCategory(Intent.CATEGORY_HOME)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context.startActivity(intent)
                Thread {
                    Thread.sleep(1000)
                    android.os.Process.killProcess(android.os.Process.myPid())
                }.start()
            } catch (e2: Exception) {
                android.os.Process.killProcess(android.os.Process.myPid())
            }
        }
    }
}