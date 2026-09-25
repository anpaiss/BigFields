package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Test

class NumberPaddingTest {

    @Test fun `right aligned number is padded on the left`() {
        assertEquals(Padded("8", "9.8", ""), NumberPadding.pad("9.8", "88.8", Align.RIGHT))
    }

    @Test fun `left aligned number is padded on the right`() {
        assertEquals(Padded("", "9.8", "8"), NumberPadding.pad("9.8", "88.8", Align.LEFT))
    }

    @Test fun `centred number is padded on both sides`() {
        assertEquals(Padded("8", "5", "8"), NumberPadding.pad("5", "888", Align.CENTER))
        assertEquals(Padded("", "84", "1"), NumberPadding.pad("84", "188", Align.CENTER))
    }

    @Test fun `every value of the same field has the same total length`() {
        for (text in listOf("0.0", "9.8", "10.2", "55.5")) {
            assertEquals(4, NumberPadding.pad(text, "88.8", Align.RIGHT).full.length)
        }
    }

    @Test fun `a number longer than the template is left alone`() {
        assertEquals(Padded("", "123.4", ""), NumberPadding.pad("123.4", "88.8", Align.RIGHT))
    }
}
