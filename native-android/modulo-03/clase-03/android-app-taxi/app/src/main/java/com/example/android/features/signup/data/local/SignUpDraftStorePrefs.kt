package com.example.android.features.signup.data.local

import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.store.SignUpDraftStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SignUpDraftStorePrefs(
    private val prefs: SharedPreferences
) : SignUpDraftStore {

    override suspend fun get(): SignUpDraft = withContext(Dispatchers.IO) {
        SignUpDraft(
            givenName = prefs.getString(KEY_GIVEN_NAME, null).orEmpty(),
            familyName = prefs.getString(KEY_FAMILY_NAME, null).orEmpty(),
            email = prefs.getString(KEY_EMAIL, null).orEmpty()
        )
    }

    override suspend fun savePersonal(givenName: String, familyName: String) {
        prefs.edit {
            putString(KEY_GIVEN_NAME, givenName)
            putString(KEY_FAMILY_NAME, familyName)
        }
    }

    override suspend fun saveEmail(email: String) {
        prefs.edit { putString(KEY_EMAIL, email) }
    }

    override suspend fun clear() {
        prefs.edit { clear() }
    }

    companion object {
        const val PREFS_NAME = "signup_draft"
        private const val KEY_GIVEN_NAME = "given_name"
        private const val KEY_FAMILY_NAME = "family_name"
        private const val KEY_EMAIL = "email"
    }
}
