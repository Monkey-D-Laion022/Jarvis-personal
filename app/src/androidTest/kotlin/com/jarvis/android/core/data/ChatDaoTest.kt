package com.jarvis.android.core.data

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ChatDaoTest {
    private lateinit var db: JarvisDatabase
    private lateinit var repository: ChatRepository
    private var clock = 1_000L

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            JarvisDatabase::class.java,
        ).allowMainThreadQueries().build()
        repository = ChatRepository(db.chatDao()) { clock++ }
    }

    @After fun tearDown() {
        db.close()
    }

    @Test fun inserta_y_lee_en_orden_cronologico() = runBlocking {
        repository.addUserMessage("hola")
        repository.addAssistantMessage("buenas", "SMALL_TALK")

        val messages = repository.observeMessages().first()

        assertEquals(listOf("hola", "buenas"), messages.map { it.text })
        assertEquals(listOf(Author.USER, Author.ASSISTANT), messages.map { it.author })
        assertEquals("SMALL_TALK", messages[1].kind)
    }

    @Test fun clear_borra_todo() = runBlocking {
        repository.addUserMessage("uno")
        repository.addUserMessage("dos")
        assertEquals(2, db.chatDao().count())

        repository.clear()

        assertEquals(0, db.chatDao().count())
        assertEquals(emptyList<ChatMessage>(), repository.observeMessages().first())
    }

    @Test fun recentTurns_devuelve_los_ultimos_en_orden() = runBlocking {
        repository.addUserMessage("a")
        repository.addAssistantMessage("b", null)
        repository.addUserMessage("c")

        val turns = repository.recentTurns(2)

        assertEquals(listOf("b", "c"), turns.map { it.text })
        assertEquals(listOf(false, true), turns.map { it.fromUser })
    }

    @Test fun autor_desconocido_cae_en_assistant() = runBlocking {
        db.chatDao().insert(ChatMessageEntity(author = "???", text = "x", createdAt = 1, kind = null))

        assertEquals(Author.ASSISTANT, repository.observeMessages().first().single().author)
    }
}
