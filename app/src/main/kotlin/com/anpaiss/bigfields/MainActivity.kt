package com.anpaiss.bigfields

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/** Impostazioni: stile, didascalia, fascia per il banner, anteprima e elenco dei campi. */
class MainActivity : AppCompatActivity() {

    private lateinit var prefs: Prefs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        prefs = Prefs(this)

        val spinner = findViewById<Spinner>(R.id.sp_style)
        val styles = FieldStyle.entries
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, styles.map { it.label })
        spinner.setSelection(styles.indexOf(prefs.style))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                (view as? TextView)?.setTextColor(Color.WHITE)
                prefs.style = styles[pos]
                showPreview()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        findViewById<SwitchCompat>(R.id.sw_label).apply {
            isChecked = prefs.showLabel
            setOnCheckedChangeListener { _, checked -> prefs.showLabel = checked; showPreview() }
        }
        findViewById<SwitchCompat>(R.id.sw_banner).apply {
            isChecked = prefs.bannerRoom
            setOnCheckedChangeListener { _, checked -> prefs.bannerRoom = checked; showPreview() }
        }

        findViewById<SwitchCompat>(R.id.sw_sample).apply {
            isChecked = prefs.sampleValues
            setOnCheckedChangeListener { _, checked -> prefs.sampleValues = checked }
        }

        findViewById<TextView>(R.id.tv_fields).text = Catalog.all.joinToString("\n") {
            "• " + getString(resources.getIdentifier("f_${it.typeId.replace('-', '_')}_name", "string", packageName))
        }
        showPreview()
    }

    /**
     * Una pagina a due campi in pixel veri (velocità sopra, frequenza cardiaca
     * in zona 3 sotto), con sopra il banner di navigazione simulato, poi
     * rimpicciolita per stare nel riquadro. Stessi layout e colori del Karoo.
     */
    private fun showPreview() {
        val look = prefs.look()
        findViewById<TextView>(R.id.tv_banner_hint).text =
            if (look.bannerRoom) "On: while navigating, every field keeps its bottom band free. The banner never covers a number, " +
                "and all numbers stay the same size, a little smaller. Without navigation the numbers fill the cell."
            else "Off: the numbers are larger. While navigating, the banner covers the bottom of the lower field."

        val host = findViewById<FrameLayout>(R.id.preview)
        host.removeAllViews()

        val page = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val dims = cellDims(resources)
        val frames = listOf("speed" to null, "hr" to 3).mapNotNull { (typeId, zone) ->
            Catalog.byTypeId(typeId)?.let {
                FieldFrames.frame(it, it.sample, zone, look, Units(), Align.CENTER, System.currentTimeMillis())
            }
        }
        // Come sul Karoo: cifre alte uguali in tutta la pagina
        val areas = frames.map { CellGeometry.numberArea(CELL_W_PX, CELL_H_PX, dims, it.showLabel, it.bannerBand) }
        val digitH = frames.zip(areas).minOf { (f, a) -> NumberRenderer.maxDigitHeight(f.template, a.first, a.second) }
        for ((f, a) in frames.zip(areas)) {
            column.addView(cell(f, a, digitH), LinearLayout.LayoutParams(CELL_W_PX, CELL_H_PX).apply { bottomMargin = GAP_PX })
        }
        page.addView(column)
        page.addView(banner(), FrameLayout.LayoutParams(PAGE_W_PX, resources.getDimensionPixelSize(R.dimen.banner_band),
            Gravity.BOTTOM))
        host.addView(page, FrameLayout.LayoutParams(PAGE_W_PX, PAGE_H_PX))

        host.post {
            val scale = host.height.toFloat() / PAGE_H_PX
            page.pivotX = 0f
            page.pivotY = 0f
            page.scaleX = scale
            page.scaleY = scale
            page.translationX = (host.width - PAGE_W_PX * scale) / 2f
        }
    }

    private fun cell(f: FieldFrame, area: Pair<Int, Int>, digitH: Float): View {
        val v = LayoutInflater.from(this).inflate(layoutFor(f), null, false)
        val root = v.findViewById<View>(R.id.root)
        if (f.colors.bgRes != null) root.setBackgroundResource(f.colors.bgRes) else root.setBackgroundColor(f.colors.fill)
        v.findViewById<TextView>(R.id.tv_label).apply { text = f.label; setTextColor(f.colors.label) }
        v.findViewById<ImageView>(R.id.iv_value).apply {
            setImageBitmap(NumberRenderer.render(f.text, f.align, area.first, area.second, digitH))
            setColorFilter(f.colors.value)
        }
        return v
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
        layoutParams = ViewGroup.LayoutParams(PAGE_W_PX, ViewGroup.LayoutParams.MATCH_PARENT)
    }

    companion object {
        // Pagina di corsa del Karoo 3 misurata alla prima prova (25/09): una
        // cella a pagina intera è 478x642 px; qui divisa in due.
        private const val PAGE_W_PX = 480
        private const val CELL_W_PX = 478
        private const val CELL_H_PX = 318
        private const val GAP_PX = 3
        private const val PAGE_H_PX = 2 * (CELL_H_PX + GAP_PX)
    }
}
