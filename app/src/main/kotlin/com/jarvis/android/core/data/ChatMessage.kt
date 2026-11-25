package com.jarvis.android.core.data

enum class Author { USER, ASSISTANT }

data class ChatMessage(
    val id: Long,
    val author: Author,
    val text: String,
    val createdAt: Long,
    val kind: String?,
)
