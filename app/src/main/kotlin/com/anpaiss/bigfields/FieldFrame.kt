package com.anpaiss.bigfields

import android.content.res.Resources
import java.util.TimeZone

/** Allineamento del numero nella cella (quello scelto nell'editor profili del Karoo). */
enum class Align { LEFT, CENTER, RIGHT }

/**
 * Tutto ciò che si vede in un campo in un istante, tranne l'altezza delle
 * cifre, che dipende anche dagli altri campi della pagina. Due frame uguali
 * danno lo stesso disegno: il campo si ridisegna solo quando il frame cambia.
 * [template] è la larghezza tipica del numero: fissa il corpo del campo, così
 * il numero non cambia grandezza passando da 9.8 a 10.2.
 */
data class FieldFrame(
    val label: String,
    val text: String,
    val template: String,
    val align: Align,
    val colors: FieldColors,
    val showLabel: Boolean,
    val bannerBand: Boolean = false,
    val font: NumberFont = NumberFont.DEFAULT,
)

object FieldFrames {

    /** [zone] conta solo per i campi che hanno una [Metric.zone]. */
    fun frame(
        metric: Metric,
        raw: Double?,
        zone: Int?,
        look: Prefs.Look,
        units: Units,
        align: Align,
        nowMs: Long,
        tz: TimeZone = TimeZone.getDefault(),
    ): FieldFrame {
        val f = ValueFormatter.format(metric, raw, units, nowMs, tz)
        val label = listOf(metric.label, f.unit).filter { it.isNotEmpty() }.joinToString("  ")
        val colors = look.style.colorsFor(if (metric.zone != null && raw != null) zone else null)
        return FieldFrame(label, f.text, f.template, align, colors, look.showLabel, look.bannerRoom, look.font)
    }
}

/** Uno dei quattro layout (stessi id): didascalia sì/no, fascia per il banner sì/no. */
fun layoutFor(f: FieldFrame): Int = when {
    f.showLabel && f.bannerBand -> R.layout.view_big_field_band
    f.showLabel -> R.layout.view_big_field
    f.bannerBand -> R.layout.view_big_field_bare_band
    else -> R.layout.view_big_field_bare
}

fun cellDims(res: Resources) = CellDims(
    padH = res.getDimensionPixelSize(R.dimen.cell_pad_h),
    padTop = res.getDimensionPixelSize(R.dimen.cell_pad_top),
    padBottom = res.getDimensionPixelSize(R.dimen.cell_pad_bottom),
    labelRow = res.getDimensionPixelSize(R.dimen.label_row),
    band = res.getDimensionPixelSize(R.dimen.banner_band),
    rowGap = res.getDimensionPixelSize(R.dimen.row_gap),
)
