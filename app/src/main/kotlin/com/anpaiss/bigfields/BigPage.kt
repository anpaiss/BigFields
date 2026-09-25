package com.anpaiss.bigfields

import android.widget.RemoteViews

/**
 * Campi pagina: un solo campo nell'editor del Karoo che occupa la pagina e
 * dentro disegna da 1 a [MAX_ROWS] dati in righe di altezza uguale. Nel Karoo 3
 * l'altezza dei campi la decide l'editor (due campi grafici = 21 e 39 righe su
 * 60, 25/09): solo così la divisione la scegliamo noi, come per la dashboard
 * di BetterEta.
 */
object Pages {
    const val COUNT = 4
    const val MAX_ROWS = 4

    fun typeId(page: Int) = "page-$page"

    /** Le pagine che Andrea usa di solito (25/09). */
    val defaults: List<List<String>> = listOf(
        listOf("distance", "ascent"),               // fatto
        listOf("dist-to-dest", "climb-left"),       // da fare
        listOf("arrival", "time-to-dest"),          // ETA nativa, confronto con BetterEta
        listOf("altitude", "grade", "temperature"), // accanto al grafico altimetrico
    )

    /** Righe valide: solo dati del catalogo, al massimo [MAX_ROWS]; vuoto = default. */
    fun sanitize(page: Int, ids: List<String>): List<String> =
        ids.filter { Catalog.byTypeId(it) != null }.take(MAX_ROWS).ifEmpty { defaults[page - 1] }
}

/** Cosa disegnare in un campo pagina: una riga per frame, cifre alte uguali. */
data class PageDrawing(
    val frames: List<FieldFrame>,
    val w: Int,
    val h: Int,
    val digitH: Float,
    val separator: Int,
)

object PageRenderer {

    private val ROW = intArrayOf(R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4)
    private val LABEL = intArrayOf(R.id.tv_label_1, R.id.tv_label_2, R.id.tv_label_3, R.id.tv_label_4)
    private val VALUE = intArrayOf(R.id.iv_value_1, R.id.iv_value_2, R.id.iv_value_3, R.id.iv_value_4)

    /** Area delle righe e altezza delle cifre: la più bassa tra le righe, così sono tutte uguali. */
    fun drawing(frames: List<FieldFrame>, cellW: Int, cellH: Int, dims: CellDims, style: FieldStyle): PageDrawing {
        val first = frames.first()
        val (w, h) = CellGeometry.pageRowArea(cellW, cellH, dims, frames.size, first.showLabel, first.bannerBand)
        val digitH = frames.minOf { NumberRenderer.maxDigitHeight(it.template, w, h, it.font) }.toInt().toFloat()
        return PageDrawing(frames, w, h, digitH, style.separator)
    }

    fun remoteViews(packageName: String, d: PageDrawing): RemoteViews {
        val first = d.frames.first()
        val views = RemoteViews(packageName, layoutFor(d.frames.size, first.showLabel, first.bannerBand))
        views.setInt(R.id.root, "setBackgroundColor", d.separator)
        d.frames.forEachIndexed { i, f ->
            val c = f.colors
            if (c.bgRes != null) views.setInt(ROW[i], "setBackgroundResource", c.bgRes)
            else views.setInt(ROW[i], "setBackgroundColor", c.fill)
            views.setTextColor(LABEL[i], c.label)
            views.setTextViewText(LABEL[i], f.label)
            views.setImageViewBitmap(VALUE[i], NumberRenderer.render(f.text, f.align, d.w, d.h, d.digitH, f.font))
            views.setInt(VALUE[i], "setColorFilter", c.value)
        }
        return views
    }

    private fun layoutFor(rows: Int, label: Boolean, band: Boolean): Int = when (rows.coerceIn(1, Pages.MAX_ROWS)) {
        1 -> pick(label, band, R.layout.view_big_page_1, R.layout.view_big_page_1_band, R.layout.view_big_page_1_bare, R.layout.view_big_page_1_bare_band)
        2 -> pick(label, band, R.layout.view_big_page_2, R.layout.view_big_page_2_band, R.layout.view_big_page_2_bare, R.layout.view_big_page_2_bare_band)
        3 -> pick(label, band, R.layout.view_big_page_3, R.layout.view_big_page_3_band, R.layout.view_big_page_3_bare, R.layout.view_big_page_3_bare_band)
        else -> pick(label, band, R.layout.view_big_page_4, R.layout.view_big_page_4_band, R.layout.view_big_page_4_bare, R.layout.view_big_page_4_bare_band)
    }

    private fun pick(label: Boolean, band: Boolean, plain: Int, withBand: Int, bare: Int, bareBand: Int) = when {
        label && band -> withBand
        label -> plain
        band -> bareBand
        else -> bare
    }
}
