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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
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
        val align = config.align()
        val viewKey = "$dataTypeId#${System.nanoTime()}"
        var loggedSize = false
        val stats = RedrawStats(metric.typeId)
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))

            // Anteprima nell'editor: niente fascia, la cella è piccola e il banner lì non c'è
            val input = if (config.preview) previewInput(metric) else rowInput(metric, hub)
            val look = if (config.preview) previewLook(hub) else rideLook(hub)
            val frames = combine(input, look, hub.units) { i, l, units -> frameOf(metric, i, l, units, align) }

            // Altezza delle cifre: la massima per questa cella, poi la più bassa
            // tra i campi della pagina, così le cifre sono alte uguali ovunque.
            val dims = cellDims(context.resources)
            val (cellW, cellH) = config.viewSize
            val drawings = frames.distinctUntilChanged().map { f ->
                val (w, h) = CellGeometry.numberArea(cellW, cellH, dims, f.showLabel, f.bannerBand)
                val own = NumberRenderer.maxDigitHeight(f.template, w, h, f.font)
                if (!config.preview) hub.reportDigitHeight(viewKey, dataTypeId, own)
                Drawing(f, w, h, own)
            }
            val page = if (config.preview) flowOf(emptyList()) else hub.pageDigitHeights()
            val sized = combine(drawings, page) { d, onPage ->
                d.copy(digitH = CellGeometry.pageDigitHeight(d.digitH, onPage).toInt().toFloat())
            }.distinctUntilChanged()
            // Tornando visibile o alla chiusura dell'app il campo ridisegna sempre (vedi Redraw)
            val shown = if (config.preview) sized else sized.whileVisible(hub.visibleDataTypeIds, dataTypeId)
            combine(shown, Redraw.requests) { d, _ -> d }.collect { d ->
                if (!loggedSize) {
                    loggedSize = true
                    FieldLog.shared?.log("VIEW", "${metric.typeId} area numero ${d.w}x${d.h} px, cifre ${d.digitH.toInt()} px")
                }
                val t0 = System.currentTimeMillis()
                try {
                    emitter.updateView(render(context, d))
                } catch (e: Exception) {
                    FieldLog.shared?.error("ERR", "Disegno di ${metric.typeId} fallito", e)
                }
                stats.record("'${d.f.text}' cifre ${d.digitH.toInt()} px", System.currentTimeMillis() - t0)
            }
        }
        emitter.setCancellable {
            FieldLog.shared?.log("VIEW", "stop ${metric.typeId}")
            hub.forgetView(viewKey)
            scope.cancel()
        }
    }

    /** Un frame con l'area del numero e l'altezza delle cifre con cui disegnarlo. */
    private data class Drawing(val f: FieldFrame, val w: Int, val h: Int, val digitH: Float)

    private fun render(context: Context, d: Drawing): RemoteViews {
        val f = d.f
        val views = RemoteViews(context.packageName, layoutFor(f))
        val c = f.colors
        if (c.bgRes != null) views.setInt(R.id.root, "setBackgroundResource", c.bgRes)
        else views.setInt(R.id.root, "setBackgroundColor", c.fill)
        views.setTextColor(R.id.tv_label, c.label)
        views.setTextViewText(R.id.tv_label, f.label)
        views.setImageViewBitmap(R.id.iv_value, NumberRenderer.render(f.text, f.align, d.w, d.h, d.digitH, f.font))
        views.setInt(R.id.iv_value, "setColorFilter", c.value)
        return views
    }
}
