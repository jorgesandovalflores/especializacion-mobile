package com.example.datastore

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private object SettingsKeys {
    val DARK_MODE: Preferences.Key<Boolean> = booleanPreferencesKey("dark_mode")
    val USERNAME: Preferences.Key<String> = stringPreferencesKey("username")
}

class SettingsRepository(
    private val context: Context
) {
    val darkModeFlow: Flow<Boolean> =
        context.appDataStore.data.map { it[SettingsKeys.DARK_MODE] ?: false }

    val usernameFlow: Flow<String> =
        context.appDataStore.data.map { it[SettingsKeys.USERNAME] ?: "" }

    suspend fun setDarkMode(enabled: Boolean) {
        context.appDataStore.edit { prefs ->
            prefs[SettingsKeys.DARK_MODE] = enabled
        }
    }

    suspend fun setUsername(username: String) {
        context.appDataStore.edit { prefs ->
            prefs[SettingsKeys.USERNAME] = username
        }
    }
}