package com.example.android.features.signup.domain.store

import com.example.android.features.signup.domain.model.SignUpDraft

interface SignUpDraftStore {
    suspend fun get(): SignUpDraft
    suspend fun savePersonal(givenName: String, familyName: String)
    suspend fun saveEmail(email: String)
    suspend fun clear()
}
