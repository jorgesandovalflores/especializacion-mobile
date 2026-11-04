package com.example.android.core.data.realtime

import com.example.android.core.data.RefreshResult
import com.example.android.core.data.TokenRefresher
import com.example.android.core.domain.SessionStore
import com.example.android.core.domain.realtime.RealtimeClient
import com.example.android.core.domain.realtime.RealtimeConnection
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.net.URI

class SocketIoRealtimeClient(
    private val baseUrl: String,
    private val session: SessionStore,
    private val refresher: TokenRefresher,
    private val scope: CoroutineScope
) : RealtimeClient {

    private val socket = MutableStateFlow<Socket?>(null)
    private val _connection = MutableStateFlow(RealtimeConnection.Disconnected)
    override val connection: StateFlow<RealtimeConnection> = _connection.asStateFlow()

    init {
        session.accessToken()
            .distinctUntilChanged()
            .filter { it.isNullOrBlank() }
            .onEach { disconnect() }
            .launchIn(scope)
    }

    override fun connect() {
        if (socket.value != null) return
        _connection.value = RealtimeConnection.Connecting
        scope.launch {
            val token = session.accessToken().first()
            if (token.isNullOrBlank()) {
                _connection.value = RealtimeConnection.Disconnected
                return@launch
            }
            open(token)
        }
    }

    override fun disconnect() {
        socket.value?.let {
            it.off()
            it.disconnect()
        }
        socket.value = null
        _connection.value = RealtimeConnection.Disconnected
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun on(event: String): Flow<String> = socket.flatMapLatest { current ->
        callbackFlow {
            val listener = Emitter.Listener { args -> args.firstOrNull()?.let { trySend(it.toString()) } }
            current?.on(event, listener)
            awaitClose { current?.off(event, listener) }
        }
    }

    private fun open(token: String) {
        val options = IO.Options.builder()
            .setTransports(arrayOf("websocket"))
            .setAuth(mapOf(AUTH_TOKEN to token))
            .setReconnection(true)
            .setReconnectionDelay(RECONNECT_DELAY_MS)
            .setReconnectionDelayMax(RECONNECT_DELAY_MAX_MS)
            .build()
        val created = IO.socket(URI.create(baseUrl.trimEnd('/') + NAMESPACE), options)
        created.on(Socket.EVENT_CONNECT) { _connection.value = RealtimeConnection.Connected }
        created.on(Socket.EVENT_DISCONNECT) {
            if (socket.value === created) _connection.value = RealtimeConnection.Connecting
        }
        created.on(Socket.EVENT_CONNECT_ERROR) { args ->
            val unauthorized = args.firstOrNull()?.toString()?.contains(UNAUTHORIZED) == true
            if (unauthorized) scope.launch { renew(token) }
        }
        socket.value = created
        created.connect()
    }

    private suspend fun renew(staleToken: String) {
        disconnect()
        when (val result = runCatching { refresher.refresh(staleToken) }.getOrNull()) {
            is RefreshResult.Refreshed -> {
                _connection.value = RealtimeConnection.Connecting
                open(result.accessToken)
            }
            else -> _connection.value = RealtimeConnection.Disconnected
        }
    }

    private companion object {
        const val NAMESPACE = "/realtime"
        const val AUTH_TOKEN = "token"
        const val UNAUTHORIZED = "unauthorized"
        const val RECONNECT_DELAY_MS = 1_000L
        const val RECONNECT_DELAY_MAX_MS = 10_000L
    }
}
