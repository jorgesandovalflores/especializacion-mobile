package com.example.android.features.profile.domain.model

sealed interface UploadEvent {
    data class Progress(val percent: Int) : UploadEvent
    data class Done(val url: String) : UploadEvent
}
