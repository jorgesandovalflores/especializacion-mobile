package com.example.android.core.data

import com.example.android.core.domain.DomainException
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.net.SocketTimeoutException

class ErrorMapperTest {

    private fun httpError(code: Int, json: String) = HttpException(
        Response.error<Any>(code, json.toResponseBody("application/json".toMediaType()))
    )

    @Test
    fun `401 se traduce a UnauthorizedException con el mensaje del servidor`() {
        val error = ErrorMapper.map(httpError(401, UNAUTHORIZED_JSON))

        assertTrue(error is DomainException.UnauthorizedException)
        assertEquals("Tu sesión expiró. Inicia sesión nuevamente.", error.message)
    }

    @Test
    fun `401 sin cuerpo usa el mensaje de sesion expirada`() {
        val error = ErrorMapper.map(httpError(401, ""))

        assertEquals(ErrorMapper.MESSAGE_UNAUTHORIZED, error.message)
    }

    @Test
    fun `una respuesta que no es el JSON esperado se traduce a ServerException`() {
        val syntax = ErrorMapper.map(JsonSyntaxException("Expected BEGIN_ARRAY"))
        val malformed = ErrorMapper.map(MalformedJsonException("Unterminated object"))

        assertTrue(syntax is DomainException.ServerException)
        assertTrue(malformed is DomainException.ServerException)
        assertEquals(ErrorMapper.MESSAGE_PARSE, malformed.message)
    }

    @Test
    fun `un timeout se traduce a NetworkException`() {
        val error = ErrorMapper.map(SocketTimeoutException("timeout"))

        assertTrue(error is DomainException.NetworkException)
        assertEquals(ErrorMapper.MESSAGE_TIMEOUT, error.message)
    }
}
