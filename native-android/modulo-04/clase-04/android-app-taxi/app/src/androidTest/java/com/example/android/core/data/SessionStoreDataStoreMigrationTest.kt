package com.example.android.core.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionStoreDataStoreMigrationTest {

    private val context: Context = InstrumentationRegistry.getInstrumentation().targetContext

    @After
    fun tearDown() {
        runBlocking { SessionStoreDataStore(context).clear() }
        context.deleteSharedPreferences(SessionStoreEncryptedPrefs.DEFAULT_PREFS_NAME)
        context.preferencesDataStoreFile(SessionStoreDataStore.DEFAULT_STORE_NAME).delete()
    }

    @Test
    fun sessionSavedWithSharedPreferences_isReadableFromDataStore() = runBlocking {
        val prefsStore = SessionStoreEncryptedPrefs(context)
        prefsStore.saveTokens(ACCESS, REFRESH)
        prefsStore.setRegistrationPending(true)

        val dataStore = SessionStoreDataStore(context)

        assertEquals(ACCESS, dataStore.accessToken().first())
        assertEquals(REFRESH, dataStore.refreshToken().first())
        assertTrue(dataStore.registrationPending().first())
    }

    private companion object {
        const val ACCESS = "access-token-value"
        const val REFRESH = "refresh-token-value"
    }
}
