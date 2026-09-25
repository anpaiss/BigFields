package com.anpaiss.bigfields

import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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

/** Aspetto in corsa: la fascia per il banner vale solo mentre si naviga, fuori il banner non c'è. */
fun rideLook(prefs: Prefs, hub: BigFieldsExtension): Flow<Prefs.Look> =
    combine(prefs.lookFlow(), hub.navigating) { look, nav -> look.copy(bannerRoom = look.bannerRoom && nav) }

/** Frame dell'anteprima nell'editor profili: valore d'esempio, zona 3 per vedere i colori. */
fun previewFrames(metric: Metric, look: Flow<Prefs.Look>, hub: BigFieldsExtension, align: Align): Flow<FieldFrame> =
    combine(look, hub.units) { l, units ->
        FieldFrames.frame(metric, metric.sample, 3, l, units, align, System.currentTimeMillis())
    }

/** Frame in corsa di un dato, aggiornati a ogni valore, zona, cambio d'aspetto o di unità. */
fun liveFrames(metric: Metric, look: Flow<Prefs.Look>, hub: BigFieldsExtension, align: Align): Flow<FieldFrame> =
    combine(values(metric, hub), zones(metric, hub), look, hub.units, ticks(metric)) { raw, zone, l, units, now ->
        val shown = raw ?: if (l.sampleValues) metric.sample else null
        FieldFrames.frame(metric, shown, zone ?: if (l.sampleValues) 3 else null, l, units, align, now)
    }

/** Valore grezzo del dato; null quando manca. Registra nel log il primo stato ricevuto. */
private fun values(metric: Metric, hub: BigFieldsExtension): Flow<Double?> {
    val type = metric.karooType ?: return flowOf(0.0)
    var logged = false
    return hub.stream(type).onEach { state ->
        if (!logged) {
            logged = true
            FieldLog.shared?.log("DATA", "${metric.typeId} <- $type primo stato: " +
                if (state is StreamState.Streaming) "valori=${state.dataPoint.values}" else state.toString())
        }
    }.map { (it as? StreamState.Streaming)?.dataPoint?.singleValue }
}

private fun zones(metric: Metric, hub: BigFieldsExtension): Flow<Int?> {
    val zone = metric.zone ?: return flowOf(null)
    return hub.stream(zone.karooType).map { (it as? StreamState.Streaming)?.dataPoint?.singleValue?.toInt() }
}

/** Orologio a 1 s solo per i dati che cambiano col tempo; gli altri un istante fisso. */
private fun ticks(metric: Metric): Flow<Long> =
    if (metric.kind == Kind.CLOCK || metric.kind == Kind.ARRIVAL) flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1_000L)
        }
    } else flowOf(0L)
