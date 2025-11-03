package com.example.demoworkermanager.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "app_state")

class AppStateManager(private val context: Context) {

    // Keys para DataStore
    private val locationTrackingActiveKey = booleanPreferencesKey("location_tracking_active")
    private val trackingWithServiceKey = booleanPreferencesKey("tracking_with_service")

    val isLocationTrackingActive: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[locationTrackingActiveKey] ?: false
        }

    val isTrackingWithService: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[trackingWithServiceKey] ?: false
        }

    suspend fun startLocationTracking(withService: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[locationTrackingActiveKey] = true
            preferences[trackingWithServiceKey] = withService
        }
    }

    suspend fun stopLocationTracking() {
        context.dataStore.edit { preferences ->
            preferences[locationTrackingActiveKey] = false
        }
    }
}