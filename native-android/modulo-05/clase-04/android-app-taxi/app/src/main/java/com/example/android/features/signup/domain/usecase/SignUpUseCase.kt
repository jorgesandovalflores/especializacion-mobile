package com.example.android.features.signup.domain.usecase

import com.example.android.core.domain.SessionStore
import com.example.android.core.domain.toDomainException
import com.example.android.features.signup.domain.repository.SignUpRepository
import com.example.android.features.signup.domain.store.SignUpDraftStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed interface SignUpState {
    data object Idle : SignUpState
    data object Loading : SignUpState
    data object Success : SignUpState
    data class Error(val message: String) : SignUpState
}

class SignUpUseCase(
    private val repo: SignUpRepository,
    private val draftStore: SignUpDraftStore,
    private val session: SessionStore
) {
    operator fun invoke(): Flow<SignUpState> = flow {
        emit(SignUpState.Loading)
        val draft = draftStore.get()
        repo.signUp(
            givenName = draft.givenName.trim(),
            familyName = draft.familyName.trim(),
            email = draft.email.trim()
        )
        session.setRegistrationPending(false)
        draftStore.clear()
        emit(SignUpState.Success)
    }.catch { emit(SignUpState.Error(it.toDomainException().message)) }
}
