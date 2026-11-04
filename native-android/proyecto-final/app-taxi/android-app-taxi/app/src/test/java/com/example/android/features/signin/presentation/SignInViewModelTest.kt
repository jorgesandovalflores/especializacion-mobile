package com.example.android.features.signin.presentation

import com.example.android.core.domain.DomainException
import com.example.android.features.signin.FakeAuthRepository
import com.example.android.features.signin.FakeSessionStore
import com.example.android.features.signin.domain.model.OtpGenerateResult
import com.example.android.features.signin.domain.usecase.OtpGenerateState
import com.example.android.features.signin.domain.usecase.OtpGenerateUseCase
import com.example.android.features.signin.domain.usecase.OtpValidateState
import com.example.android.features.signin.domain.usecase.OtpValidateUseCase
import com.example.android.features.signin.validResult
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SignInViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()
    private val session = FakeSessionStore()

    @Before
    fun setUp() = Dispatchers.setMain(dispatcher)

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun viewModel(repo: FakeAuthRepository) = SignInViewModel(
        otpGenerateUseCase = OtpGenerateUseCase(repo),
        otpValidateUseCase = OtpValidateUseCase(repo, session)
    )

    @Test
    fun `pedir el codigo pasa por Loading y termina en Success`() = runTest(dispatcher) {
        val response = CompletableDeferred<OtpGenerateResult>()
        val vm = viewModel(FakeAuthRepository(onGenerate = { response.await() }))

        vm.callGenerateOtp("987654321")
        assertEquals(OtpGenerateState.Loading, vm.generateOtpUi.value)

        response.complete(OtpGenerateResult("2026-11-02T15:00:00Z"))
        assertEquals(OtpGenerateState.Success("987654321", "2026-11-02T15:00:00Z"), vm.generateOtpUi.value)
    }

    @Test
    fun `mientras carga ignora un segundo pedido`() = runTest(dispatcher) {
        val response = CompletableDeferred<OtpGenerateResult>()
        val repo = FakeAuthRepository(onGenerate = { response.await() })
        val vm = viewModel(repo)

        vm.callGenerateOtp("987654321")
        vm.callGenerateOtp("987654321")

        assertEquals(listOf("51987654321"), repo.generatedFor)
    }

    @Test
    fun `un error queda en el estado y clearGenerateState lo limpia`() = runTest(dispatcher) {
        val vm = viewModel(FakeAuthRepository(onGenerate = {
            throw DomainException.ValidationException("Ya existe un código activo.")
        }))

        vm.callGenerateOtp("987654321")
        assertEquals(OtpGenerateState.Error("Ya existe un código activo."), vm.generateOtpUi.value)

        vm.clearGenerateState()
        assertEquals(OtpGenerateState.Idle, vm.generateOtpUi.value)
    }

    @Test
    fun `un pasajero nuevo valida el codigo y debe registrarse`() = runTest(dispatcher) {
        val vm = viewModel(FakeAuthRepository(onValidate = { phone, _ -> validResult(phone, "INACTIVE_REGISTER") }))

        vm.callValidateOtp("987654321", "1234")

        assertEquals(OtpValidateState.Success(showRegister = true), vm.validateOtpUi.value)
        assertTrue(session.isRegistrationPending())
    }
}
