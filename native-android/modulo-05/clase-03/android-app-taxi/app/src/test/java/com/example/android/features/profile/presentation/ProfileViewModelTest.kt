package com.example.android.features.profile.presentation

import com.example.android.features.profile.FakeImageCompressor
import com.example.android.features.profile.FakePhotoStorage
import com.example.android.features.profile.FakeProfileRepository
import com.example.android.features.profile.domain.usecase.ChangePhotoUseCase
import com.example.android.features.profile.domain.usecase.GetProfileUseCase
import com.example.android.features.profile.domain.usecase.PhotoState
import com.example.android.features.profile.domain.usecase.ProfileState
import com.example.android.features.profile.passenger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = FakeProfileRepository()
    private val storage = FakePhotoStorage()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = ProfileViewModel(
        getProfile = GetProfileUseCase(repo),
        changePhoto = ChangePhotoUseCase(FakeImageCompressor(), storage, repo)
    )

    @Test
    fun `al abrir carga el perfil`() = runTest(dispatcher) {
        assertEquals(ProfileState.Success(passenger()), viewModel().profile.value)
    }

    @Test
    fun `al subir la foto el perfil se actualiza con la nueva URL`() = runTest(dispatcher) {
        val vm = viewModel()

        vm.onPhotoPicked("content://media/picker/1")

        val updated = passenger(photoUrl = "https://storage/avatar.jpg")
        assertEquals(PhotoState.Success(updated), vm.photo.value)
        assertEquals(ProfileState.Success(updated), vm.profile.value)

        vm.onToastShown()
        assertNull(vm.photo.value)
    }
}
