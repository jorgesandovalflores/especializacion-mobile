package com.example.android.core.data

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionStoreEncryptedPrefsTest {

    private lateinit var context: Context
    private lateinit var store: SessionStoreEncryptedPrefs

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteSharedPreferences(PREFS_NAME)
        store = SessionStoreEncryptedPrefs(context, PREFS_NAME)
    }

    @After
    fun tearDown() {
        context.deleteSharedPreferences(PREFS_NAME)
    }

    @Test
    fun saveTokens_thenRead_thenClear_roundtrip() = runBlocking {
        assertNull(store.accessToken().first())
        assertNull(store.refreshToken().first())

        store.saveTokens(ACCESS, REFRESH)

        assertEquals(ACCESS, store.accessToken().first())
        assertEquals(REFRESH, store.refreshToken().first())
        assertEquals(ACCESS, store.currentAccessToken())

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

        val raw = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).all
        assertEquals(2, raw.size)
        raw.values.forEach { value ->
            val stored = value as String
            assertFalse(stored.contains(ACCESS))
            assertFalse(stored.contains(REFRESH))
        }
    }

    @Test
    fun tokens_areReadableFromNewInstance() = runBlocking {
        store.saveTokens(ACCESS, REFRESH)

        val other = SessionStoreEncryptedPrefs(context, PREFS_NAME)

        assertEquals(ACCESS, other.accessToken().first())
        assertEquals(REFRESH, other.refreshToken().first())
    }

    @Test
    fun corruptedOrSwappedValues_areTreatedAsNoSession() = runBlocking {
        store.saveTokens(ACCESS, REFRESH)
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accessRaw = prefs.getString("access_token", null)
        assertNotNull(accessRaw)

        prefs.edit()
            .putString("refresh_token", accessRaw)
            .putString("access_token", "not-base64-###")
            .commit()

        assertNull(store.accessToken().first())
        assertNull(store.refreshToken().first())
    }

    private companion object {
        const val PREFS_NAME = "session_store_test"
        const val ACCESS = "access-token-value"
        const val REFRESH = "refresh-token-value"
    }
}
