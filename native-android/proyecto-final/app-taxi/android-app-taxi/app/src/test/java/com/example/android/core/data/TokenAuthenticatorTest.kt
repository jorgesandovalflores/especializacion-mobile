package com.example.android.core.data

import com.example.android.features.signin.FakeSessionStore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import mockwebserver3.Dispatcher
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import mockwebserver3.RecordedRequest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

class TokenAuthenticatorTest {

    private lateinit var server: MockWebServer
    private lateinit var session: FakeSessionStore
    private lateinit var stack: NetworkStack

    @Before
    fun setUp() {
        server = MockWebServer()
        server.start()
        session = FakeSessionStore()
        runBlocking { session.saveTokens("old-access", "old-refresh") }
        stack = NetworkStack(server, session)
    }

    @After
    fun tearDown() {
        server.close()
    }

    @Test
    fun `con el access token vencido renueva la sesion y repite la peticion`() = runBlocking {
        server.enqueue(json(401, UNAUTHORIZED_JSON))
        server.enqueue(json(200, TOKENS_JSON))
        server.enqueue(json(200, "[]"))

        val menu = stack.api.menu()

        assertTrue(menu.isEmpty())
        val first = server.takeRequest()
        val refresh = server.takeRequest()
        val retry = server.takeRequest()
        assertEquals("Bearer old-access", first.headers["Authorization"])
        assertEquals("/auth/refresh", refresh.url.encodedPath)
        assertNull(refresh.headers["Authorization"])
        assertEquals("""{"refreshToken":"old-refresh"}""", refresh.body?.utf8())
        assertEquals("Bearer new-access", retry.headers["Authorization"])
        assertEquals("new-access", session.currentAccess())
        assertEquals("new-refresh", session.currentRefresh())
        assertEquals(0, stack.expiration.expirations)
    }

    @Test
    fun `varias peticiones con 401 a la vez hacen un solo refresh`() = runBlocking {
        val refreshCalls = AtomicInteger()
        server.dispatcher = object : Dispatcher() {
            override fun dispatch(request: RecordedRequest): MockResponse = when {
                request.url.encodedPath == "/auth/refresh" -> {
                    refreshCalls.incrementAndGet()
                    Thread.sleep(200)
                    json(200, TOKENS_JSON)
                }
                request.headers["Authorization"] == "Bearer new-access" -> json(200, "[]")
                else -> json(401, UNAUTHORIZED_JSON)
            }
        }

        val results = List(4) { async(Dispatchers.IO) { stack.api.menu() } }.awaitAll()

        assertEquals(4, results.size)
        assertEquals(1, refreshCalls.get())
        assertEquals("new-access", session.currentAccess())
    }

    @Test
    fun `si el backend rechaza el refresh token la sesion expira`() = runBlocking {
        server.enqueue(json(401, UNAUTHORIZED_JSON))
        server.enqueue(json(401, UNAUTHORIZED_JSON))

        val error = runCatching { stack.api.menu() }.exceptionOrNull()

        assertEquals(401, (error as HttpException).code())
        assertEquals(2, server.requestCount)
        assertEquals(1, stack.expiration.expirations)
        assertNull(session.currentAccess())
    }

    @Test
    fun `si el refresh falla por un error del servidor la sesion se conserva`() = runBlocking {
        server.enqueue(json(401, UNAUTHORIZED_JSON))
        server.enqueue(json(500, """{"status_code":500,"message":"error"}"""))

        val error = runCatching { stack.api.menu() }.exceptionOrNull()

        assertTrue(error is IOException)
        assertEquals(0, stack.expiration.expirations)
        assertEquals("old-access", session.currentAccess())
        assertEquals("old-refresh", session.currentRefresh())
    }

    @Test
    fun `si el token renovado tambien recibe 401 no vuelve a intentar`() = runBlocking {
        server.enqueue(json(401, UNAUTHORIZED_JSON))
        server.enqueue(json(200, TOKENS_JSON))
        server.enqueue(json(401, UNAUTHORIZED_JSON))

        val error = runCatching { stack.api.menu() }.exceptionOrNull()

        assertEquals(401, (error as HttpException).code())
        assertEquals(3, server.requestCount)
    }
}
