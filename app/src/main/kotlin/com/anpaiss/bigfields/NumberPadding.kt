package com.anpaiss.bigfields

/** Allineamento del numero nella cella (quello scelto nell'editor profili del Karoo). */
enum class Align { LEFT, CENTER, RIGHT }

/**
 * Il numero diviso in tre pezzi: [before] e [after] vanno disegnati trasparenti.
 * Il testo intero ([full]) è largo almeno quanto il template.
 */
data class Padded(val before: String, val text: String, val after: String) {
    val full: String get() = before + text + after
}

/**
 * Il ridimensionamento automatico del testo sceglie la grandezza in base alla
 * lunghezza della stringa: senza correzione il numero cambierebbe dimensione
 * passando da 9.8 a 10.2 km/h o da 99 a 100 bpm. Per tenerlo fermo il testo
 * viene completato fino alla larghezza tipica del campo con caratteri presi dal
 * template, poi disegnati trasparenti. Un numero più lungo del template non
 * viene toccato: la cella lo rimpicciolisce, come deve.
 *
 * Il lato dei caratteri trasparenti segue l'allineamento: a destra si
 * riempie a sinistra, così le cifre visibili restano sul bordo destro.
 */
object NumberPadding {

    fun pad(text: String, template: String, align: Align): Padded {
        val missing = template.length - text.length
        if (missing <= 0) return Padded("", text, "")
        val filler = template.take(missing)
        return when (align) {
            Align.RIGHT -> Padded(filler, text, "")
            Align.LEFT -> Padded("", text, filler)
            Align.CENTER -> {
                val left = missing / 2
                Padded(filler.take(left), text, filler.drop(left))
            }
        }
    }
}
