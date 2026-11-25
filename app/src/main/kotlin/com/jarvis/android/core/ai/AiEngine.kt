package com.jarvis.android.core.ai

data class ChatTurn(val fromUser: Boolean, val text: String)

sealed interface AiResult {
    data class Success(val text: String) : AiResult
    data class Unavailable(val reason: String) : AiResult
    data class Failure(val message: String) : AiResult
}

/**
 * Punto de extensión para un motor de IA (modelo local o proveedor permitido en el futuro).
 * Ningún proveedor está acoplado al resto del proyecto.
 */
interface AiEngine {
    val id: String
    suspend fun generate(prompt: String, history: List<ChatTurn>): AiResult
}

/** Fase 1: no hay motor de IA. Siempre declara que no está disponible. */
object UnavailableAiEngine : AiEngine {
    override val id: String = "none"
    override suspend fun generate(prompt: String, history: List<ChatTurn>): AiResult =
        AiResult.Unavailable("Fase 1: sin motor de IA configurado")
}
