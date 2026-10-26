package com.example.android.core.data

import com.example.android.core.domain.DomainException
import com.google.gson.JsonObject
import com.google.gson.JsonParseException
import com.google.gson.JsonParser
import com.google.gson.stream.MalformedJsonException
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

object ErrorMapper {

    const val MESSAGE_CLIENT = "No se pudo procesar la solicitud"
    const val MESSAGE_SERVER = "El servidor no está disponible. Inténtalo más tarde"
    const val MESSAGE_NETWORK = "No se pudo conectar con el servidor. Revisa tu conexión"
    const val MESSAGE_TIMEOUT = "El servidor tardó demasiado en responder"
    const val MESSAGE_UNAUTHORIZED = "Tu sesión expiró. Inicia sesión nuevamente"
    const val MESSAGE_PARSE = "No pudimos leer la respuesta del servidor"
    const val MESSAGE_UNKNOWN = "Ocurrió un error inesperado"

    fun map(t: Throwable): DomainException = when (t) {
        is DomainException -> t
        is HttpException -> mapHttp(t)
        is JsonParseException, is MalformedJsonException ->
            DomainException.ServerException(MESSAGE_PARSE, causeThrowable = t)
        is SocketTimeoutException -> DomainException.NetworkException(MESSAGE_TIMEOUT, causeThrowable = t)
        is IOException -> DomainException.NetworkException(MESSAGE_NETWORK, causeThrowable = t)
        else -> DomainException.UnknownException(MESSAGE_UNKNOWN, causeThrowable = t)
    }

    private fun mapHttp(e: HttpException): DomainException {
        val code = e.code()
        val message = when (code) {
            in 500..599 -> MESSAGE_SERVER
            401 -> e.serverMessage() ?: MESSAGE_UNAUTHORIZED
            in 400..499 -> e.serverMessage() ?: MESSAGE_CLIENT
            else -> MESSAGE_UNKNOWN
        }

        return when (code) {
            400, 422 -> DomainException.ValidationException(message, code, e)
            401 -> DomainException.UnauthorizedException(message, code, e)
            in 400..499 -> DomainException.ClientException(message, code, e)
            in 500..599 -> DomainException.ServerException(message, code, e)
            else -> DomainException.UnknownException(message, code, e)
        }
    }

    private fun HttpException.serverMessage(): String? = runCatching {
        val body = response()?.errorBody()?.string().orEmpty()
        val json = JsonParser.parseString(body).asJsonObject
        json.firstFieldError() ?: json.stringOrNull("message")
    }.getOrNull()?.takeIf { it.isNotBlank() }

    private fun JsonObject.firstFieldError(): String? {
        val errors = get("errors")?.takeIf { it.isJsonArray }?.asJsonArray ?: return null
        val first = errors.firstOrNull()?.takeIf { it.isJsonObject }?.asJsonObject ?: return null
        return first.stringOrNull("message")
    }

    private fun JsonObject.stringOrNull(key: String): String? =
        get(key)?.takeIf { it.isJsonPrimitive }?.asString
}
