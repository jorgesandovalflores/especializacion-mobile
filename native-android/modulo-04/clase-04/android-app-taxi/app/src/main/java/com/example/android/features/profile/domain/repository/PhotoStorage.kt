package com.example.android.features.profile.domain.repository

import com.example.android.features.profile.domain.model.UploadEvent
import kotlinx.coroutines.flow.Flow

interface PhotoStorage {
    fun upload(image: ByteArray): Flow<UploadEvent>
}
