package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.TimeZone

class ValueFormatterTest {

    private val metric = Units()
    private val imperial = Units(imperialDistance = true, imperialElevation = true, imperialTemperature = true)
    private val utc = TimeZone.getTimeZone("UTC")

    private fun fmt(typeId: String, raw: Double?, units: Units = metric, nowMs: Long = 0L) =
        ValueFormatter.format(Catalog.byTypeId(typeId)!!, raw, units, nowMs, utc)

    @Test fun `speed in km per hour with one decimal`() {
        assertEquals(Formatted("27.0", "KM/H", "88.8"), fmt("speed", 7.5))
        assertEquals("0.0", fmt("speed", 0.0).text)
    }

    @Test fun `speed in miles per hour`() {
        assertEquals(Formatted("16.8", "MPH", "88.8"), fmt("speed", 7.5, imperial))
    }

    @Test fun `missing value keeps unit and width`() {
        assertEquals(Formatted("--", "KM/H", "88.8"), fmt("speed", null))
        assertEquals(Formatted("--", "BPM", "188"), fmt("hr", null))
    }

    @Test fun `counts are whole numbers with their unit`() {
        assertEquals(Formatted("142", "BPM", "188"), fmt("hr", 141.6))
        assertEquals(Formatted("215", "W", "888"), fmt("power", 215.2))
        assertEquals(Formatted("84", "RPM", "188"), fmt("cadence", 84.0))
    }

    @Test fun `distance in km and miles`() {
        assertEquals(Formatted("23.5", "KM", "88.8"), fmt("distance", 23450.0))
        assertEquals("14.6", fmt("distance", 23450.0, imperial).text)
        assertEquals("99.9", fmt("distance", 99940.0).text)
        assertEquals("100", fmt("distance", 99960.0).text)
        assertEquals("123", fmt("distance", 123400.0).text)
        assertEquals("999", fmt("distance", 999000.0).text)
    }

    @Test fun `short distance switches from metres to km`() {
        assertEquals(Formatted("350", "M", "888"), fmt("next-turn", 350.0))
        assertEquals(Formatted("2.3", "KM", "888"), fmt("next-turn", 2300.0))
        assertEquals(Formatted("328", "FT", "888"), fmt("next-turn", 100.0, imperial))
        assertEquals(Formatted("1.4", "MI", "888"), fmt("next-turn", 2300.0, imperial))
    }

    @Test fun `elevation in metres and feet`() {
        assertEquals(Formatted("812", "M", "8888"), fmt("altitude", 812.4))
        assertEquals(Formatted("2664", "FT", "8888"), fmt("altitude", 812.0, imperial))
    }

    @Test fun `grade keeps its sign and never shows minus zero`() {
        assertEquals("-7", fmt("grade", -7.4).text)
        assertEquals("0", fmt("grade", -0.3).text)
    }

    @Test fun `temperature in celsius and fahrenheit`() {
        assertEquals(Formatted("21", "°C", "-8"), fmt("temperature", 21.0))
        assertEquals("70", fmt("temperature", 21.0, imperial).text)
    }

    @Test fun `durations in h mm ss, minutes only under an hour`() {
        assertEquals("1:23:45", fmt("ride-time", 5025000.0).text)
        assertEquals("12:34", fmt("lap-time", 754000.0).text)
        assertEquals("0:05", fmt("lap-time", 5999.0).text)
    }

    @Test fun `time to destination in h mm`() {
        assertEquals(Formatted("0:58", "", "8:88"), fmt("time-to-dest", 3480000.0))
        assertEquals("2:05", fmt("time-to-dest", 7500000.0).text)
    }

    @Test fun `arrival is now plus time to destination`() {
        val tenOClock = 10 * 3_600_000L
        assertEquals("10:58", fmt("arrival", 3480000.0, nowMs = tenOClock).text)
    }

    @Test fun `clock shows the current time without data`() {
        val t = 14 * 3_600_000L + 7 * 60_000L
        assertEquals("14:07", fmt("clock", null, nowMs = t).text)
    }
}
