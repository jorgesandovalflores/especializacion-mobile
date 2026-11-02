package com.example.driver.core.data.realtime

import com.example.driver.core.data.RefreshResult
import com.example.driver.core.data.TokenRefresher
import com.example.driver.core.domain.SessionExpiration
import com.example.driver.core.domain.SessionStore
import com.example.driver.core.domain.realtime.ConnectionState
import com.example.driver.core.domain.realtime.RealtimeGateway
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.engineio.client.transports.WebSocket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.cancellation.CancellationException

class SocketIoRealtimeClient(
    private val url: String,
    private val session: SessionStore,
    private val refresher: TokenRefresher,
    private val expiration: SessionExpiration,
    private val httpClient: OkHttpClient,
    private val scope: CoroutineScope
) : RealtimeGateway {

    private data class Incoming(val event: String, val payload: String)

    private val lock = Any()
    private var socket: Socket? = null
    private var socketToken: String? = null
    private var wanted = false
    private var authRetries = 0
    private val subscribed: MutableSet<String> = ConcurrentHashMap.newKeySet()
    private val incoming = MutableSharedFlow<Incoming>(extraBufferCapacity = BUFFER_CAPACITY)
    private val _connection = MutableStateFlow(ConnectionState.Disconnected)
    override val connection: StateFlow<ConnectionState> = _connection.asStateFlow()

    init {
        session.accessToken()
            .drop(1)
            .filter { it.isNullOrBlank() }
            .onEach { disconnect() }
            .launchIn(scope)
    }

    override fun connect() {
        synchronized(lock) {
            if (wanted) return
            wanted = true
            authRetries = 0
        }
        scope.launch { open() }
    }

    override fun disconnect() {
        synchronized(lock) {
            wanted = false
            close()
        }
        _connection.value = ConnectionState.Disconnected
    }

    override fun on(event: String): Flow<String> {
        if (subscribed.add(event)) {
            synchronized(lock) { socket?.let { listen(it, event) } }
        }
        return incoming.filter { it.event == event }.map { it.payload }
    }

    override fun emit(event: String, payload: Map<String, Any?>): Boolean {
        val current = synchronized(lock) { socket }?.takeIf { it.connected() } ?: return false
        val json = JSONObject()
        payload.forEach { (key, value) -> if (value != null) json.put(key, value) }
        current.emit(event, json)
        return true
    }

    private suspend fun open() {
        val token = session.accessToken().first()
        if (token.isNullOrBlank()) {
            disconnect()
            return
        }
        synchronized(lock) {
            if (!wanted) return
            close()
            _connection.value = ConnectionState.Connecting
            socketToken = token
            socket = createSocket(token).also { it.connect() }
        }
    }

    private fun close() {
        socket?.let {
            it.off()
            it.disconnect()
        }
        socket = null
    }

    private fun createSocket(token: String): Socket {
        val options = IO.Options().apply {
            forceNew = true
            reconnection = true
            reconnectionDelay = RECONNECT_DELAY_MS
            reconnectionDelayMax = RECONNECT_DELAY_MAX_MS
            transports = arrayOf(WebSocket.NAME)
            auth = mapOf(AUTH_TOKEN to token)
            callFactory = httpClient
            webSocketFactory = httpClient
        }
        val created = IO.socket(URI.create(url), options)
        created.on(Socket.EVENT_CONNECT) {
            if (isCurrent(created)) {
                authRetries = 0
                _connection.value = ConnectionState.Connected
            }
        }
        created.on(Socket.EVENT_DISCONNECT) { args ->
            if (isCurrent(created)) onDisconnected(args.firstOrNull() as? String)
        }
        created.on(Socket.EVENT_CONNECT_ERROR) { args ->
            if (isCurrent(created)) onConnectError(args.firstOrNull())
        }
        subscribed.forEach { listen(created, it) }
        return created
    }

    private fun listen(target: Socket, event: String) {
        target.on(event) { args ->
            incoming.tryEmit(Incoming(event, args.firstOrNull()?.toString().orEmpty()))
        }
    }

    private fun isCurrent(candidate: Socket): Boolean = synchronized(lock) { socket === candidate }

    private fun onDisconnected(reason: String?) {
        when (reason) {
            REASON_SERVER_DISCONNECT -> {
                _connection.value = ConnectionState.Connecting
                scope.launch {
                    delay(SERVER_DISCONNECT_RETRY_MS)
                    open()
                }
            }
            REASON_CLIENT_DISCONNECT -> _connection.value = ConnectionState.Disconnected
            else -> _connection.value = ConnectionState.Connecting
        }
    }

    private fun onConnectError(error: Any?) {
        val message = when (error) {
            is JSONObject -> error.optString(KEY_MESSAGE)
            is Throwable -> error.message
            else -> null
        }
        if (message.equals(UNAUTHORIZED, ignoreCase = true)) {
            val stale = synchronized(lock) { socketToken }
            scope.launch { renewAndReconnect(stale) }
        } else {
            _connection.value = ConnectionState.Connecting
        }
    }

    private suspend fun renewAndReconnect(staleToken: String?) {
        if (authRetries >= MAX_AUTH_RETRIES) {
            disconnect()
            return
        }
        authRetries++
        val result = try {
            refresher.refresh(staleToken)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            null
        }
        when (result) {
            is RefreshResult.Refreshed -> open()
            RefreshResult.Rejected -> {
                disconnect()
                expiration.expire()
            }
            null -> {
                delay(SERVER_DISCONNECT_RETRY_MS)
                open()
            }
        }
    }

    private companion object {
        const val AUTH_TOKEN = "token"
        const val KEY_MESSAGE = "message"
        const val UNAUTHORIZED = "unauthorized"
        const val REASON_SERVER_DISCONNECT = "io server disconnect"
        const val REASON_CLIENT_DISCONNECT = "io client disconnect"
        const val RECONNECT_DELAY_MS = 1_000L
        const val RECONNECT_DELAY_MAX_MS = 5_000L
        const val SERVER_DISCONNECT_RETRY_MS = 3_000L
        const val MAX_AUTH_RETRIES = 2
        const val BUFFER_CAPACITY = 64
    }
}
