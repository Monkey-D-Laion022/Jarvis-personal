package com.jarvis.android.core.data

import com.jarvis.android.core.ai.ChatTurn
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(
    private val dao: ChatDao,
    private val now: () -> Long = System::currentTimeMillis,
) {
    fun observeMessages(): Flow<List<ChatMessage>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    suspend fun addUserMessage(text: String): Long =
        dao.insert(ChatMessageEntity(author = Author.USER.name, text = text, createdAt = now(), kind = null))

    suspend fun addAssistantMessage(text: String, kind: String?): Long =
        dao.insert(ChatMessageEntity(author = Author.ASSISTANT.name, text = text, createdAt = now(), kind = kind))

    suspend fun clear() = dao.clear()

    /** Últimos [limit] turnos en orden cronológico. */
    suspend fun recentTurns(limit: Int): List<ChatTurn> =
        dao.latest(limit).asReversed().map { ChatTurn(it.author == Author.USER.name, it.text) }

    private fun ChatMessageEntity.toDomain() = ChatMessage(
        id = id,
        author = runCatching { Author.valueOf(author) }.getOrDefault(Author.ASSISTANT),
        text = text,
        createdAt = createdAt,
        kind = kind,
    )
}
