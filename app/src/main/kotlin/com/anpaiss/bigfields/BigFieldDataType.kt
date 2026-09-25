package com.anpaiss.bigfields

import android.content.Context
import android.widget.RemoteViews
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.DataPoint
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

/**
 * Un campo grande: legge il dato nativo [metric] e lo ridisegna a tutta cella.
 * Il Karoo non mostra la sua intestazione (showHeader = false): didascalia e
 * unità le disegniamo noi, più piccole, per lasciare spazio al numero.
 */
class BigFieldDataType(
    extension: String,
    private val metric: Metric,
    private val hub: BigFieldsExtension,
) : DataTypeImpl(extension, metric.typeId) {

    /** Ripete il dato nativo col nostro id: serve se il campo viene usato come numerico. */
    override fun startStream(emitter: Emitter<StreamState>) {
        val type = metric.karooType
        if (type == null) {
            emitter.onNext(StreamState.NotAvailable)
            return
        }
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            hub.stream(type).collect { state ->
                emitter.onNext(
                    if (state is StreamState.Streaming) StreamState.Streaming(
                        DataPoint(dataTypeId, state.dataPoint.values, state.dataPoint.sourceId)
                    ) else state
                )
            }
        }
        emitter.setCancellable { scope.cancel() }
    }

    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        // La misura della prima prova: cella in px, griglia (60 = pagina intera)
        // e carattere in sp che il Karoo userebbe per un campo nativo lì.
        FieldLog.shared?.log("VIEW", "startView ${metric.typeId} size=${config.viewSize} " +
            "grid=${config.gridSize} nativeTextSize=${config.textSize}sp align=${config.alignment} " +
            "preview=${config.preview}")
        val align = when (config.alignment) {
            ViewConfig.Alignment.LEFT -> Align.LEFT
            ViewConfig.Alignment.CENTER -> Align.CENTER
            ViewConfig.Alignment.RIGHT -> Align.RIGHT
        }
        val prefs = Prefs(context)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))

            val frames: Flow<FieldFrame> = if (config.preview) {
                // Editor profili: un valore d'esempio, zona 3 per vedere i colori
                combine(prefs.lookFlow(), hub.units) { look, units ->
                    FieldFrames.frame(metric, metric.sample, 3, look, units, align, System.currentTimeMillis())
                }
            } else {
                combine(values(), zones(), prefs.lookFlow(), hub.units, ticks()) { raw, zone, look, units, now ->
                    FieldFrames.frame(metric, raw, zone, look, units, align, now)
                }.whileVisible(hub.visibleDataTypeIds, dataTypeId)
            }

            frames.distinctUntilChanged().collect { emitter.updateView(render(context, it)) }
        }
        emitter.setCancellable {
            FieldLog.shared?.log("VIEW", "stop ${metric.typeId}")
            scope.cancel()
        }
    }

    /** Valore grezzo del dato; null quando manca. Registra nel log il primo valore ricevuto. */
    private fun values(): Flow<Double?> {
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

    private fun zones(): Flow<Int?> {
        val zone = metric.zone ?: return flowOf(null)
        return hub.stream(zone.karooType).map {
            (it as? StreamState.Streaming)?.dataPoint?.singleValue?.toInt()
        }
    }

    /** Orologio a 1 s solo per i campi che cambiano col tempo; gli altri un istante fisso. */
    private fun ticks(): Flow<Long> =
        if (metric.kind == Kind.CLOCK || metric.kind == Kind.ARRIVAL) flow {
            while (true) {
                emit(System.currentTimeMillis())
                delay(1_000L)
            }
        } else flowOf(0L)

    private fun render(context: Context, f: FieldFrame): RemoteViews {
        val layout = if (f.showLabel) R.layout.view_big_field else R.layout.view_big_field_bare
        val views = RemoteViews(context.packageName, layout)
        val c = f.colors
        if (c.bgRes != null) views.setInt(R.id.root, "setBackgroundResource", c.bgRes)
        else views.setInt(R.id.root, "setBackgroundColor", c.fill)
        views.setTextColor(R.id.tv_label, c.label)
        views.setTextViewText(R.id.tv_label, f.label)
        views.setTextColor(R.id.tv_value, c.value)
        views.setTextViewText(R.id.tv_value, f.number.toCharSequence())
        return views
    }
}

