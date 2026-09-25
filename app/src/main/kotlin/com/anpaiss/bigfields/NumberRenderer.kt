package com.anpaiss.bigfields

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface

/**
 * Disegna il numero come immagine a un canale (solo alfa): il colore lo mette
 * la ImageView con setColorFilter, così un solo bitmap vale per ogni stile e
 * pesa un quarto di un bitmap a colori (478x300 px = 143 KB).
 *
 * L'altezza delle cifre è quella del contorno vero del carattere, non della
 * riga di testo: vedi [CellGeometry].
 */
object NumberRenderer {

    private const val REF_SIZE = 200f

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.SUBPIXEL_TEXT_FLAG).apply {
        typeface = Typeface.create("sans-serif-condensed", Typeface.BOLD)
    }
    private val r = Rect()

    /** Altezza di una cifra al corpo di riferimento. */
    private val refDigitH: Float = synchronized(paint) {
        paint.textSize = REF_SIZE
        paint.getTextBounds("8", 0, 1, r)
        r.height().toFloat()
    }

    /** Altezza massima delle cifre perché [template] stia in [areaW]×[areaH]. */
    fun maxDigitHeight(template: String, areaW: Int, areaH: Int): Float = synchronized(paint) {
        paint.textSize = REF_SIZE
        paint.getTextBounds(template, 0, template.length, r)
        CellGeometry.maxDigitHeight(paint.measureText(template), r.height().toFloat(), refDigitH, areaW, areaH)
    }

    /**
     * [text] con cifre alte [digitH] px in un bitmap [w]×[h]: cifre centrate in
     * verticale sulla loro altezza, in orizzontale secondo [align]. Il corpo è
     * fisso per il campo, quindi il numero non cambia grandezza quando cambia
     * il numero di cifre; si sposta solo, secondo l'allineamento.
     */
    fun render(text: String, align: Align, w: Int, h: Int, digitH: Float): Bitmap = synchronized(paint) {
        val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ALPHA_8)
        if (digitH <= 0f) return bmp
        paint.textSize = REF_SIZE * digitH / refDigitH
        val tw = paint.measureText(text)
        val x = when (align) {
            Align.LEFT -> 0f
            Align.CENTER -> (w - tw) / 2f
            Align.RIGHT -> w - tw
        }.coerceAtLeast(0f)
        val baseline = (h + digitH) / 2f
        Canvas(bmp).drawText(text, x, baseline, paint)
        bmp
    }
}
