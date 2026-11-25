package com.jarvis.android.core.persona

/**
 * Personalidad original y configurable (no copia diálogos de ninguna película).
 * [addressAs] es cómo quiere el usuario que lo llame; vacío = sin tratamiento.
 */
data class Persona(
    val name: String = "JARVIS",
    val addressAs: String = "",
) {
    /** Sufijo para frases: ", señor" o "" si no hay tratamiento configurado. */
    fun vocative(): String = if (addressAs.isBlank()) "" else ", ${addressAs.trim()}"
}
