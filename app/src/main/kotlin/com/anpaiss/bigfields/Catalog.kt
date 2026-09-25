package com.anpaiss.bigfields

import io.hammerhead.karooext.models.DataType.Type

/**
 * Tutti i campi offerti. Ogni voce deve avere il suo <DataType> in
 * res/xml/extension_info.xml e il nome in strings.xml (CatalogTest lo verifica).
 *
 * Unità dei valori grezzi: velocità in m/s e distanze in m verificate con
 * BetterEta; TIME_TO_DESTINATION in ms verificato il 30/08. Le altre sono da
 * confermare sul Karoo: il log registra il primo valore grezzo di ogni campo
 * (riga "DATA").
 *
 * I valori d'esempio di distanze e dislivelli sono al massimo realistico
 * (999 km, 1000 m): l'anteprima mostra il caso che riempie di più la riga.
 */
object Catalog {

    val all: List<Metric> = listOf(
        Metric("speed",         Type.SPEED,                         "SPEED",        Kind.SPEED,             7.5),
        Metric("speed-3s",      Type.SMOOTHED_3S_AVERAGE_SPEED,     "SPEED 3S",     Kind.SPEED,             7.5),
        Metric("avg-speed",     Type.AVERAGE_SPEED,                 "AVG SPEED",    Kind.SPEED,             6.1),
        Metric("max-speed",     Type.MAX_SPEED,                     "MAX SPEED",    Kind.SPEED,            15.3),
        Metric("hr",            Type.HEART_RATE,                    "HEART RATE",   Kind.COUNT,           142.0, "BPM",  "188",  ZoneSource.HR),
        Metric("avg-hr",        Type.AVERAGE_HR,                    "AVG HR",       Kind.COUNT,           131.0, "BPM",  "188"),
        Metric("power",         Type.POWER,                         "POWER",        Kind.COUNT,           215.0, "W",    "888",  ZoneSource.POWER),
        Metric("power-3s",      Type.SMOOTHED_3S_AVERAGE_POWER,     "POWER 3S",     Kind.COUNT,           210.0, "W",    "888",  ZoneSource.POWER),
        Metric("avg-power",     Type.AVERAGE_POWER,                 "AVG POWER",    Kind.COUNT,           176.0, "W",    "888"),
        Metric("np",            Type.NORMALIZED_POWER,              "NORM POWER",   Kind.COUNT,           192.0, "W",    "888"),
        Metric("cadence",       Type.CADENCE,                       "CADENCE",      Kind.COUNT,            84.0, "RPM",  "188"),
        Metric("distance",      Type.DISTANCE,                      "DISTANCE",     Kind.DISTANCE,     999000.0),
        Metric("ride-time",     Type.RIDE_TIME,                     "RIDE TIME",    Kind.DURATION,    5025000.0),
        Metric("elapsed-time",  Type.ELAPSED_TIME,                  "ELAPSED",      Kind.DURATION,    5710000.0),
        Metric("clock",         null,                               "TIME",         Kind.CLOCK,             0.0),
        Metric("grade",         Type.ELEVATION_GRADE,               "GRADE",        Kind.GRADE,             7.4),
        Metric("altitude",      Type.PRESSURE_ELEVATION_CORRECTION, "ALTITUDE",     Kind.ELEVATION,       812.0),
        Metric("ascent",        Type.ELEVATION_GAIN,                "ASCENT",       Kind.ELEVATION,      1000.0),
        Metric("dist-to-dest",  Type.DISTANCE_TO_DESTINATION,       "TO DEST",      Kind.DISTANCE,     999000.0),
        Metric("time-to-dest",  Type.TIME_TO_DESTINATION,           "TIME TO DEST", Kind.DURATION_HM, 3480000.0),
        Metric("arrival",       Type.TIME_TO_DESTINATION,           "ARRIVAL",      Kind.ARRIVAL,     3480000.0),
        Metric("climb-left",    Type.ELEVATION_REMAINING,           "CLIMB LEFT",   Kind.ELEVATION,      1000.0),
        Metric("next-turn",     Type.DISTANCE_TO_NEXT_TURN,         "NEXT TURN",    Kind.SHORT_DISTANCE,  350.0),
        Metric("dist-to-top",   Type.DISTANCE_TO_TOP,               "TO TOP",       Kind.SHORT_DISTANCE, 2300.0),
        Metric("elev-to-top",   Type.ELEVATION_TO_TOP,              "UP TO TOP",    Kind.ELEVATION,       185.0),
        Metric("lap-time",      Type.ELAPSED_TIME_LAP,              "LAP TIME",     Kind.DURATION,     754000.0),
        Metric("lap-distance",  Type.DISTANCE_LAP,                  "LAP DIST",     Kind.DISTANCE,       5000.0),
        Metric("lap-avg-speed", Type.AVERAGE_SPEED_LAP,             "LAP SPEED",    Kind.SPEED,             6.6),
        Metric("lap-power",     Type.POWER_LAP,                     "LAP POWER",    Kind.COUNT,           188.0, "W",    "888"),
        Metric("temperature",   Type.TEMPERATURE,                   "TEMP",         Kind.TEMPERATURE,      21.0),
        Metric("battery",       Type.BATTERY_PERCENT,               "BATTERY",      Kind.PERCENT,          64.0),
        Metric("calories",      Type.CALORIES,                      "CALORIES",     Kind.COUNT,           512.0, "KCAL", "8888"),
    )

    fun byTypeId(typeId: String): Metric? = all.firstOrNull { it.typeId == typeId }
}
