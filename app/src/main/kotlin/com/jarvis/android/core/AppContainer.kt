package com.jarvis.android.core

import android.content.Context
import com.jarvis.android.core.ai.AiEngine
import com.jarvis.android.core.ai.UnavailableAiEngine
import com.jarvis.android.core.command.CommandProcessor
import com.jarvis.android.core.data.ChatRepository
import com.jarvis.android.core.data.JarvisDatabase
import com.jarvis.android.core.settings.SettingsStore
import com.jarvis.android.core.settings.SharedPrefsSettingsStore

/** Inyección manual sencilla (sin Hilt, a propósito). */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database: JarvisDatabase by lazy { JarvisDatabase.create(appContext) }

    val chatRepository: ChatRepository by lazy { ChatRepository(database.chatDao()) }
    val commandProcessor: CommandProcessor = CommandProcessor()
    val aiEngine: AiEngine = UnavailableAiEngine
    val settings: SettingsStore by lazy { SharedPrefsSettingsStore(appContext) }
}
