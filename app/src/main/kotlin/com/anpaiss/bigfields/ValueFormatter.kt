package com.anpaiss.bigfields

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs
import kotlin.math.roundToLong

/** Unità scelte dall'utente nel profilo Karoo. */
data class Units(
    val imperialDistance: Boolean = false,
    val imperialElevation: Boolean = false,
    val imperialTemperature: Boolean = false,
)

/**
 * Testo da mostrare: [text] è il numero, [unit] va nella didascalia, [template]
 * è la larghezza tipica del numero (vedi [NumberPadding]).
 */
data class Formatted(val text: String, val unit: String, val template: String)

/** Da valore grezzo del Karoo a testo. Puro: niente Android, orologio e fuso iniettati. */
object ValueFormatter {

    const val MISSING = "--"

    private const val MPS_TO_KMH = 3.6
    private const val MPS_TO_MPH = 2.2369363
    private const val M_PER_MILE = 1609.344
    private const val FT_PER_M   = 3.2808399

    // Sotto 0,1 mi (~160 m) una distanza breve si dice in piedi
    private const val SHORT_LIMIT_MI_M = 160.9344

    /**
     * [raw] null = dato assente (sensore spento, nessuna route): numero "--" ma
     * unità e larghezza tipica restano quelle del campo, così la cella non salta.
     * [nowMs] serve a [Kind.CLOCK] e [Kind.ARRIVAL].
     */
    fun format(
        metric: Metric,
        raw: Double?,
        units: Units,
        nowMs: Long,
        zone: TimeZone = TimeZone.getDefault(),
    ): Formatted {
        val kind = metric.kind
        val unit = unitFor(metric, raw, units)
        val template = metric.template ?: templateFor(kind)
        if (raw == null && kind != Kind.CLOCK) return Formatted(MISSING, unit, template)
        val v = raw ?: 0.0
        val text = when (kind) {
            Kind.SPEED -> oneDecimal(v * if (units.imperialDistance) MPS_TO_MPH else MPS_TO_KMH)
            Kind.DISTANCE -> oneDecimal(v / if (units.imperialDistance) M_PER_MILE else 1000.0)
            Kind.SHORT_DISTANCE -> shortDistance(v, units)
            Kind.ELEVATION -> whole(if (units.imperialElevation) v * FT_PER_M else v)
            Kind.GRADE, Kind.COUNT, Kind.PERCENT -> whole(v)
            Kind.TEMPERATURE -> whole(if (units.imperialTemperature) v * 9.0 / 5.0 + 32.0 else v)
            Kind.DURATION -> hms(v)
            Kind.DURATION_HM -> hm(v)
            Kind.ARRIVAL -> clock(nowMs + v.roundToLong(), zone)
            Kind.CLOCK -> clock(nowMs, zone)
        }
        return Formatted(text, unit, template)
    }

    fun unitFor(metric: Metric, raw: Double?, units: Units): String = when (metric.kind) {
        Kind.SPEED -> if (units.imperialDistance) "MPH" else "KM/H"
        Kind.DISTANCE -> if (units.imperialDistance) "MI" else "KM"
        Kind.SHORT_DISTANCE -> when {
            units.imperialDistance -> if (raw != null && raw < SHORT_LIMIT_MI_M) "FT" else "MI"
            else -> if (raw != null && raw < 1000.0) "M" else "KM"
        }
        Kind.ELEVATION -> if (units.imperialElevation) "FT" else "M"
        Kind.GRADE, Kind.PERCENT -> "%"
        Kind.COUNT -> metric.unitLabel
        Kind.TEMPERATURE -> if (units.imperialTemperature) "°F" else "°C"
        Kind.DURATION, Kind.DURATION_HM, Kind.ARRIVAL, Kind.CLOCK -> ""
    }

    fun templateFor(kind: Kind): String = when (kind) {
        Kind.SPEED, Kind.DISTANCE -> "88.8"
        Kind.SHORT_DISTANCE -> "888"
        Kind.ELEVATION -> "8888"
        Kind.GRADE -> "-18"
        Kind.COUNT -> "888"
        Kind.PERCENT -> "88"
        Kind.TEMPERATURE -> "-8"
        Kind.DURATION -> "8:88:88"
        Kind.DURATION_HM -> "8:88"
        Kind.ARRIVAL, Kind.CLOCK -> "88:88"
    }

    private fun shortDistance(m: Double, units: Units): String = when {
        units.imperialDistance && m < SHORT_LIMIT_MI_M -> whole(m * FT_PER_M)
        units.imperialDistance -> oneDecimal(m / M_PER_MILE)
        m < 1000.0 -> whole(m)
        else -> oneDecimal(m / 1000.0)
    }

    /** Punto decimale fisso: la cifra si legge uguale con qualsiasi lingua del sistema. */
    private fun oneDecimal(v: Double): String {
        val tenths = (abs(v) * 10.0).roundToLong()
        val sign = if (v < 0 && tenths > 0) "-" else ""
        return "$sign${tenths / 10}.${tenths % 10}"
    }

    private fun whole(v: Double): String = v.roundToLong().let { if (it == 0L) "0" else it.toString() }

    private fun hms(ms: Double): String {
        val s = (ms / 1000.0).toLong().coerceAtLeast(0)
        val h = s / 3600
        val m = (s % 3600) / 60
        val sec = s % 60
        return if (h > 0) String.format(Locale.US, "%d:%02d:%02d", h, m, sec)
        else String.format(Locale.US, "%d:%02d", m, sec)
    }

    private fun hm(ms: Double): String {
        val min = (ms / 60_000.0).roundToLong().coerceAtLeast(0)
        return String.format(Locale.US, "%d:%02d", min / 60, min % 60)
    }

    private fun clock(epochMs: Long, zone: TimeZone): String {
        val c = Calendar.getInstance(zone).apply { timeInMillis = epochMs }
        return String.format(Locale.US, "%d:%02d", c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE))
    }
}
