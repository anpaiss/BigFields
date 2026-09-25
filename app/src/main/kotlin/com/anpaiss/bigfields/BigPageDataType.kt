package com.anpaiss.bigfields

import android.content.Context
import io.hammerhead.karooext.extension.DataTypeImpl
import io.hammerhead.karooext.internal.Emitter
import io.hammerhead.karooext.internal.ViewEmitter
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UpdateGraphicConfig
import io.hammerhead.karooext.models.ViewConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/** Campo pagina [page]: i dati scelti nell'app, in righe uguali (vedi [Pages]). */
class BigPageDataType(
    extension: String,
    private val page: Int,
    private val hub: BigFieldsExtension,
) : DataTypeImpl(extension, Pages.typeId(page)) {

    /** Nessun valore numerico unico da offrire: il campo è solo grafico. */
    override fun startStream(emitter: Emitter<StreamState>) {
        emitter.onNext(StreamState.NotAvailable)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun startView(context: Context, config: ViewConfig, emitter: ViewEmitter) {
        FieldLog.shared?.log("VIEW", "startView ${Pages.typeId(page)} size=${config.viewSize} " +
            "grid=${config.gridSize} preview=${config.preview}")
        val align = config.align()
        val prefs = Prefs(context)
        val dims = cellDims(context.resources)
        val (cellW, cellH) = config.viewSize
        var loggedSize = false
        val stats = RedrawStats(Pages.typeId(page))
        val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        scope.launch {
            emitter.onNext(UpdateGraphicConfig(showHeader = false))

            val look = if (config.preview) prefs.lookFlow().map { it.copy(bannerRoom = false) } else rideLook(prefs, hub)
            val rows: Flow<List<FieldFrame>> = prefs.pageRowsFlow(page).flatMapLatest { ids ->
                val metrics = ids.mapNotNull { Catalog.byTypeId(it) }
                combine(metrics.map {
                    if (config.preview) previewFrames(it, look, hub, align) else liveFrames(it, look, hub, align)
                }) { it.toList() }
            }
            val visibleRows = if (config.preview) rows else rows.whileVisible(hub.visibleDataTypeIds, dataTypeId)

            combine(visibleRows.distinctUntilChanged(), prefs.lookFlow()) { frames, l ->
                PageRenderer.drawing(frames, cellW, cellH, dims, l.style)
            }.distinctUntilChanged().collect { d ->
                if (!loggedSize) {
                    loggedSize = true
                    FieldLog.shared?.log("VIEW", "${Pages.typeId(page)} ${d.frames.size} righe, " +
                        "area numero ${d.w}x${d.h} px, cifre ${d.digitH.toInt()} px")
                }
                val t0 = System.currentTimeMillis()
                try {
                    emitter.updateView(PageRenderer.remoteViews(context.packageName, d))
                } catch (e: Exception) {
                    FieldLog.shared?.error("ERR", "Disegno di ${Pages.typeId(page)} fallito", e)
                }
                stats.record(d.frames.joinToString(" | ") { "'${it.text}'" } + " cifre ${d.digitH.toInt()} px",
                    System.currentTimeMillis() - t0)
            }
        }
        emitter.setCancellable {
            FieldLog.shared?.log("VIEW", "stop ${Pages.typeId(page)}")
            scope.cancel()
        }
    }
}
