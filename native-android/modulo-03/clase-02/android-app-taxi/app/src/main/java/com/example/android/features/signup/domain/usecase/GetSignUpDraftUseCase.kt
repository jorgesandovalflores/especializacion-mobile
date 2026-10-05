package com.example.android.features.signup.domain.usecase

import com.example.android.features.signup.domain.model.SignUpDraft
import com.example.android.features.signup.domain.store.SignUpDraftStore

class GetSignUpDraftUseCase(
    private val draftStore: SignUpDraftStore
) {
    suspend operator fun invoke(): SignUpDraft = draftStore.get()
}
