package com.example.android.core.data

import com.example.android.core.domain.LocalCache
import com.example.android.core.domain.SessionExpiration
import com.example.android.core.domain.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first

class SessionExpirationImpl(
    private val session: SessionStore,
    private val localCache: LocalCache
) : SessionExpiration {

    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    override val events: Flow<Unit> = _events.asSharedFlow()

    override suspend fun expire() {
        val hadSession = !session.accessToken().first().isNullOrBlank()
        session.clear()
        localCache.clear()
        if (hadSession) _events.tryEmit(Unit)
    }
}
