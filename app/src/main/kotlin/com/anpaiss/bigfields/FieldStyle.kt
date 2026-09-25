package com.anpaiss.bigfields

/**
 * Carattere dei numeri. Il numero è disegnato da noi come immagine, quindi può
 * usare un carattere incluso nell'app. Prima prova 25/09: in una riga di
 * mezza pagina il limite è la larghezza, e le cifre del carattere del Karoo
 * (IBM Plex; "sans-serif-condensed" è solo un alias del normale) sono larghe
 * quasi quanto alte. Barlow Condensed (licenza OFL) dà cifre ~36 % più alte.
 */
enum class NumberFont(val label: String) {
    CONDENSED("Condensed (Barlow)"),
    KAROO("Karoo"),
    ;

    companion object {
        val DEFAULT = CONDENSED
    }
}

/**
 * Colori risolti per un campo. [fill] è il colore di fondo effettivo (serve al
 * controllo del contrasto); [bgRes] è il drawable con il bordo, quando c'è.
 */
data class FieldColors(
    val fill: Int,
    val bgRes: Int?,
    val label: Int,
    val value: Int,
)

/**
 * Preset di stile, scelto nell'app. Stessi nomi e colori di AxsRatio
 * (TileStyle.kt) e BetterEta (EtaStyle.kt): ogni abbinamento testo/fondo ha
 * contrasto WCAG >= 4.5:1 (FieldStyleTest lo verifica). La zona di frequenza
 * cardiaca o potenza parla nel canale che lo stile usa in AxsRatio: VIVID e
 * PASTEL col fondo, OUTLINE col bordo, INK col colore del numero. I due stili
 * Karoo sono monocromi come i campi nativi.
 */
enum class FieldStyle(val label: String) {
    KAROO_DARK("Karoo dark"),
    KAROO_LIGHT("Karoo light"),
    VIVID("Vivid"),
    OUTLINE("Outline"),
    PASTEL("Pastel"),
    INK("Ink"),
    ;

    /** Colore delle linee tra le righe di un campo pagina: scuro, tranne sul fondo chiaro. */
    val separator: Int get() = if (this == KAROO_LIGHT) 0xFF9E9E9E.toInt() else BLACK

    /** [zone] 1..5 oppure null (campo senza zona o zona non ancora nota). */
    fun colorsFor(zone: Int?): FieldColors {
        val z = zone?.coerceIn(1, 5)
        return when (this) {
            KAROO_DARK -> FieldColors(BLACK, null, GREY, WHITE)           // 21:1, didascalia 7.8:1
            KAROO_LIGHT -> FieldColors(WHITE, null, DARK_GREY, BLACK)     // 21:1, didascalia 6.2:1

            VIVID -> when (z) {
                1    -> solid(0xFF9E9E9E.toInt(), BLACK)  // 7.8:1
                2    -> solid(0xFF1976D2.toInt(), WHITE)  // 4.6:1
                3    -> solid(0xFF4CAF50.toInt(), BLACK)  // 7.6:1
                4    -> solid(0xFFFF9800.toInt(), BLACK)  // 9.7:1
                5    -> solid(0xFFD32F2F.toInt(), WHITE)  // 5.0:1
                else -> solid(ACCENT, BLACK)              // 6.7:1
            }

            // Testo bianco su quasi-nero (19:1); la zona colora il bordo.
            OUTLINE -> FieldColors(OUTLINE_FILL, outlineRes(z), GREY, WHITE)

            PASTEL -> when (z) {
                1    -> solid(0xFFE0E0E0.toInt(), BLACK)  // 13.9:1
                2    -> solid(0xFF90CAF9.toInt(), BLACK)  // 12.0:1
                3    -> solid(0xFFA5D6A7.toInt(), BLACK)  // 12.8:1
                4    -> solid(0xFFFFCC80.toInt(), BLACK)  // 14.2:1
                5    -> solid(0xFFEF9A9A.toInt(), BLACK)  //  9.8:1
                else -> solid(0xFFFFAB91.toInt(), BLACK)  // 11.5:1
            }

            // Numero nel colore della zona su nero (min 5.3:1); bordo sottile.
            INK -> FieldColors(INK_FILL, R.drawable.bg_ink, GREY, zoneAccent(z))
        }
    }

    companion object {
        val DEFAULT = KAROO_DARK

        const val ACCENT = 0xFFFF5823.toInt()
        const val BLACK = 0xFF000000.toInt()
        const val WHITE = 0xFFFFFFFF.toInt()
        private const val GREY = 0xFF9E9E9E.toInt()
        private const val DARK_GREY = 0xFF616161.toInt()
        const val OUTLINE_FILL = 0xFF101010.toInt()   // come bg_outline_*.xml
        const val INK_FILL = 0xFF0A0A0A.toInt()       // come bg_ink.xml

        private fun solid(bg: Int, fg: Int) = FieldColors(bg, null, fg, fg)

        /** Colori pieni delle zone su fondo nero, come zoneAccent di AxsRatio. */
        fun zoneAccent(zone: Int?): Int = when (zone) {
            1    -> 0xFF9E9E9E.toInt()  // 7.8:1
            2    -> 0xFF2196F3.toInt()  // 6.6:1
            3    -> 0xFF4CAF50.toInt()  // 7.6:1
            4    -> 0xFFFF9800.toInt()  // 9.7:1
            5    -> 0xFFF44336.toInt()  // 5.3:1
            else -> ACCENT              // 6.7:1
        }

        private fun outlineRes(zone: Int?): Int = when (zone) {
            1    -> R.drawable.bg_outline_z1
            2    -> R.drawable.bg_outline_z2
            3    -> R.drawable.bg_outline_z3
            4    -> R.drawable.bg_outline_z4
            5    -> R.drawable.bg_outline_z5
            else -> R.drawable.bg_outline_accent
        }
    }
}
