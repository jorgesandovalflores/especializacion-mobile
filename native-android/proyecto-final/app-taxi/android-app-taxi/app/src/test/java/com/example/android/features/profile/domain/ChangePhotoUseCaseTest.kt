package com.example.android.features.profile.domain

import com.example.android.core.domain.DomainException
import com.example.android.features.profile.FakeImageCompressor
import com.example.android.features.profile.FakePhotoStorage
import com.example.android.features.profile.FakeProfileRepository
import com.example.android.features.profile.domain.usecase.ChangePhotoUseCase
import com.example.android.features.profile.domain.usecase.PhotoState
import com.example.android.features.profile.passenger
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ChangePhotoUseCaseTest {

    private val compressor = FakeImageCompressor()
    private val storage = FakePhotoStorage()
    private val repo = FakeProfileRepository()
    private val useCase = ChangePhotoUseCase(compressor, storage, repo)

    @Test
    fun `comprime, sube con progreso y guarda la URL en el backend`() = runTest {
        val states = useCase("content://media/picker/1").toList()

        assertEquals(
            listOf(
                PhotoState.Preparing,
                PhotoState.Uploading(0),
                PhotoState.Uploading(50),
                PhotoState.Uploading(100),
                PhotoState.Success(passenger(photoUrl = "https://storage/avatar.jpg"))
            ),
            states
        )
        assertEquals(listOf("content://media/picker/1"), compressor.compressed)
        assertEquals(listOf("https://storage/avatar.jpg"), repo.savedUrls)
    }

    @Test
    fun `si Storage falla no guarda nada y muestra el mensaje`() = runTest {
        storage.failure = DomainException.NetworkException("No se pudo subir la foto. Revisa tu conexión")

        val last = useCase("content://media/picker/1").toList().last()

        assertEquals(PhotoState.Error("No se pudo subir la foto. Revisa tu conexión"), last)
        assertEquals(emptyList<String>(), repo.savedUrls)
    }

    @Test
    fun `si el backend rechaza la URL termina en Error`() = runTest {
        repo.failure = DomainException.ValidationException("La foto no pertenece a tu perfil. Vuelve a subirla.")

        val last = useCase("content://media/picker/1").toList().last()

        assertEquals(PhotoState.Error("La foto no pertenece a tu perfil. Vuelve a subirla."), last)
    }
}
