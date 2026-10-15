package com.example.android.features.home.presentation

import com.example.android.core.domain.LocalCache
import com.example.android.features.home.FakeLocationRepository
import com.example.android.features.home.domain.model.LocationError
import com.example.android.features.home.domain.model.LocationException
import com.example.android.features.home.domain.usecase.ObserveLocationUseCase
import com.example.android.features.home.domain.usecase.SignOutUseCase
import com.example.android.features.home.userLocation
import com.example.android.features.signin.FakeSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val repo = FakeLocationRepository()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel() = HomeViewModel(
        signOut = SignOutUseCase(FakeSessionStore(), LocalCache { }),
        observeLocation = ObserveLocationUseCase(repo)
    )

    private fun TestScope.collect(vm: HomeViewModel) =
        backgroundScope.launch(dispatcher) { vm.mapUi.collect { } }

    @Test
    fun `sin permiso muestra el panel de permisos`() = runTest(dispatcher) {
        val vm = viewModel()
        collect(vm)

        assertEquals(MapUiState.Idle, vm.mapUi.value)

        vm.onPermissionChecked(LocationPermission.Denied(blocked = false))

        assertEquals(MapUiState.Permission(blocked = false), vm.mapUi.value)
        assertEquals(emptyList<Boolean>(), repo.requests)
    }

    @Test
    fun `con permiso preciso pasa de Idle a Success`() = runTest(dispatcher) {
        val vm = viewModel()
        collect(vm)

        vm.onPermissionResult(LocationPermission.Granted(precise = true))
        assertEquals(MapUiState.Idle, vm.mapUi.value)

        val location = userLocation()
        repo.updates.emit(location)

        assertEquals(MapUiState.Success(location, precise = true), vm.mapUi.value)
        assertEquals(listOf(true), repo.requests)
    }

    @Test
    fun `un rechazo definitivo no se pierde al volver del dialogo`() = runTest(dispatcher) {
        val vm = viewModel()
        collect(vm)

        vm.onPermissionResult(LocationPermission.Denied(blocked = true))
        vm.onPermissionChecked(LocationPermission.Denied(blocked = false))

        assertEquals(MapUiState.Permission(blocked = true), vm.mapUi.value)
    }

    @Test
    fun `reintentar vuelve a pedir la ubicacion tras un error`() = runTest(dispatcher) {
        repo.failure = LocationException(LocationError.SettingsDisabled)
        val vm = viewModel()
        collect(vm)

        vm.onPermissionResult(LocationPermission.Granted(precise = false))
        assertEquals(MapUiState.Error(LocationError.SettingsDisabled), vm.mapUi.value)

        repo.failure = null
        val location = userLocation(accuracyMeters = 900f)
        repo.updates.emit(location)
        vm.retry()

        assertEquals(MapUiState.Success(location, precise = false), vm.mapUi.value)
        assertEquals(listOf(false, false), repo.requests)
    }

    @Test
    fun `sin pantalla visible deja de escuchar el GPS a los 5 segundos`() = runTest(dispatcher) {
        val vm = viewModel()
        val screen = collect(vm)
        vm.onPermissionResult(LocationPermission.Granted(precise = true))
        assertEquals(1, repo.activeCollectors)

        screen.cancel()
        advanceTimeBy(4_999)
        assertEquals(1, repo.activeCollectors)

        advanceTimeBy(2)
        runCurrent()
        assertEquals(0, repo.activeCollectors)
    }
}
