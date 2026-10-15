package com.example.android.features.home.domain.repository

import com.example.android.features.home.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeLocation(precise: Boolean): Flow<UserLocation>
}
