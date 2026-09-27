package com.anpaiss.bigfields

import io.hammerhead.karooext.models.DataType.Field
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValuePickerTest {

    @Test fun `distance to destination is read by name, not as the first value`() {
        // Ordine come arriva dal Karoo: ON_ROUTE prima della distanza (primo giro 26/09)
        val values = linkedMapOf(Field.ON_ROUTE to 0.0, Field.DISTANCE_TO_DESTINATION to 12345.0)
        assertEquals(12345.0, ValuePicker.pick(values, ValuePicker.fieldOf["dist-to-dest"])!!, 0.0)
    }

    @Test fun `climb left is the ascent, not the descent`() {
        val values = linkedMapOf(Field.DESCENT_REMAINING to 0.0, Field.ASCENT_REMAINING to 420.0)
        assertEquals(420.0, ValuePicker.pick(values, ValuePicker.fieldOf["climb-left"])!!, 0.0)
    }

    @Test fun `a single value is used whatever its key`() {
        assertEquals(7.5, ValuePicker.pick(mapOf(Field.SINGLE to 7.5), Field.SPEED)!!, 0.0)
        assertEquals(812.0, ValuePicker.pick(mapOf("OTHER" to 812.0), null)!!, 0.0)
    }

    @Test fun `several values without the expected one give no value`() {
        assertNull(ValuePicker.pick(mapOf("A" to 1.0, "B" to 2.0), Field.SPEED))
    }

    @Test fun `a value that is not finite gives no value instead of a crash`() {
        assertNull(ValuePicker.pick(mapOf(Field.SPEED to Double.NaN), Field.SPEED))
        assertNull(ValuePicker.pick(mapOf("X" to Double.POSITIVE_INFINITY), null))
    }

    @Test fun `every multi-value data type of the catalog has an expected field`() {
        for (id in listOf("dist-to-dest", "climb-left", "time-to-dest", "arrival")) {
            assertTrue(id, ValuePicker.fieldOf[id] != null)
        }
        for (id in ValuePicker.fieldOf.keys) assertTrue(id, Catalog.byTypeId(id) != null)
    }
}
