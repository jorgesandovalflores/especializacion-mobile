package com.example.android.features.signin.data.remote.dto

data class AuthOtpValidateRequest(
    val phone: String,
    val code: String
)