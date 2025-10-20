package com.example.android.core.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiError(
    @SerialName("status_code") val statusCode: Int? = null,
    @SerialName("message") val message: String? = null
)