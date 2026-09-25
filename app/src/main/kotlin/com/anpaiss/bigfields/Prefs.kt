package com.anpaiss.bigfields

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

/** Preferenze utente. Stesso pattern di Prefs.kt in AxsRatio e BetterEta. */
class Prefs(ctx: Context) {

    private val sp: SharedPreferences = ctx.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var style: FieldStyle
        get() = sp.getString(KEY_STYLE, null)
            ?.let { runCatching { FieldStyle.valueOf(it) }.getOrNull() }
            ?: FieldStyle.DEFAULT
        set(v) = sp.edit().putString(KEY_STYLE, v.name).apply()

    var showLabel: Boolean
        get() = sp.getBoolean(KEY_SHOW_LABEL, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_LABEL, v).apply()

    /** Stile e didascalia come Flow: i campi si ridisegnano appena cambiano nell'app. */
    fun lookFlow(): Flow<Look> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_STYLE || key == KEY_SHOW_LABEL) trySend(Look(style, showLabel))
        }
        sp.registerOnSharedPreferenceChangeListener(listener)
        send(Look(style, showLabel))
        awaitClose { sp.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    data class Look(val style: FieldStyle, val showLabel: Boolean)

    companion object {
        private const val NAME = "big_fields"
        private const val KEY_STYLE = "style"
        private const val KEY_SHOW_LABEL = "show_label"
    }
}
