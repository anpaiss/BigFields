package com.anpaiss.bigfields

import android.content.Context
import android.util.Log
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Log testuale in Android/data/com.anpaiss.bigfields/files/big_fields.log,
 * 1 MB con una generazione .old. Serve soprattutto alle misure della prima
 * prova sul Karoo: dimensioni delle celle (VIEW) e primo valore grezzo di
 * ogni dato (DATA). Specchiato su Logcat col tag BigFields.
 */
class FieldLog(ctx: Context) {

    private val file: File? = ctx.getExternalFilesDir(null)?.let { File(it, "big_fields.log") }
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    @Synchronized
    fun log(tag: String, msg: String) {
        Log.i("BigFields", "[$tag] $msg")
        val f = file ?: return
        runCatching {
            if (f.length() > MAX_BYTES) {
                val old = File(f.parentFile, f.name + ".old")
                old.delete()
                f.renameTo(old)
            }
            f.appendText("${fmt.format(Date())} [${tag.padEnd(4)}] $msg\n")
        }
    }

    companion object {
        private const val MAX_BYTES = 1_000_000L

        @Volatile var shared: FieldLog? = null
    }
}
