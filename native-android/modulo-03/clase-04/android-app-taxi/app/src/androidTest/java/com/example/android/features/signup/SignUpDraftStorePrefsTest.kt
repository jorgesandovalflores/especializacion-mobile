package com.example.android.features.signup

import android.content.Context
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.android.features.signup.data.local.SignUpDraftStorePrefs
import com.example.android.features.signup.domain.model.SignUpDraft
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SignUpDraftStorePrefsTest {

    private lateinit var context: Context
    private lateinit var store: SignUpDraftStorePrefs

    @Before
    fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.deleteSharedPreferences(PREFS_NAME)
        store = newStore()
    }

    @After
    fun tearDown() {
        context.deleteSharedPreferences(PREFS_NAME)
    }

    @Test
    fun get_withoutSavedDraft_returnsEmptyDraft() = runBlocking {
        assertEquals(SignUpDraft(), store.get())
    }

    @Test
    fun savePersonal_thenSaveEmail_keepsBothSteps() = runBlocking {
        store.savePersonal(DRAFT.givenName, DRAFT.familyName)
        store.saveEmail(DRAFT.email)

        assertEquals(DRAFT, store.get())
    }

    @Test
    fun saveEmail_doesNotOverwriteTheNames() = runBlocking {
        store.savePersonal(DRAFT.givenName, DRAFT.familyName)
        store.saveEmail("otro@example.com")

        assertEquals(DRAFT.copy(email = "otro@example.com"), store.get())
    }

    @Test
    fun draft_isReadableFromNewInstance() = runBlocking {
        store.savePersonal(DRAFT.givenName, DRAFT.familyName)
        store.saveEmail(DRAFT.email)

        assertEquals(DRAFT, newStore().get())
    }

    @Test
    fun clear_removesTheDraft() = runBlocking {
        store.savePersonal(DRAFT.givenName, DRAFT.familyName)

        store.clear()

        assertEquals(SignUpDraft(), store.get())
    }

    private fun newStore() =
        SignUpDraftStorePrefs(context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE))

    private companion object {
        const val PREFS_NAME = "signup_draft_test"
        val DRAFT = SignUpDraft(givenName = "Jorge", familyName = "Sandoval", email = "jorge@example.com")
    }
}
