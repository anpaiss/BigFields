package com.anpaiss.bigfields

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * I layout dei campi vengono gonfiati dai RemoteViews, che ammettono solo
 * alcune classi. Un <View> semplice come fascia per il banner faceva chiudere
 * l'app e avrebbe rotto i campi in navigazione (25/09).
 */
class RemoteLayoutTest {

    private val allowed = setOf(
        "FrameLayout", "LinearLayout", "RelativeLayout", "GridLayout",
        "TextView", "ImageView", "Button", "ImageButton", "ProgressBar",
    )

    @Test fun `field layouts use only classes RemoteViews can inflate`() {
        val layouts = File("src/main/res/layout").listFiles { f -> f.name.startsWith("view_big_") }!!
        assertTrue(layouts.size >= 20)
        for (f in layouts) {
            val tags = Regex("""<([A-Za-z.]+)[\s>/]""").findAll(f.readText())
                .map { it.groupValues[1] }.filter { it != "?xml" }.toSet()
            for (t in tags) assertTrue("${f.name}: <$t> non ammesso nei RemoteViews", t in allowed)
        }
    }
}
