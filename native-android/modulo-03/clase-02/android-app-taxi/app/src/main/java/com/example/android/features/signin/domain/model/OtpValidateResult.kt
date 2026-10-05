package com.example.android.features.signin.domain.model

import com.example.android.commons.domain.model.Passenger

data class OtpValidateResult(
    val tokens: SessionTokens,
    val user: Passenger
)
