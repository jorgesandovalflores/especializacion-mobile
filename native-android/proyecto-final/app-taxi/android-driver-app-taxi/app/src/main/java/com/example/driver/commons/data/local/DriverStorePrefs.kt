package com.example.driver.commons.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.driver.commons.domain.model.Driver
import com.example.driver.commons.domain.store.DriverStore
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class DriverStorePrefs(
    context: Context,
    private val gson: Gson = Gson()
) : DriverStore {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun driver(): Flow<Driver?> = callbackFlow {
        trySend(read())
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == null || key == KEY_DRIVER) trySend(read())
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        awaitClose { prefs.unregisterOnSharedPreferenceChangeListener(listener) }
    }.distinctUntilChanged()

    override suspend fun saveDriver(driver: Driver) {
        withContext(Dispatchers.IO) { prefs.edit { putString(KEY_DRIVER, gson.toJson(driver)) } }
    }

    override fun isOnline(): Boolean = prefs.getBoolean(KEY_ONLINE, false)

    override suspend fun setOnline(online: Boolean) {
        withContext(Dispatchers.IO) { prefs.edit { putBoolean(KEY_ONLINE, online) } }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) { prefs.edit { clear() } }
    }

    private fun read(): Driver? = prefs.getString(KEY_DRIVER, null)?.let {
        runCatching { gson.fromJson(it, Driver::class.java) }.getOrNull()
    }

    private companion object {
        const val PREFS_NAME = "driver_store"
        const val KEY_DRIVER = "driver"
        const val KEY_ONLINE = "online"
    }
}
