package com.anpaiss.bigfields

import android.content.Context
import android.util.Log
import java.io.File
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Log testuale in Android/data/com.anpaiss.bigfields/files/big_fields.log,
 * 1 MB per file e tre generazioni (.old, .old2, .old3), come BetterEta.
 * Uno solo per processo ([get]): estensione e app scrivono nello stesso file.
 *
 * [log] scrive sempre (avvio, errori, crash); [debug] solo con il log
 * diagnostico acceso nell'app (default acceso durante le prove). Specchiato
 * su Logcat col tag BigFields.
 */
class FieldLog private constructor(ctx: Context) {

    private val file: File? = ctx.getExternalFilesDir(null)?.let { File(it, "big_fields.log") }
    private val fmt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

    /** Log diagnostico: aggiornato da [Prefs] quando cambia l'interruttore. */
    @Volatile var verbose: Boolean = true

    @Synchronized
    fun log(tag: String, msg: String) {
        Log.i("BigFields", "[$tag] $msg")
        val f = file ?: return
        runCatching {
            if (f.length() > MAX_BYTES) rotate(f)
            f.appendText("${fmt.format(Date())} [${tag.padEnd(4)}] $msg\n")
        }
    }

    fun debug(tag: String, msg: String) {
        if (verbose) log(tag, msg)
    }

    fun error(tag: String, msg: String, t: Throwable? = null) {
        val trace = t?.let { StringWriter().also { w -> it.printStackTrace(PrintWriter(w)) }.toString() }
        log(tag, if (trace == null) msg else "$msg\n$trace")
    }

    private fun rotate(f: File) {
        val dir = f.parentFile
        File(dir, f.name + ".old3").delete()
        File(dir, f.name + ".old2").renameTo(File(dir, f.name + ".old3"))
        File(dir, f.name + ".old").renameTo(File(dir, f.name + ".old2"))
        f.renameTo(File(dir, f.name + ".old"))
    }

    companion object {
        private const val MAX_BYTES = 1_000_000L

        @Volatile var shared: FieldLog? = null
            private set

        /**
         * Il log del processo. Al primo uso registra la versione e installa un
         * gestore che scrive nel file ogni crash prima di passarlo al sistema.
         */
        @Synchronized
        fun get(ctx: Context): FieldLog {
            shared?.let { return it }
            val log = FieldLog(ctx.applicationContext)
            log.verbose = Prefs(ctx).diagnosticLog
            shared = log
            runCatching {
                val pi = ctx.packageManager.getPackageInfo(ctx.packageName, 0)
                log.log("SYS", "Versione ${pi.versionName} (${pi.longVersionCode}), installata " +
                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(pi.lastUpdateTime)))
            }
            val previous = Thread.getDefaultUncaughtExceptionHandler()
            Thread.setDefaultUncaughtExceptionHandler { thread, t ->
                log.error("CRASH", "Eccezione non gestita su ${thread.name}", t)
                previous?.uncaughtException(thread, t)
            }
            return log
        }
    }
}

/**
 * Riepilogo al minuto dei ridisegni di un campo: quanti, l'ultimo testo
 * mostrato e il tempo massimo di disegno. Solo con il log diagnostico.
 */
class RedrawStats(private val name: String) {
    private var count = 0
    private var maxMs = 0L
    private var windowStart = System.currentTimeMillis()

    fun record(shown: String, ms: Long) {
        count++
        if (ms > maxMs) maxMs = ms
        val now = System.currentTimeMillis()
        if (now - windowStart >= 60_000L) {
            FieldLog.shared?.debug("DRAW", "$name: $count ridisegni in ${(now - windowStart) / 1000}s, " +
                "ultimo $shown, disegno max $maxMs ms")
            count = 0
            maxMs = 0
            windowStart = now
        }
    }
}
