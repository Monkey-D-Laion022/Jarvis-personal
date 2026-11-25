package com.jarvis.android.feature.chat

import com.jarvis.android.core.data.ChatMessage

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoaded: Boolean = false,
    val isThinking: Boolean = false,
    val errorMessage: String? = null,
    val addressAs: String = "",
)
