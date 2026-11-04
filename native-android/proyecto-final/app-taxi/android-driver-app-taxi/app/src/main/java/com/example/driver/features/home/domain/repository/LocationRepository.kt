package com.example.driver.features.home.domain.repository

import com.example.driver.features.home.domain.model.UserLocation
import kotlinx.coroutines.flow.Flow

interface LocationRepository {
    fun observeLocation(precise: Boolean): Flow<UserLocation>
}
