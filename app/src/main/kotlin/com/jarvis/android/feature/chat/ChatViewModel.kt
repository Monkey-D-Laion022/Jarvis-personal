package com.jarvis.android.feature.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.jarvis.android.core.AppContainer
import com.jarvis.android.core.ai.AiEngine
import com.jarvis.android.core.ai.AiResult
import com.jarvis.android.core.command.CommandProcessor
import com.jarvis.android.core.command.Effect
import com.jarvis.android.core.command.PendingAction
import com.jarvis.android.core.command.Reply
import com.jarvis.android.core.command.ReplyKind
import com.jarvis.android.core.data.ChatRepository
import com.jarvis.android.core.persona.Persona
import com.jarvis.android.core.settings.SettingsStore
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

class ChatViewModel(
    private val repository: ChatRepository,
    private val processor: CommandProcessor,
    private val aiEngine: AiEngine,
    private val settings: SettingsStore,
) : ViewModel() {

    private val thinking = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)
    private val addressAs = MutableStateFlow(settings.addressAs)
    private var pending: PendingAction? = null

    private val messagesFlow = repository.observeMessages().catch {
        error.value = "No pude leer el historial."
        emit(emptyList())
    }

    val uiState: StateFlow<ChatUiState> = combine(messagesFlow, thinking, error, addressAs) {
            messages, isThinking, err, address ->
        ChatUiState(
            messages = messages,
            isLoaded = true,
            isThinking = isThinking,
            errorMessage = err,
            addressAs = address,
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        ChatUiState(addressAs = settings.addressAs),
    )

    fun send(text: String) {
        val input = text.trim().take(MAX_INPUT)
        if (input.isEmpty() || thinking.value) return
        thinking.value = true
        error.value = null
        viewModelScope.launch {
            try {
                repository.addUserMessage(input)
                val persona = Persona(addressAs = addressAs.value)
                var reply: Reply = processor.process(input, persona, pending)
                pending = reply.pending

                if (reply.kind == ReplyKind.UNKNOWN) {
                    val ai = withTimeoutOrNull(AI_TIMEOUT_MS) {
                        aiEngine.generate(input, repository.recentTurns(10))
                    }
                    if (ai is AiResult.Success && ai.text.isNotBlank()) {
                        reply = reply.copy(text = ai.text, kind = ReplyKind.AI)
                    }
                }

                if (reply.effect == Effect.CLEAR_HISTORY) repository.clear()
                repository.addAssistantMessage(reply.text, reply.kind.name)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                pending = null
                error.value = "Algo falló al procesar el mensaje. Probá de nuevo."
            } finally {
                thinking.value = false
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            try {
                pending = null
                repository.clear()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = "No pude borrar el historial."
            }
        }
    }

    fun setAddressAs(value: String) {
        val clean = value.trim().take(MAX_ADDRESS)
        settings.addressAs = clean
        addressAs.value = clean
    }

    fun dismissError() {
        error.value = null
    }

    private companion object {
        const val MAX_INPUT = 1000
        const val MAX_ADDRESS = 30
        const val AI_TIMEOUT_MS = 10_000L
    }
}

class ChatViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            "ViewModel desconocido: ${modelClass.name}"
        }
        return ChatViewModel(
            container.chatRepository,
            container.commandProcessor,
            container.aiEngine,
            container.settings,
        ) as T
    }
}
