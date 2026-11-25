package com.jarvis.android.core.command

enum class ReplyKind { CALCULATION, SMALL_TALK, INFO, LIMITATION, CONFIRMATION, AI, UNKNOWN }

/** Acción que quedó esperando una confirmación explícita del usuario. */
enum class PendingAction { CONFIRM_CLEAR_HISTORY }

/** Efecto que la capa superior debe ejecutar (el procesador no toca almacenamiento). */
enum class Effect { CLEAR_HISTORY }

data class Reply(
    val text: String,
    val kind: ReplyKind,
    val pending: PendingAction? = null,
    val effect: Effect? = null,
)
