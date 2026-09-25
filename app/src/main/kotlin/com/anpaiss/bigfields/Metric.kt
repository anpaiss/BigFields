package com.anpaiss.bigfields

import io.hammerhead.karooext.models.DataType

/** Come si legge e si scrive un valore grezzo del Karoo. */
enum class Kind {
    SPEED,          // m/s
    DISTANCE,       // m, mostrato in km/mi con un decimale
    SHORT_DISTANCE, // m, in metri sotto 1 km (svolta, cima della salita)
    ELEVATION,      // m
    GRADE,          // %
    COUNT,          // numero intero con unità fissa (bpm, W, rpm, kcal)
    PERCENT,        // %
    TEMPERATURE,    // °C
    DURATION,       // ms, h:mm:ss
    DURATION_HM,    // ms, h:mm
    ARRIVAL,        // ms che mancano all'arrivo, mostrati come ora locale h:mm
    CLOCK,          // ora locale, nessun dato Karoo
}

/** Zona che colora il campo negli stili che hanno colori di stato. */
enum class ZoneSource(val karooType: String) {
    HR(DataType.Type.HR_ZONE),
    POWER(DataType.Type.POWER_ZONE),
}

/**
 * Un campo di BigFields: [typeId] è l'id nell'estensione (extension_info.xml),
 * [karooType] il dato nativo da cui legge (null = nessuno, vedi [Kind.CLOCK]).
 * [unitLabel] vale solo per [Kind.COUNT]; [sample] è il valore grezzo mostrato
 * nell'anteprima dell'editor profili; [template] sostituisce la larghezza tipica
 * del [Kind] (vedi [NumberPadding]).
 */
data class Metric(
    val typeId: String,
    val karooType: String?,
    val label: String,
    val kind: Kind,
    val sample: Double,
    val unitLabel: String = "",
    val template: String? = null,
    val zone: ZoneSource? = null,
)
