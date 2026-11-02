package com.example.android.core.data

import com.example.android.core.domain.SessionExpiration
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import mockwebserver3.MockResponse
import mockwebserver3.MockWebServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET

interface ProtectedApi {
    @GET("menu/active/PASSENGER")
    suspend fun menu(): List<Map<String, Any>>
}

class FakeSessionExpiration(
    private val session: SessionStore
) : SessionExpiration {
    var expirations = 0
        private set

    override val events: Flow<Unit> = MutableSharedFlow()

    override suspend fun expire() {
        expirations++
        session.clear()
    }
}

class NetworkStack(server: MockWebServer, session: SessionStore) {
    val expiration = FakeSessionExpiration(session)

    private val publicClient = OkHttpClient.Builder().build()

    private val refreshApi: RefreshApi = retrofit(server, publicClient).create(RefreshApi::class.java)

    private val client = publicClient.newBuilder()
        .addInterceptor(AuthInterceptor(session))
        .authenticator(TokenAuthenticator(TokenRefresher(session, refreshApi), expiration))
        .build()

    val api: ProtectedApi = retrofit(server, client).create(ProtectedApi::class.java)

    private fun retrofit(server: MockWebServer, client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(server.url("/"))
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}

fun json(code: Int, body: String) = MockResponse(
    code = code,
    headers = okhttp3.Headers.headersOf("Content-Type", "application/json"),
    body = body
)

const val TOKENS_JSON = """{"accessToken":"new-access","refreshToken":"new-refresh","user":{"id":"1"}}"""
const val UNAUTHORIZED_JSON = """{"status_code":401,"message":"Tu sesión expiró. Inicia sesión nuevamente.","errors":[]}"""
