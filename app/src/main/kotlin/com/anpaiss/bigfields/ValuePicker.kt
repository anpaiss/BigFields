package com.anpaiss.bigfields

import io.hammerhead.karooext.models.DataType.Field

/**
 * Quale valore leggere da un DataPoint del Karoo.
 *
 * Primo giro con BigFields (26/09): "distanza a destinazione" e "salita
 * rimanente" a zero per tutto il giro. `DataPoint.singleValue` dell'SDK è solo
 * il PRIMO valore della mappa, e per quei dati il Karoo manda più valori
 * insieme (con la distanza anche ON_ROUTE = 0/1, con la salita anche la
 * discesa rimanente). BetterEta li legge per nome da sempre
 * (Field.DISTANCE_TO_DESTINATION, Field.ASCENT_REMAINING).
 */
object ValuePicker {

    /** Campo atteso per ogni dato del catalogo; assente = dato a valore unico. */
    val fieldOf: Map<String, String> = mapOf(
        "speed" to Field.SPEED,
        "speed-3s" to Field.SMOOTHED_3S_AVERAGE_SPEED,
        "avg-speed" to Field.AVERAGE_SPEED,
        "max-speed" to Field.MAX_SPEED,
        "hr" to Field.HEART_RATE,
        "avg-hr" to Field.AVG_HR,
        "power" to Field.POWER,
        "power-3s" to Field.SMOOTHED_3S_AVERAGE_POWER,
        "avg-power" to Field.AVERAGE_POWER,
        "np" to Field.NORMALIZED_POWER,
        "cadence" to Field.CADENCE,
        "distance" to Field.DISTANCE,
        "ride-time" to Field.RIDE_TIME,
        "elapsed-time" to Field.ELAPSED_TIME,
        "grade" to Field.ELEVATION_GRADE,
        "altitude" to Field.PRESSURE_ELEVATION,
        "ascent" to Field.ELEVATION_GAIN,
        "dist-to-dest" to Field.DISTANCE_TO_DESTINATION,
        "time-to-dest" to Field.TIME_TO_DESTINATION,
        "arrival" to Field.TIME_TO_DESTINATION,
        "climb-left" to Field.ASCENT_REMAINING,
        "next-turn" to Field.DISTANCE_TO_NEXT_TURN,
        "dist-to-top" to Field.DISTANCE_TO_TOP,
        "elev-to-top" to Field.ELEVATION_TO_TOP,
        "temperature" to Field.TEMPERATURE,
        "battery" to Field.BATTERY_PERCENT,
        "calories" to Field.CALORIES,
    )

    /** Campo atteso per le zone che colorano i campi. */
    val zoneFieldOf: Map<ZoneSource, String> = mapOf(
        ZoneSource.HR to Field.HR_ZONE,
        ZoneSource.POWER to Field.POWER_ZONE,
    )

    /**
     * Il valore di [expected] se c'è; altrimenti, con un solo valore nella
     * mappa, quello; con più valori e nessuno atteso, null: meglio "--" che un
     * numero sbagliato mostrato come buono.
     */
    fun pick(values: Map<String, Double>, expected: String?): Double? {
        if (expected != null) values[expected]?.let { return it }
        return values.values.singleOrNull() ?: values[Field.SINGLE]
    }
}
