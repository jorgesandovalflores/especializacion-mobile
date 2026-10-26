package com.example.android.features.home.data.push

import com.example.android.features.home.domain.model.PushMessage

const val DEEPLINK_HOME = "app-taxi://passenger/home"

fun Map<String, String>.toPushMessage(): PushMessage? {
    val title = this["title"]?.takeIf { it.isNotBlank() } ?: return null
    val body = this["body"]?.takeIf { it.isNotBlank() } ?: return null
    return PushMessage(title, body, this["deeplink"]?.takeIf { it.isNotBlank() } ?: DEEPLINK_HOME)
}
