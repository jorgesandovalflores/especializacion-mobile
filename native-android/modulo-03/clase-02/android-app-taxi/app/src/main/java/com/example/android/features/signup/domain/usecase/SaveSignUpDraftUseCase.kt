package com.example.android.features.signup.domain.usecase

import com.example.android.features.signup.domain.store.SignUpDraftStore

class SaveSignUpDraftUseCase(
    private val draftStore: SignUpDraftStore
) {
    suspend fun personal(givenName: String, familyName: String) =
        draftStore.savePersonal(givenName, familyName)

    suspend fun email(email: String) = draftStore.saveEmail(email)
}
