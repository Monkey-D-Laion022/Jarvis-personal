package com.jarvis.android.core.settings

import android.content.Context

interface SettingsStore {
    /** Cómo quiere el usuario que lo llame (vacío = sin tratamiento). */
    var addressAs: String
}

/** Preferencias no sensibles en SharedPreferences privadas. Aquí no se guardan secretos. */
class SharedPrefsSettingsStore(context: Context) : SettingsStore {
    private val prefs =
        context.applicationContext.getSharedPreferences("jarvis_settings", Context.MODE_PRIVATE)

    override var addressAs: String
        get() = prefs.getString(KEY_ADDRESS_AS, "").orEmpty()
        set(value) {
            prefs.edit().putString(KEY_ADDRESS_AS, value).apply()
        }

    private companion object {
        const val KEY_ADDRESS_AS = "address_as"
    }
}
