package com.anpaiss.bigfields

import io.hammerhead.karooext.models.DataType.Field
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
        // Our other extensions (12/11/2026): what they publish as a number, read with the right key and unit.
        // BetterETA: minutes for arrival and time to go, seconds for Way Home and the next point, -1 = no estimate yet.
        // Way Home's minutes from BetterETA's "minutes left" stream: the Karoo passes another extension only the
        // main value of a stream, so the "allowance" key of eta-way-home never arrived (16/11/2026)
        Metric("be-way-home", "TYPE_EXT::better-eta::eta-way-home-left", "WAY HOME", Kind.MINUTES, 38.0, "", field = Field.SINGLE, scale = 1.0 / 60.0, negativeIsMissing = false),
        Metric("be-home-at", "TYPE_EXT::better-eta::eta-way-home", "HOME AT", Kind.ARRIVAL, 2880000.0, "", field = Field.SINGLE, scale = 1000.0, negativeIsMissing = true),
        Metric("be-arrival", "TYPE_EXT::better-eta::eta-absolute", "BETTER ETA", Kind.ARRIVAL, 3480000.0, "", field = Field.SINGLE, scale = 60_000.0, negativeIsMissing = true),
        Metric("be-to-go", "TYPE_EXT::better-eta::eta-remaining", "BETTER TO GO", Kind.DURATION_HM, 3480000.0, "", field = Field.SINGLE, scale = 60_000.0, negativeIsMissing = true),
        Metric("be-next-poi", "TYPE_EXT::better-eta::eta-next-poi", "NEXT POINT", Kind.ARRIVAL, 1500000.0, "", field = Field.SINGLE, scale = 1000.0, negativeIsMissing = true),
        Metric("bosch-battery", "TYPE_EXT::bosch-data::bike_battery", "BIKE BATTERY", Kind.PERCENT, 64.0, "", field = null, scale = 1.0, negativeIsMissing = false),
        Metric("bestie-battery", "TYPE_EXT::bosch-data::board_battery", "BESTIE BATT", Kind.PERCENT, 87.0, "", field = null, scale = 1.0, negativeIsMissing = false),
        Metric("bosch-power", "TYPE_EXT::bosch-data::bike_power", "RIDER POWER", Kind.COUNT, 180.0, "W", field = null, scale = 1.0, negativeIsMissing = false),
        Metric("range-arrival", "TYPE_EXT::bosch-range::compact", "BATT AT END", Kind.PERCENT, 23.0, "", field = null, scale = 1.0, negativeIsMissing = false),
        // RouteProfile (14/11/2026): the climbs ahead, metres and %, -1 = no climb ahead.
        Metric("rp-to-climb", "TYPE_EXT::routeprofile::to-climb", "TO CLIMB", Kind.SHORT_DISTANCE, 1200.0, "", field = null, scale = 1.0, negativeIsMissing = true),
        Metric("rp-to-top", "TYPE_EXT::routeprofile::to-top", "TO TOP", Kind.SHORT_DISTANCE, 3700.0, "", field = null, scale = 1.0, negativeIsMissing = true),
        Metric("rp-up-to-top", "TYPE_EXT::routeprofile::up-to-top", "UP TO TOP", Kind.ELEVATION, 245.0, "", field = null, scale = 1.0, negativeIsMissing = true),
        Metric("rp-grade-to-top", "TYPE_EXT::routeprofile::grade-to-top", "TOP GRADE", Kind.GRADE, 8.0, "", field = null, scale = 1.0, negativeIsMissing = true),
        // BetterETA: the next climb's top, seconds (its own model; never the Karoo's times), -1 = no climb ahead.
        Metric("be-top-at", "TYPE_EXT::better-eta::eta-top-at", "AT THE TOP", Kind.ARRIVAL, 1440000.0, "", field = Field.SINGLE, scale = 1000.0, negativeIsMissing = true),
        Metric("be-time-to-top", "TYPE_EXT::better-eta::eta-time-to-top", "TIME TO TOP", Kind.DURATION_HM, 1440000.0, "", field = Field.SINGLE, scale = 1000.0, negativeIsMissing = true),
    )

    fun byTypeId(typeId: String): Metric? = all.firstOrNull { it.typeId == typeId }
}
