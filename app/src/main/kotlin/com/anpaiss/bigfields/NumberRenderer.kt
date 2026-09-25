package com.anpaiss.bigfields

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import androidx.core.content.res.ResourcesCompat

/**
 * Disegna il numero come immagine a un canale (solo alfa): il colore lo mette
 * la ImageView con setColorFilter, così un solo bitmap vale per ogni stile e
 * pesa un quarto di un bitmap a colori (478x300 px = 143 KB).
 *
 * L'altezza delle cifre è quella del contorno vero del carattere, non della
 * riga di testo: vedi [CellGeometry]. Il carattere è scelto in [NumberFont].
 */
object NumberRenderer {

    private const val REF_SIZE = 200f

    private class Face(typeface: Typeface) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply { this.typeface = typeface }
        val bounds = Rect()
        val refDigitH: Float = Rect().also {
            paint.textSize = REF_SIZE
            paint.getTextBounds("8", 0, 1, it)
        }.height().toFloat()
    }

    private val karoo by lazy { Face(Typeface.create("sans-serif", Typeface.BOLD)) }
    @Volatile private var condensed: Face? = null

    /** Carica i caratteri inclusi nell'app; da chiamare prima di disegnare. */
    fun init(context: Context) {
        if (condensed != null) return
        condensed = ResourcesCompat.getFont(context.applicationContext, R.font.barlow_condensed_bold)?.let { Face(it) }
    }

    private fun face(font: NumberFont): Face = when (font) {
        NumberFont.CONDENSED -> condensed ?: karoo
        NumberFont.KAROO -> karoo
    }

    /** Altezza massima delle cifre perché [template] stia in [areaW]×[areaH]. */
    fun maxDigitHeight(template: String, areaW: Int, areaH: Int, font: NumberFont): Float {
        val f = face(font)
        synchronized(f) {
            f.paint.textSize = REF_SIZE
            f.paint.getTextBounds(template, 0, template.length, f.bounds)
            return CellGeometry.maxDigitHeight(
                f.paint.measureText(template), f.bounds.height().toFloat(), f.refDigitH, areaW, areaH)
        }
    }

    /**
     * [text] con cifre alte [digitH] px in un bitmap [w]×[h]: cifre centrate in
     * verticale sulla loro altezza, in orizzontale secondo [align]. Il corpo è
     * fisso per il campo, quindi il numero non cambia grandezza quando cambia
     * il numero di cifre; si sposta solo, secondo l'allineamento.
     */
    fun render(text: String, align: Align, w: Int, h: Int, digitH: Float, font: NumberFont): Bitmap {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        if (digitH <= 0f) return bmp
        val f = face(font)
        synchronized(f) {
            f.paint.textSize = REF_SIZE * digitH / f.refDigitH
            val tw = f.paint.measureText(text)
            val x = when (align) {
                Align.LEFT -> 0f
                Align.CENTER -> (w - tw) / 2f
                Align.RIGHT -> w - tw
            }.coerceAtLeast(0f)
            Canvas(bmp).drawText(text, x, (h + digitH) / 2f, f.paint)
        }
        return bmp
    }
}
