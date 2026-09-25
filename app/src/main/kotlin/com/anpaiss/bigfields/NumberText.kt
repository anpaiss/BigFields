package com.anpaiss.bigfields

import android.graphics.Color
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

/** Il numero da mettere nella TextView: il riempimento di [NumberPadding] è trasparente. */
fun Padded.toCharSequence(): CharSequence {
    if (before.isEmpty() && after.isEmpty()) return text
    val s = SpannableString(full)
    if (before.isNotEmpty()) {
        s.setSpan(ForegroundColorSpan(Color.TRANSPARENT), 0, before.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    if (after.isNotEmpty()) {
        s.setSpan(ForegroundColorSpan(Color.TRANSPARENT), before.length + text.length, s.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
    return s
}
