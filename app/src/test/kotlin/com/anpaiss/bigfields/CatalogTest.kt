package com.anpaiss.bigfields

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/** Il catalogo, extension_info.xml e strings.xml devono dire la stessa cosa. */
class CatalogTest {

    private val res = File("src/main/res")
    private val info = File(res, "xml/extension_info.xml").readText()
    private val strings = File(res, "values/strings.xml").readText()

    @Test fun `type ids are unique`() {
        val ids = Catalog.all.map { it.typeId }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun `every catalog entry is declared to the Karoo and vice versa`() {
        val declared = Regex("""typeId="([^"]+)"""").findAll(info).map { it.groupValues[1] }.toList()
        val pages = (1..Pages.COUNT).map { Pages.typeId(it) }
        assertEquals(pages + Catalog.all.map { it.typeId }, declared)
    }

    @Test fun `every declared field has a name and a description`() {
        for (m in Catalog.all) {
            val key = m.typeId.replace('-', '_')
            assertTrue("name of ${m.typeId}", strings.contains("name=\"f_${key}_name\""))
            assertTrue("description of ${m.typeId}", strings.contains("name=\"f_${key}_desc\""))
        }
    }

    @Test fun `page defaults are valid catalog rows`() {
        for (page in 1..Pages.COUNT) {
            val rows = Pages.defaults[page - 1]
            assertEquals(rows, Pages.sanitize(page, rows))
            assertTrue(rows.size in 1..Pages.MAX_ROWS)
        }
    }

    @Test fun `page rows drop unknown data, cap at two and fall back to the default`() {
        assertEquals(listOf("speed", "hr"), Pages.sanitize(1, listOf("speed", "nope", "hr")))
        assertEquals(2, Pages.sanitize(1, Catalog.all.map { it.typeId }).size)
        // Pagine salvate con tre righe prima del limite (28/09): restano le prime due
        assertEquals(listOf("altitude", "grade"), Pages.sanitize(4, listOf("altitude", "grade", "temperature")))
        assertEquals(Pages.defaults[1], Pages.sanitize(2, emptyList()))
    }

    @Test fun `every entry except the clock reads a Karoo data type`() {
        for (m in Catalog.all) {
            assertEquals(m.typeId, m.kind == Kind.CLOCK, m.karooType == null)
        }
    }

    @Test fun `counts carry their unit`() {
        for (m in Catalog.all.filter { it.kind == Kind.COUNT }) {
            assertTrue(m.typeId, m.unitLabel.isNotEmpty())
        }
    }
}
