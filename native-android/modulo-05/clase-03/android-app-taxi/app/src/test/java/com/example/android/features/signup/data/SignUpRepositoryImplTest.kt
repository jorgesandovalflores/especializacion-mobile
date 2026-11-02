package com.example.android.features.signup.data

import com.example.android.commons.data.remote.dto.PassengerDto
import com.example.android.core.domain.DomainException
import com.example.android.features.signup.data.remote.SignUpApi
import com.example.android.features.signup.data.remote.dto.SignUpRequest
import com.example.android.features.signup.data.repository.SignUpRepositoryImpl
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class SignUpRepositoryImplTest {

    private class FakeSignUpApi(private val onSignUp: suspend (SignUpRequest) -> PassengerDto) : SignUpApi {
        var lastRequest: SignUpRequest? = null

        override suspend fun signUp(body: SignUpRequest): PassengerDto {
            lastRequest = body
            return onSignUp(body)
        }
    }

    @Test
    fun `respuesta exitosa se mapea al pasajero de dominio`() = runTest {
        val api = FakeSignUpApi { body ->
            PassengerDto("1", "51987654321", body.givenName, body.familyName, body.email, null, "ACTIVE")
        }

        val passenger = SignUpRepositoryImpl(api).signUp("Jorge", "Sandoval", "jorge@example.com")

        assertEquals(SignUpRequest("Jorge", "Sandoval", "jorge@example.com"), api.lastRequest)
        assertEquals("ACTIVE", passenger.status)
        assertEquals("Jorge", passenger.givenName)
    }

    @Test
    fun `422 por correo usado se traduce a ValidationException con el mensaje del servidor`() = runTest {
        val body = """{"status_code":422,"message":"El correo ya está registrado por otro pasajero."}"""
        val api = FakeSignUpApi {
            throw HttpException(Response.error<Any>(422, body.toResponseBody("application/json".toMediaType())))
        }

        val error = runCatching { SignUpRepositoryImpl(api).signUp("Jorge", "Sandoval", "jorge@example.com") }
            .exceptionOrNull()

        assertTrue(error is DomainException.ValidationException)
        assertEquals("El correo ya está registrado por otro pasajero.", error?.message)
    }
}
