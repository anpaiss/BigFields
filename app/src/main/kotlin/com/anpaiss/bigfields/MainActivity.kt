package com.anpaiss.bigfields

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/** Impostazioni: stile, didascalia, fascia per il banner, righe dei campi pagina, anteprima. */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs
    private var previewPage = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)
        NumberRenderer.init(this)

        val styles = FieldStyle.entries
        findViewById<Spinner>(R.id.sp_style).setup(styles.map { it.label }, styles.indexOf(prefs.style)) { pos ->
            prefs.style = styles[pos]
            showPreview()
        }
        val fonts = NumberFont.entries
        findViewById<Spinner>(R.id.sp_font).setup(fonts.map { it.label }, fonts.indexOf(prefs.font)) { pos ->
            prefs.font = fonts[pos]
            showPreview()
        }
        findViewById<SwitchCompat>(R.id.sw_label).bind(prefs.showLabel) { prefs.showLabel = it }
        findViewById<SwitchCompat>(R.id.sw_banner).bind(prefs.bannerRoom) { prefs.bannerRoom = it }
        findViewById<SwitchCompat>(R.id.sw_sample).bind(prefs.sampleValues) { prefs.sampleValues = it }

        buildPageEditors(findViewById(R.id.pages))

        findViewById<TextView>(R.id.tv_fields).text = Catalog.all.joinToString("\n") { "• " + nameOf(it) }
        showPreview()
    }

    private fun nameOf(m: Metric): String =
        getString(resources.getIdentifier("f_${m.typeId.replace('-', '_')}_name", "string", packageName))

    /** Per ogni campo pagina quattro menu: i dati delle righe, "—" = riga non usata. */
    private fun buildPageEditors(container: LinearLayout) {
        val choices = listOf(NONE) + Catalog.all.map { nameOf(it) }
        for (page in 1..Pages.COUNT) {
            container.addView(TextView(this).apply {
                text = "Big Page $page"
                setTextColor(Color.WHITE)
                textSize = 16f
                setPadding(0, dp(14), 0, dp(2))
            })
            val spinners = mutableListOf<Spinner>()
            val rows = prefs.pageRows(page)
            for (i in 0 until Pages.MAX_ROWS) {
                val current = rows.getOrNull(i)?.let { id -> Catalog.all.indexOfFirst { it.typeId == id } + 1 } ?: 0
                val sp = Spinner(this)
                spinners += sp
                sp.setup(choices, current) {
                    val ids = spinners.map { s -> s.selectedItemPosition }
                        .filter { it > 0 }.map { Catalog.all[it - 1].typeId }
                    prefs.setPageRows(page, ids)
                    previewPage = page
                    showPreview()
                }
                container.addView(sp, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(44)))
            }
        }
    }

    /**
     * Il campo pagina scelto, disegnato con lo stesso codice del Karoo
     * (RemoteViews applicate qui) a grandezza vera 478x642 px e rimpicciolito
     * nel riquadro, con il banner di navigazione simulato in fondo.
     */
    private fun showPreview() {
        val look = prefs.look()
        findViewById<TextView>(R.id.tv_banner_hint).text =
            if (look.bannerRoom) "On: while navigating, a band at the bottom of the page stays free for the banner; " +
                "the rows share the rest equally. Without navigation the rows fill the page."
            else "Off: the rows always fill the page. While navigating, the banner covers the bottom of the last row."
        findViewById<TextView>(R.id.tv_preview_title).text = "PREVIEW: BIG PAGE $previewPage WHILE NAVIGATING"

        val frames = prefs.pageRows(previewPage).mapNotNull { Catalog.byTypeId(it) }.map {
            FieldFrames.frame(it, it.sample, 3, look, Units(), Align.CENTER, System.currentTimeMillis())
        }
        val drawing = PageRenderer.drawing(frames, PAGE_W_PX, PAGE_H_PX, cellDims(resources), look.style)
        val host = findViewById<FrameLayout>(R.id.preview)
        host.removeAllViews()
        val page = FrameLayout(this)
        page.addView(PageRenderer.remoteViews(packageName, drawing).apply(this, page),
            FrameLayout.LayoutParams(PAGE_W_PX, PAGE_H_PX))
        page.addView(banner(), FrameLayout.LayoutParams(PAGE_W_PX, resources.getDimensionPixelSize(R.dimen.banner_band),
            Gravity.BOTTOM))
        host.addView(page, FrameLayout.LayoutParams(PAGE_W_PX, PAGE_H_PX))
        host.post {
            val scale = minOf(host.width.toFloat() / PAGE_W_PX, host.height.toFloat() / PAGE_H_PX)
            page.pivotX = 0f
            page.pivotY = 0f
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = (host.width - PAGE_W_PX * scale) / 2f
        }
    }

    /** Il banner "next cue" del Karoo, disegnato alla buona: serve a vedere cosa copre. */
    private fun banner(): View = TextView(this).apply {
        text = "↰  Turn left  ·  200 m"
        setTextColor(Color.WHITE)
        textSize = 20f
        gravity = Gravity.CENTER
        alpha = 0.92f
        background = GradientDrawable().apply {
            setColor(0xFF2E7D32.toInt())
            cornerRadius = 18f
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun Spinner.setup(items: List<String>, selected: Int, onPick: (Int) -> Unit) {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, items)
        setSelection(selected, false)
        // Android chiama il listener anche alla prima disposizione: conta solo un cambio vero
        var last = selected
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE)
                if (pos == last) return
                last = pos
                onPick(pos)
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun SwitchCompat.bind(value: Boolean, onChange: (Boolean) -> Unit) {
        isChecked = value
        setOnCheckedChangeListener { _, checked -> onChange(checked); showPreview() }
    }

    companion object {
        private const val NONE = "—"

        // Una cella a pagina intera sul Karoo 3, misurata alla prima prova (25/09)
        private const val PAGE_W_PX = 478
        private const val PAGE_H_PX = 642
    }
}
