package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.TimeZone
import kotlin.math.pow

class FieldStyleTest {

    private fun luminance(c: Int): Double {
        fun ch(v: Int): Double {
            val s = v / 255.0
            return if (s <= 0.03928) s / 12.92 else ((s + 0.055) / 1.055).pow(2.4)
        }
        return 0.2126 * ch((c shr 16) and 0xFF) + 0.7152 * ch((c shr 8) and 0xFF) + 0.0722 * ch(c and 0xFF)
    }

    private fun contrast(a: Int, b: Int): Double {
        val (hi, lo) = listOf(luminance(a), luminance(b)).sortedDescending()
        return (hi + 0.05) / (lo + 0.05)
    }

    @Test fun `every style and zone meets WCAG AA for number and label`() {
        for (style in FieldStyle.entries) {
            for (zone in listOf(null, 1, 2, 3, 4, 5)) {
                val c = style.colorsFor(zone)
                assertTrue("$style z$zone number", contrast(c.value, c.fill) >= 4.5)
                assertTrue("$style z$zone label", contrast(c.label, c.fill) >= 4.5)
            }
        }
    }

    @Test fun `power zones above five use the zone five colours`() {
        for (style in FieldStyle.entries) {
            assertEquals(style.colorsFor(5), style.colorsFor(7))
        }
    }

    @Test fun `the zone colours only fields that have a zone and a value`() {
        val look = Prefs.Look(FieldStyle.VIVID, true)
        val utc = TimeZone.getTimeZone("UTC")
        val hr = Catalog.byTypeId("hr")!!
        val speed = Catalog.byTypeId("speed")!!
        val plain = FieldStyle.VIVID.colorsFor(null)
        assertEquals(FieldStyle.VIVID.colorsFor(4),
            FieldFrames.frame(hr, 170.0, 4, look, Units(), Align.RIGHT, 0L, utc).colors)
        assertEquals(plain, FieldFrames.frame(hr, null, 4, look, Units(), Align.RIGHT, 0L, utc).colors)
        assertEquals(plain, FieldFrames.frame(speed, 8.0, 4, look, Units(), Align.RIGHT, 0L, utc).colors)
    }

    @Test fun `label joins name and unit`() {
        val look = Prefs.Look(FieldStyle.KAROO_DARK, true)
        val utc = TimeZone.getTimeZone("UTC")
        assertEquals("SPEED  KM/H",
            FieldFrames.frame(Catalog.byTypeId("speed")!!, 8.0, null, look, Units(), Align.RIGHT, 0L, utc).label)
        assertEquals("RIDE TIME",
            FieldFrames.frame(Catalog.byTypeId("ride-time")!!, 8.0, null, look, Units(), Align.RIGHT, 0L, utc).label)
    }

    @Test fun `the banner band follows the option and is the same for every field`() {
        val utc = TimeZone.getTimeZone("UTC")
        for (room in listOf(true, false)) {
            val look = Prefs.Look(FieldStyle.KAROO_DARK, true, bannerRoom = room)
            for (m in Catalog.all) {
                assertEquals(m.typeId, room,
                    FieldFrames.frame(m, m.sample, null, look, Units(), Align.RIGHT, 0L, utc).bannerBand)
            }
        }
    }

    @Test fun `four layouts for label and band`() {
        val utc = TimeZone.getTimeZone("UTC")
        val speed = Catalog.byTypeId("speed")!!
        val layouts = listOf(true, false).flatMap { label -> listOf(true, false).map { band ->
            layoutFor(FieldFrames.frame(speed, 8.0, null, Prefs.Look(FieldStyle.VIVID, label, band),
                Units(), Align.RIGHT, 0L, utc))
        } }
        assertEquals(4, layouts.toSet().size)
    }
}
