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

    var font: NumberFont
        get() = sp.getString(KEY_FONT, null)
            ?.let { runCatching { NumberFont.valueOf(it) }.getOrNull() }
            ?: NumberFont.DEFAULT
        set(v) = sp.edit().putString(KEY_FONT, v.name).apply()

    var showLabel: Boolean
        get() = sp.getBoolean(KEY_SHOW_LABEL, true)
        set(v) = sp.edit().putBoolean(KEY_SHOW_LABEL, v).apply()

    /**
     * Fascia libera in fondo a OGNI campo mentre si naviga, alta quanto il
     * banner "next cue" che la navigazione disegna in fondo allo schermo. In tutti i campi e non solo
     * in quello in basso, perché i numeri della pagina restino della stessa
     * grandezza (richiesta di Andrea 25/09). Accesa di default: un numero
     * coperto è peggio di un numero un po' più piccolo.
     */
    var bannerRoom: Boolean
        get() = sp.getBoolean(KEY_BANNER_ROOM, true)
        set(v) = sp.edit().putBoolean(KEY_BANNER_ROOM, v).apply()

    /**
     * Solo per le prove sul Karoo senza registrare: al posto di un dato
     * assente il campo mostra il valore d'esempio del catalogo.
     */
    var sampleValues: Boolean
        get() = sp.getBoolean(KEY_SAMPLE_VALUES, false)
        set(v) = sp.edit().putBoolean(KEY_SAMPLE_VALUES, v).apply()

    /** Dati delle righe del campo pagina [page] (1..[Pages.COUNT]), in ordine. */
    fun pageRows(page: Int): List<String> =
        Pages.sanitize(page, sp.getString(pageKey(page), null)?.split(',').orEmpty())

    fun setPageRows(page: Int, ids: List<String>) =
        sp.edit().putString(pageKey(page), Pages.sanitize(page, ids).joinToString(",")).apply()

    fun pageRowsFlow(page: Int): Flow<List<String>> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == pageKey(page)) trySend(pageRows(page))
        }
        sp.registerOnSharedPreferenceChangeListener(listener)
        send(pageRows(page))
        awaitClose { sp.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    private fun pageKey(page: Int) = "page_${page}_rows"

    /** Stile, didascalia e fascia come Flow: i campi si ridisegnano appena cambiano nell'app. */
    fun lookFlow(): Flow<Look> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key in LOOK_KEYS) trySend(look())
        }
        sp.registerOnSharedPreferenceChangeListener(listener)
        send(look())
        awaitClose { sp.unregisterOnSharedPreferenceChangeListener(listener) }
    }

    fun look() = Look(style, showLabel, bannerRoom, sampleValues, font)

    data class Look(
        val style: FieldStyle,
        val showLabel: Boolean,
        val bannerRoom: Boolean = true,
        val sampleValues: Boolean = false,
        val font: NumberFont = NumberFont.DEFAULT,
    )

    companion object {
        private const val NAME = "big_fields"
        private const val KEY_STYLE = "style"
        private const val KEY_SHOW_LABEL = "show_label"
        private const val KEY_BANNER_ROOM = "banner_room"
        private const val KEY_SAMPLE_VALUES = "sample_values"
        private const val KEY_FONT = "font"
        private val LOOK_KEYS = setOf(KEY_STYLE, KEY_SHOW_LABEL, KEY_BANNER_ROOM, KEY_SAMPLE_VALUES, KEY_FONT)
    }
}
