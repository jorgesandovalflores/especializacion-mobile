package com.example.android.features.home.domain

import com.example.android.features.home.FakeLocationRepository
import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.LocationException
import com.example.android.features.home.domain.usecase.LocationState
import com.example.android.features.home.domain.usecase.ObserveLocationUseCase
import com.example.android.features.home.userLocation
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ObserveLocationUseCaseTest {

    @Test
    fun `emite Loading y luego cada ubicacion como Success`() = runTest {
        val repo = FakeLocationRepository()
        val first = userLocation()
        repo.updates.emit(first)

        val states = ObserveLocationUseCase(repo)(precise = true).take(2).toList()

        assertEquals(listOf(LocationState.Loading, LocationState.Success(first)), states)
        assertEquals(listOf(true), repo.requests)
    }

    @Test
    fun `el GPS apagado se emite como Error SettingsDisabled`() = runTest {
        val repo = FakeLocationRepository().apply {
            failure = LocationException(LocationError.SettingsDisabled)
        }

        val states = ObserveLocationUseCase(repo)(precise = true).toList()

        assertEquals(
            listOf(LocationState.Loading, LocationState.Error(LocationError.SettingsDisabled)),
            states
        )
    }

    @Test
    fun `un error desconocido se emite como Error Unavailable`() = runTest {
        val repo = FakeLocationRepository().apply { failure = IllegalStateException("boom") }

        val last = ObserveLocationUseCase(repo)(precise = false).toList().last()

        assertEquals(LocationState.Error(LocationError.Unavailable), last)
        assertEquals(listOf(false), repo.requests)
    }
}
