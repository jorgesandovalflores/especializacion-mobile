package com.example.android.core.data

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class SessionStoreEncryptedPrefs(
    context: Context
) : SessionStore {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "session_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val KEY_ACCESS = "access_token"
    private val KEY_REFRESH = "refresh_token"
    private val KEY_USER = "user"

    override suspend fun saveTokensAndUser(access: String, refresh: String, user: String) {
        prefs.edit()
            .putString(KEY_ACCESS, access)
            .putString(KEY_REFRESH, refresh)
            .putString(KEY_USER, user)
            .apply()
    }

    override fun accessToken(): Flow<String?> = prefs.asFlow(KEY_ACCESS)
    override fun refreshToken(): Flow<String?> = prefs.asFlow(KEY_REFRESH)
    override fun getUser(): Flow<String?> = prefs.asFlow(KEY_USER)

    override suspend fun clear() { prefs.edit().clear().apply() }

    private fun SharedPreferences.asFlow(key: String): Flow<String?> = callbackFlow {
        trySend(getString(key, null))
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { sp, k ->
            if (k == key) trySend(sp.getString(k, null))
        }
        registerOnSharedPreferenceChangeListener(listener)
        awaitClose { unregisterOnSharedPreferenceChangeListener(listener) }
    }
}