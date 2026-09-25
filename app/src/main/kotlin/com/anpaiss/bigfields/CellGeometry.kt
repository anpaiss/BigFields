package com.anpaiss.bigfields

import kotlin.math.max
import kotlin.math.min

/** Misure fisse della cella in pixel (dimens.xml), lette una volta dalle risorse. */
data class CellDims(
    val padH: Int,
    val padTop: Int,
    val padBottom: Int,
    val labelRow: Int,
    val band: Int,
    val rowGap: Int = 0,
)

/**
 * Geometria pura del numero: quanto spazio ha e quanto può essere alto.
 *
 * Prima prova sul Karoo (25/09): con l'autosize della TextView le cifre
 * occupavano circa metà dello spazio, perché l'autosize riempie la riga di
 * testo intera (spazio sopra e sotto le lettere compreso). I nativi, nella
 * stessa cella, avevano cifre alte il 60 % in più. Qui si lavora invece sul
 * contorno vero delle cifre.
 */
object CellGeometry {

    /** Quota della larghezza e dell'altezza disponibili che le cifre possono occupare. */
    const val FILL_W = 0.96f
    const val FILL_H = 0.86f

    /** Area del numero (larghezza, altezza) in una cella [cellW]×[cellH]. */
    fun numberArea(cellW: Int, cellH: Int, d: CellDims, showLabel: Boolean, band: Boolean): Pair<Int, Int> {
        val w = cellW - 2 * d.padH
        val h = cellH - d.padTop - d.padBottom - (if (showLabel) d.labelRow else 0) - (if (band) d.band else 0)
        return max(w, 1) to max(h, 1)
    }

    /**
     * Area del numero in ogni riga di un campo pagina: [rows] righe di altezza
     * uguale separate da [CellDims.rowGap], la fascia per il banner una volta
     * sola in fondo alla cella.
     */
    fun pageRowArea(cellW: Int, cellH: Int, d: CellDims, rows: Int, showLabel: Boolean, band: Boolean): Pair<Int, Int> {
        val n = max(rows, 1)
        val usable = cellH - (if (band) d.band else 0) - (n - 1) * d.rowGap
        return numberArea(cellW, usable / n, d, showLabel, band = false)
    }

    /**
     * Altezza massima delle cifre perché il template stia nell'area.
     * [tplW]/[tplH] = contorno del template e [digitH] = altezza di una cifra,
     * tutti misurati allo stesso corpo di riferimento.
     */
    fun maxDigitHeight(tplW: Float, tplH: Float, digitH: Float, areaW: Int, areaH: Int): Float {
        if (tplW <= 0f || tplH <= 0f || digitH <= 0f) return 0f
        val scale = min(areaW * FILL_W / tplW, areaH * FILL_H / tplH)
        return digitH * scale
    }

    /**
     * Altezza con cui disegnare: la più bassa tra i campi della pagina, così
     * le cifre sono alte uguali in tutta la pagina anche con celle diverse,
     * ma mai più della propria (il template deve starci in larghezza).
     */
    fun pageDigitHeight(own: Float, onPage: Collection<Float>): Float =
        onPage.filter { it > 0f }.minOrNull()?.let { min(it, own) } ?: own
}
