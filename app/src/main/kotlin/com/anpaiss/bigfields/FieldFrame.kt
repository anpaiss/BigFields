package com.anpaiss.bigfields

import java.util.TimeZone

/**
 * Tutto ciò che si vede in un campo in un istante. Due frame uguali danno lo
 * stesso disegno: il campo si ridisegna solo quando il frame cambia.
 */
data class FieldFrame(
    val label: String,
    val number: Padded,
    val colors: FieldColors,
    val showLabel: Boolean,
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
        return FieldFrame(label, NumberPadding.pad(f.text, f.template, align), colors, look.showLabel)
    }
}
