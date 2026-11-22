package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Test

class CellGeometryTest {

    private val dims = CellDims(padH = 8, padTop = 6, padBottom = 6, labelRow = 44, band = 100)

    @Test fun `caption loses its unit only in a cell too narrow for both`() {
        assertEquals(true, CellGeometry.dropUnit(238, "WAY HOME  MIN"))   // half cell: was cut to "WAY HO…"
        assertEquals(false, CellGeometry.dropUnit(238, "TO TOP  KM"))     // fits, as seen on the Karoo
        assertEquals(false, CellGeometry.dropUnit(478, "WAY HOME  MIN"))  // full width
    }

    @Test fun `caption falls back to the short title only when needed`() {
        assertEquals("WAY HOME", CellGeometry.fittingLabel(238, "WAY HOME  MIN", "WAY HOME", null))
        assertEquals("BIKE BATT", CellGeometry.fittingLabel(238, "BIKE BATTERY", "BIKE BATTERY", "BIKE BATT"))
        assertEquals("BIKE BATTERY", CellGeometry.fittingLabel(478, "BIKE BATTERY", "BIKE BATTERY", "BIKE BATT"))
        assertEquals("TO TOP  KM", CellGeometry.fittingLabel(238, "TO TOP  KM", "TO TOP", null))
    }

    @Test fun `every short caption fits a half cell`() {
        Catalog.all.mapNotNull { it.short }.forEach { assertEquals(it, false, CellGeometry.dropUnit(238, it)) }
        // 9 at most: "BOARD BATT" (10, wide letters) was still cut on the Karoo
        Catalog.all.mapNotNull { it.short }.forEach { assertEquals(it, true, it.length <= 9) }
    }

    @Test fun `number area subtracts padding, label and band`() {
        assertEquals(462 to 306, CellGeometry.numberArea(478, 318, dims, showLabel = false, band = false))
        assertEquals(462 to 262, CellGeometry.numberArea(478, 318, dims, showLabel = true, band = false))
        assertEquals(462 to 162, CellGeometry.numberArea(478, 318, dims, showLabel = true, band = true))
    }

    @Test fun `a tiny cell never gives a negative area`() {
        assertEquals(1 to 1, CellGeometry.numberArea(10, 50, dims, showLabel = true, band = true))
    }

    @Test fun `digit height is limited by height in a wide cell`() {
        // template 100x50 al corpo di riferimento, cifra alta 50: area 1000x100
        val h = CellGeometry.maxDigitHeight(100f, 50f, 50f, 1000, 100)
        assertEquals(100 * CellGeometry.FILL_H, h, 0.01f)
    }

    @Test fun `digit height is limited by width in a narrow cell`() {
        val h = CellGeometry.maxDigitHeight(100f, 50f, 50f, 100, 1000)
        assertEquals(50 * CellGeometry.FILL_W, h, 0.01f)
    }

    @Test fun `the page uses the lowest digit height, never more than the own`() {
        assertEquals(120f, CellGeometry.pageDigitHeight(180f, listOf(180f, 120f)), 0f)
        assertEquals(90f, CellGeometry.pageDigitHeight(90f, listOf(180f, 120f)), 0f)
        assertEquals(150f, CellGeometry.pageDigitHeight(150f, emptyList()), 0f)
    }

    @Test fun `page rows share the height equally, band once at the bottom`() {
        val d = dims.copy(rowGap = 4)
        // 642 - 4 di separatore = 638, due righe da 319; numero = 319 - 12 - 44
        assertEquals(462 to 263, CellGeometry.pageRowArea(478, 642, d, rows = 2, showLabel = true, band = false))
        // con la fascia: 642 - 100 - 4 = 538, righe da 269
        assertEquals(462 to 213, CellGeometry.pageRowArea(478, 642, d, rows = 2, showLabel = true, band = true))
        assertEquals(462 to 630, CellGeometry.pageRowArea(478, 642, d, rows = 1, showLabel = false, band = false))
        // the field list's preview (240x128): two captioned rows leave 6 px, so no captions there
        assertEquals(false, CellGeometry.pageLabelFits(240, 128, d, rows = 2, band = false))
        assertEquals(true, CellGeometry.pageLabelFits(478, 642, d, rows = 2, band = true))
    }

    @Test fun `banner band only where the digits keep room`() {
        // half cell of a ten-field page, 238x126 (16/11): 126 - 100 - 12 - 44 < 0, no band
        assertEquals(false, CellGeometry.bandFits(126, dims, showLabel = true))
        // a third of the page, 254 high: 254 - 100 - 12 - 44 = 98, band kept
        assertEquals(true, CellGeometry.bandFits(254, dims, showLabel = true))
        // without the caption 126 - 100 - 12 = 14: still too little
        assertEquals(false, CellGeometry.bandFits(126, dims, showLabel = false))
        // a two-row page field 318 high: (318 - 100) / 2 = 109 per row, 109 - 56 = 53 < 60
        assertEquals(false, CellGeometry.bandFits(318, dims, showLabel = true, rows = 2))
    }
}
