package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Test

class CellGeometryTest {

    private val dims = CellDims(padH = 8, padTop = 6, padBottom = 6, labelRow = 44, band = 100)

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
}
