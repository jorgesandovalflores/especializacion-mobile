package com.example.android.features.home.domain.usecase

import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.LocationException
import com.example.android.features.home.domain.model.UserLocation
import com.example.android.features.home.domain.repository.LocationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed interface LocationState {
    data object Loading : LocationState
    data class Success(val location: UserLocation) : LocationState
    data class Error(val error: LocationError) : LocationState
}

class ObserveLocationUseCase(
    private val repo: LocationRepository
) {
    operator fun invoke(precise: Boolean): Flow<LocationState> = flow {
        emit(LocationState.Loading)
        emitAll(repo.observeLocation(precise).map { LocationState.Success(it) })
    }.catch { emit(LocationState.Error((it as? LocationException)?.error ?: LocationError.Unavailable)) }
}
