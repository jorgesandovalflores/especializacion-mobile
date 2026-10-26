package com.example.android.features.profile.domain.usecase

import com.example.android.commons.domain.model.Passenger
import com.example.android.core.domain.toDomainException
import com.example.android.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

sealed interface ProfileState {
    data object Loading : ProfileState
    data class Success(val passenger: Passenger) : ProfileState
    data class Error(val message: String) : ProfileState
}

class GetProfileUseCase(
    private val repo: ProfileRepository
) {
    operator fun invoke(): Flow<ProfileState> = flow {
        emit(ProfileState.Loading)
        emit(ProfileState.Success(repo.me()))
    }.catch { emit(ProfileState.Error(it.toDomainException().message)) }
}
