package com.example.android.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionStoreDataStoreTest {

    private lateinit var context: Context
    private lateinit var scope: CoroutineScope
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var store: SessionStoreDataStore

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.preferencesDataStoreFile(STORE_NAME).delete()
        scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        dataStore = PreferenceDataStoreFactory.create(scope = scope) {
            context.preferencesDataStoreFile(STORE_NAME)
        }
        store = SessionStoreDataStore(dataStore)
    }

    @After
    fun tearDown() {
        scope.cancel()
        context.preferencesDataStoreFile(STORE_NAME).delete()
    }

    @Test
    fun saveTokens_thenRead_thenClear_roundtrip() = runBlocking {
        assertNull(store.accessToken().first())
        assertNull(store.refreshToken().first())

        store.saveTokens(ACCESS, REFRESH)

        assertEquals(ACCESS, store.accessToken().first())
        assertEquals(REFRESH, store.refreshToken().first())

        store.clear()

        assertNull(store.accessToken().first())
        assertNull(store.refreshToken().first())
    }

    @Test
    fun saveTokens_overwritesPreviousTokens() = runBlocking {
        store.saveTokens(ACCESS, REFRESH)
        store.saveTokens("new-$ACCESS", "new-$REFRESH")

        assertEquals("new-$ACCESS", store.accessToken().first())
        assertEquals("new-$REFRESH", store.refreshToken().first())
    }

    @Test
    fun tokens_arePersistedEncrypted() = runBlocking {
        store.saveTokens(ACCESS, REFRESH)

        val raw = dataStore.data.first().asMap()
        assertEquals(2, raw.size)
        raw.values.forEach { value ->
            val stored = value as String
            assertFalse(stored.contains(ACCESS))
            assertFalse(stored.contains(REFRESH))
        }
    }

    @Test
    fun registrationPending_isFalseByDefault_andClearedWithTheSession() = runBlocking {
        assertFalse(store.registrationPending().first())

        store.setRegistrationPending(true)
        assertTrue(store.registrationPending().first())

        store.clear()
        assertFalse(store.registrationPending().first())
    }

    @Test
    fun corruptedOrSwappedValues_areTreatedAsNoSession() = runBlocking {
        store.saveTokens(ACCESS, REFRESH)
        val accessKey = stringPreferencesKey("access_token")
        val refreshKey = stringPreferencesKey("refresh_token")
        val accessRaw = dataStore.data.first()[accessKey]!!

        dataStore.edit { prefs ->
            prefs[refreshKey] = accessRaw
            prefs[accessKey] = "not-base64-###"
        }

        assertNull(store.accessToken().first())
        assertNull(store.refreshToken().first())
    }

    private companion object {
        const val STORE_NAME = "session_store_test"
        const val ACCESS = "access-token-value"
        const val REFRESH = "refresh-token-value"
    }
}
