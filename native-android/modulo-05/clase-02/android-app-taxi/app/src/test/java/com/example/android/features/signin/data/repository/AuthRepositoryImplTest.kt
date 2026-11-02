package com.example.android.features.signin.data.repository

import com.example.android.core.data.ErrorMapper
import com.example.android.core.domain.DomainException
import com.example.android.features.signin.data.remote.AuthApi
import com.example.android.features.signin.data.remote.dto.AuthOtpGenerateRequest
import com.example.android.features.signin.data.remote.dto.AuthOtpGenerateResponse
import com.example.android.features.signin.data.remote.dto.AuthOtpValidateRequest
import com.example.android.features.signin.data.remote.dto.AuthOtpValidateResponse
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class AuthRepositoryImplTest {

    private class FakeAuthApi(private val onGenerate: suspend () -> AuthOtpGenerateResponse) : AuthApi {
        var lastRequest: AuthOtpGenerateRequest? = null

        override suspend fun otpGenerate(body: AuthOtpGenerateRequest): AuthOtpGenerateResponse {
            lastRequest = body
            return onGenerate()
        }

        override suspend fun otpValidate(body: AuthOtpValidateRequest): AuthOtpValidateResponse =
            throw UnsupportedOperationException()
    }

    private fun httpError(code: Int, json: String) = HttpException(
        Response.error<Any>(code, json.toResponseBody("application/json".toMediaType()))
    )

    private suspend fun generateFailing(error: Throwable): Throwable {
        val repo = AuthRepositoryImpl(FakeAuthApi { throw error })
        return runCatching { repo.otpGenerate("51987654321") }.exceptionOrNull()
            ?: throw AssertionError("Se esperaba una excepción")
    }

    @Test
    fun `respuesta exitosa se mapea a dominio`() = runTest {
        val api = FakeAuthApi { AuthOtpGenerateResponse(true, "2026-09-28T15:02:00.000Z", 120, "abc") }

        val result = AuthRepositoryImpl(api).otpGenerate("51987654321")

        assertEquals("2026-09-28T15:02:00.000Z", result.expiresAt)
        assertEquals("51987654321", api.lastRequest?.phone)
    }

    @Test
    fun `422 del backend se traduce a ValidationException con el mensaje del servidor`() = runTest {
        val error = generateFailing(httpError(422, """{"status_code":422,"message":"Ya existe un código activo."}"""))

        assertTrue(error is DomainException.ValidationException)
        assertEquals("Ya existe un código activo.", error.message)
    }

    @Test
    fun `400 de validacion usa el primer mensaje de errors`() = runTest {
        val body = """{"status_code":400,"message":"Bad Request","errors":[{"field":"phone","message":"Debe tener al menos 11 caracteres."}]}"""

        val error = generateFailing(httpError(400, body))

        assertEquals("Debe tener al menos 11 caracteres.", error.message)
    }

    @Test
    fun `500 sin cuerpo JSON usa el mensaje generico de servidor`() = runTest {
        val error = generateFailing(httpError(500, "<html>"))

        assertTrue(error is DomainException.ServerException)
        assertEquals(ErrorMapper.MESSAGE_SERVER, error.message)
    }

    @Test
    fun `500 del backend ignora el mensaje del servidor y usa el generico`() = runTest {
        val body = """{"status_code":500,"message":"Ocurrió un error inesperado en el servidor.","errors":[]}"""

        val error = generateFailing(httpError(500, body))

        assertTrue(error is DomainException.ServerException)
        assertEquals(ErrorMapper.MESSAGE_SERVER, error.message)
    }

    @Test
    fun `429 por intentos se traduce a ClientException con el mensaje del servidor`() = runTest {
        val body = """{"status_code":429,"message":"Superaste el número de intentos. Solicita un código nuevo.","errors":[]}"""

        val error = generateFailing(httpError(429, body))

        assertTrue(error is DomainException.ClientException)
        assertEquals("Superaste el número de intentos. Solicita un código nuevo.", error.message)
    }

    @Test
    fun `404 sin errors usa el message del cuerpo`() = runTest {
        val error = generateFailing(httpError(404, """{"status_code":404,"message":"Not Found","errors":[]}"""))

        assertTrue(error is DomainException.ClientException)
        assertEquals("Not Found", error.message)
    }

    @Test
    fun `sin red se traduce a NetworkException`() = runTest {
        val error = generateFailing(IOException("unreachable"))

        assertTrue(error is DomainException.NetworkException)
        assertEquals(ErrorMapper.MESSAGE_NETWORK, error.message)
    }

    @Test
    fun `la cancelacion no se convierte en error de dominio`() = runTest {
        val error = generateFailing(CancellationException("cancelado"))

        assertTrue(error is CancellationException)
    }
}
