package com.jarvis.android.core.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val author: String,
    val text: String,
    val createdAt: Long,
    val kind: String?,
)
