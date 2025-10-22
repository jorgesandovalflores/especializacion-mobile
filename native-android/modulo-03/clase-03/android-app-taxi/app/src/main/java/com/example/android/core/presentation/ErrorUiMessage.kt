package com.example.android.core.presentation

import com.example.android.core.domain.DomainException

fun Throwable.toReadableMessage(): String {
    return when (this) {
        is DomainException -> this.message
        else -> this.message ?: "Unexpected error"
    }
}