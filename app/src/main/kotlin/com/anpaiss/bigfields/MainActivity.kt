package com.anpaiss.bigfields

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

/** Impostazioni: stile, didascalia sì/no, anteprima e elenco dei campi. */
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

        val sw = findViewById<SwitchCompat>(R.id.sw_label)
        sw.isChecked = prefs.showLabel
        sw.setOnCheckedChangeListener { _, checked ->
            prefs.showLabel = checked
            showPreview()
        }

        findViewById<TextView>(R.id.tv_fields).text = Catalog.all.joinToString("\n") {
            "• " + getString(resources.getIdentifier("f_${it.typeId.replace('-', '_')}_name", "string", packageName))
        }
        showPreview()
    }

    /** Stesso layout e stessi colori del campo sul Karoo, con un valore d'esempio. */
    private fun showPreview() {
        val metric = Catalog.byTypeId("hr") ?: return
        val look = Prefs.Look(prefs.style, prefs.showLabel)
        val f = FieldFrames.frame(metric, metric.sample, 3, look, Units(), Align.RIGHT, System.currentTimeMillis())
        val host = findViewById<FrameLayout>(R.id.preview)
        host.removeAllViews()
        val layout = if (f.showLabel) R.layout.view_big_field else R.layout.view_big_field_bare
        val v = LayoutInflater.from(this).inflate(layout, host, false)
        val root = v.findViewById<View>(R.id.root)
        if (f.colors.bgRes != null) root.setBackgroundResource(f.colors.bgRes) else root.setBackgroundColor(f.colors.fill)
        v.findViewById<TextView>(R.id.tv_label).apply { text = f.label; setTextColor(f.colors.label) }
        v.findViewById<TextView>(R.id.tv_value).apply {
            setTextColor(f.colors.value)
            text = f.number.toCharSequence()
        }
        host.addView(v)
    }
}
