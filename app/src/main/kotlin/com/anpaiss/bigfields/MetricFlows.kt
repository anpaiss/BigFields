package com.anpaiss.bigfields

import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/** Letture comuni a campi singoli e campi pagina. */

fun ViewConfig.align(): Align = when (alignment) {
    ViewConfig.Alignment.LEFT -> Align.LEFT
    ViewConfig.Alignment.CENTER -> Align.CENTER
    ViewConfig.Alignment.RIGHT -> Align.RIGHT
}

/**
 * Richieste di ridisegno per tutti i campi. Mentre l'app delle impostazioni è
 * in primo piano la schermata di corsa del Karoo è in pausa e può perdere
 * l'ultimo aggiornamento (prima prova 25/09: pagina rimasta a metà tra due
 * stili). L'app, chiudendosi, chiede a tutti i campi di ridisegnarsi.
 */
object Redraw {
    val requests = MutableStateFlow(0L)
}

/** Aspetto in corsa: la fascia per il banner vale solo mentre si naviga, fuori il banner non c'è. */
fun rideLook(hub: BigFieldsExtension): Flow<Prefs.Look> =
    combine(hub.look, hub.navigating) { look, nav -> look.copy(bannerRoom = look.bannerRoom && nav) }

/** Aspetto nell'editor profili: niente fascia, la cella è piccola e il banner lì non c'è. */
fun previewLook(hub: BigFieldsExtension): Flow<Prefs.Look> = hub.look.map { it.copy(bannerRoom = false) }

/** Quello che arriva dal Karoo per un dato: valore grezzo, zona, istante. */
data class RowInput(val raw: Double?, val zone: Int?, val now: Long)

/** Dati in corsa di un dato, aggiornati a ogni valore, zona o scatto d'orologio. */
fun rowInput(metric: Metric, hub: BigFieldsExtension): Flow<RowInput> =
    combine(values(metric, hub), zones(metric, hub), ticks(metric)) { raw, zone, now -> RowInput(raw, zone, now) }

/** Anteprima nell'editor profili: valore d'esempio, zona 3 per vedere i colori. */
fun previewInput(metric: Metric): Flow<RowInput> = flowOf(RowInput(metric.sample, 3, System.currentTimeMillis()))

/**
 * Il frame di un dato con un aspetto dato. Una pagina calcola tutte le righe
 * con lo stesso aspetto nello stesso passaggio: righe calcolate ognuna per
 * conto suo davano un disegno intermedio con stili diversi (25/09).
 */
fun frameOf(metric: Metric, input: RowInput, look: Prefs.Look, units: Units, align: Align): FieldFrame {
    val shown = input.raw ?: if (look.sampleValues) metric.sample else null
    val zone = input.zone ?: if (look.sampleValues) 3 else null
    return FieldFrames.frame(metric, shown, zone, look, units, align, input.now)
}

/** Valore grezzo del dato; null quando manca. Registra nel log il primo stato e i cambi di stato. */
private fun values(metric: Metric, hub: BigFieldsExtension): Flow<Double?> {
    val type = metric.karooType ?: return flowOf(0.0)
    var logged = false
    var loggedFields = false
    var lastKind: String? = null
    return hub.stream(type).onEach { state ->
        if (!logged) {
            logged = true
            FieldLog.shared?.log("DATA", "${metric.typeId} <- $type primo stato: " +
                if (state is StreamState.Streaming) "valori=${state.dataPoint.values}" else state.toString())
        }
        // Cambi di stato (sensore che si collega o si perde), non ogni valore
        val kind = state::class.simpleName
        if (lastKind != null && kind != lastKind) {
            FieldLog.shared?.debug("DATA", "${metric.typeId}: $lastKind -> $kind" +
                if (state is StreamState.Streaming) " valori=${state.dataPoint.values}" else "")
        }
        lastKind = kind
    }.map { state ->
        val values = (state as? StreamState.Streaming)?.dataPoint?.values ?: return@map null
        val expected = ValuePicker.fieldOf[metric.typeId]
        val v = ValuePicker.pick(values, expected)
        // Più valori insieme: una riga di log la prima volta, per verificare il campo scelto
        if (values.size > 1 && !loggedFields) {
            loggedFields = true
            FieldLog.shared?.log("DATA", "${metric.typeId}: valori multipli ${values.keys}, " +
                "letto ${expected ?: "nessun campo atteso"} = $v")
        }
        v
    }
}

private fun zones(metric: Metric, hub: BigFieldsExtension): Flow<Int?> {
    val zone = metric.zone ?: return flowOf(null)
    return hub.stream(zone.karooType).map { state ->
        (state as? StreamState.Streaming)?.dataPoint?.values
            ?.let { ValuePicker.pick(it, ValuePicker.zoneFieldOf[zone]) }?.toInt()
    }
}

/** Orologio a 1 s solo per i dati che cambiano col tempo; gli altri un istante fisso. */
private fun ticks(metric: Metric): Flow<Long> =
    if (metric.kind == Kind.CLOCK || metric.kind == Kind.ARRIVAL) flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    } else flowOf(0L)
