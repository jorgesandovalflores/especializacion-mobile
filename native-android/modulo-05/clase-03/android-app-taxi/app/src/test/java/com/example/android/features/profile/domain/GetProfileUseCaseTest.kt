package com.example.android.features.profile.domain

import com.example.android.core.domain.DomainException
import com.example.android.features.profile.FakeProfileRepository
import com.example.android.features.profile.domain.usecase.GetProfileUseCase
import com.example.android.features.profile.domain.usecase.ProfileState
import com.example.android.features.profile.passenger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProfileUseCaseTest {

    private val repo = FakeProfileRepository()

    @Test
    fun `emite Loading y luego el perfil`() = runTest {
        assertEquals(
            listOf(ProfileState.Loading, ProfileState.Success(passenger())),
            GetProfileUseCase(repo)().toList()
        )
    }

    @Test
    fun `un error de red termina en Error con su mensaje`() = runTest {
        repo.failure = DomainException.NetworkException("No se pudo conectar con el servidor")

        assertEquals(
            ProfileState.Error("No se pudo conectar con el servidor"),
            GetProfileUseCase(repo)().toList().last()
        )
    }
}
