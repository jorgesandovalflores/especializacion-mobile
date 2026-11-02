package com.example.android.features.signup.domain.usecase

import com.example.android.features.signup.domain.model.SignUpDraft

internal const val NAME_MIN_LENGTH = 2
const val NAME_MAX_LENGTH = 60
const val EMAIL_MAX_LENGTH = 120

private val EMAIL_REGEX = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]{2,}$")

fun SignUpDraft.hasValidNames(): Boolean =
    givenName.trim().length in NAME_MIN_LENGTH..NAME_MAX_LENGTH &&
        familyName.trim().length in NAME_MIN_LENGTH..NAME_MAX_LENGTH

fun SignUpDraft.hasValidEmail(): Boolean =
    email.length <= EMAIL_MAX_LENGTH && EMAIL_REGEX.matches(email.trim())
