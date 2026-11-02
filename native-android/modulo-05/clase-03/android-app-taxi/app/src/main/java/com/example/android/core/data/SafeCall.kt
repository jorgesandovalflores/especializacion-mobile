package com.example.android.core.data

import kotlin.coroutines.cancellation.CancellationException

inline fun <T> safeCall(block: () -> T): T = try {
    block()
} catch (e: CancellationException) {
    throw e
} catch (t: Throwable) {
    throw ErrorMapper.map(t)
}
