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
    MINUTES,        // minuti interi con segno (Way Home di BetterETA: negativo = già tardi)
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
 *
 * I dati delle nostre altre estensioni (TYPE_EXT::<estensione>::<campo>): [field] è la
 * chiave da leggere nel DataPoint (null = quella attesa del catalogo o l'unica), [scale]
 * porta il valore nell'unità del [Kind] (es. minuti -> ms), e con [negativeIsMissing] un
 * valore negativo è "nessun dato" (BetterETA pubblica -1 finché non ha una stima).
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
    val field: String? = null,
    val scale: Double = 1.0,
    val negativeIsMissing: Boolean = false,
    /** Didascalia per le celle strette, dove [label] non sta (16/11: "BIKE BAT…" in una mezza cella). */
    val short: String? = null,
)
