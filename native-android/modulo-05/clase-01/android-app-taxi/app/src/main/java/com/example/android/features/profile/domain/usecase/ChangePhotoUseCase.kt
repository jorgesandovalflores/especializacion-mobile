package com.example.android.features.profile.domain.usecase

import com.example.android.commons.domain.model.Passenger
import com.example.android.core.domain.toDomainException
import com.example.android.features.profile.domain.model.UploadEvent
import com.example.android.features.profile.domain.repository.ImageCompressor
import com.example.android.features.profile.domain.repository.PhotoStorage
import com.example.android.features.profile.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

sealed interface PhotoState {
    data object Preparing : PhotoState
    data class Uploading(val percent: Int) : PhotoState
    data class Success(val passenger: Passenger) : PhotoState
    data class Error(val message: String) : PhotoState
}

class ChangePhotoUseCase(
    private val compressor: ImageCompressor,
    private val storage: PhotoStorage,
    private val repo: ProfileRepository
) {
    operator fun invoke(uri: String): Flow<PhotoState> = flow {
        emit(PhotoState.Preparing)
        val image = compressor.compress(uri)
        emitAll(
            storage.upload(image).map { event ->
                when (event) {
                    is UploadEvent.Progress -> PhotoState.Uploading(event.percent)
                    is UploadEvent.Done -> PhotoState.Success(repo.savePhoto(event.url))
                }
            }
        )
    }.catch { emit(PhotoState.Error(it.toDomainException().message)) }
}
