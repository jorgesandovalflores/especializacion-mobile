package com.example.android.core.data

import com.example.android.features.signin.FakeSessionStore
import kotlinx.coroutines.runBlocking
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class InterceptorsTest {

    private lateinit var server: MockWebServer
    private val session = FakeSessionStore()

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        runBlocking { session.saveTokens("access", "refresh") }
    }

    @After
    fun tearDown() {
        server.close()
    }

    private fun call(path: String) {
        val client = OkHttpClient.Builder()
            .addInterceptor(HeadersInterceptor(appVersion = "1.0", newRequestId = { "request-1" }))
            .addInterceptor(AuthInterceptor(session))
            .build()
        server.enqueue(json(200, "{}"))
        client.newCall(Request.Builder().url(server.url(path)).build()).execute().close()
    }

    @Test
    fun `AuthInterceptor agrega el access token a las rutas protegidas`() {
        call("/menu/active/PASSENGER")

        assertEquals("Bearer access", server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `AuthInterceptor no envia el token a las rutas publicas de auth`() {
        call("/auth/otp-generate")

        assertNull(server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `sin sesion la peticion sale sin Authorization`() {
        runBlocking { session.clear() }

        call("/menu/active/PASSENGER")

        assertNull(server.takeRequest().headers["Authorization"])
    }

    @Test
    fun `HeadersInterceptor agrega idioma, version e id de la peticion`() {
        call("/menu/active/PASSENGER")

        val request = server.takeRequest()
        assertEquals("es", request.headers["Accept-Language"])
        assertEquals("AppTaxi-Android/1.0", request.headers["User-Agent"])
        assertEquals("request-1", request.headers["X-Request-Id"])
        assertEquals("application/json", request.headers["Accept"])
    }
}
