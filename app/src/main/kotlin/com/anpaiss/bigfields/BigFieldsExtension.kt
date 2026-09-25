package com.anpaiss.bigfields

import io.hammerhead.karooext.KarooSystemService
import io.hammerhead.karooext.extension.KarooExtension
import io.hammerhead.karooext.models.ActiveRidePage
import io.hammerhead.karooext.models.OnNavigationState
import io.hammerhead.karooext.models.OnStreamState
import io.hammerhead.karooext.models.StreamState
import io.hammerhead.karooext.models.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first

/**
 * L'estensione: un campo grafico per ogni voce del [Catalog]. Tiene la
 * connessione al Karoo e le poche cose che servono a tutti i campi: unità del
 * profilo utente e campi della pagina visibile.
 */
class BigFieldsExtension : KarooExtension("big-fields", "1") {

    private lateinit var karooSystem: KarooSystemService
    private lateinit var log: FieldLog
    private val consumerIds = mutableListOf<String>()

    private val connected = MutableStateFlow(false)
    private val _units = MutableStateFlow(Units())
    private val _visible = MutableStateFlow<Set<String>?>(null)
    private val _navigating = MutableStateFlow(false)

    val units: StateFlow<Units> = _units

    // Id completi dei dati sulla pagina che l'utente sta guardando.
    // null = non ancora noto: tutti i campi disegnano (vedi whileVisible).
    val visibleDataTypeIds: StateFlow<Set<String>?> = _visible

    // Navigazione attiva (route o destinazione). L'SDK non dice quando il Karoo
    // mostra il banner "next cue" in fondo alla pagina; il banner esiste solo
    // in navigazione, quindi la fascia per il banner vale solo qui.
    val navigating: StateFlow<Boolean> = _navigating

    override val types by lazy { Catalog.all.map { BigFieldDataType(extension, it, this) } }

    override fun onCreate() {
        super.onCreate()
        log = FieldLog(applicationContext)
        FieldLog.shared = log
        val dm = resources.displayMetrics
        log.log("SYS", "=== BigFields avviato: schermo ${dm.widthPixels}x${dm.heightPixels} px, " +
            "density ${dm.density}, fontScale ${resources.configuration.fontScale}, campi ${Catalog.all.size}")

        karooSystem = KarooSystemService(applicationContext)
        karooSystem.connect { ok ->
            log.log("SYS", "Karoo connesso: $ok")
            if (!ok) return@connect

            consumerIds += karooSystem.addConsumer(
                UserProfile.Params, { _ -> }, {}
            ) { profile: UserProfile ->
                val u = profile.preferredUnit
                val next = Units(
                    imperialDistance = u.distance == UserProfile.PreferredUnit.UnitType.IMPERIAL,
                    imperialElevation = u.elevation == UserProfile.PreferredUnit.UnitType.IMPERIAL,
                    imperialTemperature = u.temperature == UserProfile.PreferredUnit.UnitType.IMPERIAL,
                )
                if (next != _units.value) log.log("SYS", "Unità: $next")
                _units.value = next
            }

            consumerIds += karooSystem.addConsumer(
                OnNavigationState.Params, { _ -> }, {}
            ) { event: OnNavigationState ->
                val nav = event.state !is OnNavigationState.NavigationState.Idle
                if (nav != _navigating.value) log.log("NAV", "Navigazione ${if (nav) "attiva" else "ferma"}: ${event.state::class.simpleName}")
                _navigating.value = nav
            }

            // Anche la misura della prima prova: com'è fatta ogni pagina (griglia 60 = altezza intera)
            consumerIds += karooSystem.addConsumer(
                ActiveRidePage.Params, { _ -> }, {}
            ) { event: ActiveRidePage ->
                val ids = event.page.elements.map { it.dataTypeId }.toSet()
                if (ids != _visible.value) {
                    log.log("PAGE", "Pagina visibile: " + event.page.elements.joinToString {
                        "${it.dataTypeId.removePrefix("TYPE_")} ${it.gridSize.first}x${it.gridSize.second}"
                    })
                }
                _visible.value = ids
            }

            connected.value = true
        }
    }

    /** Stato di un dato del Karoo, come Flow; si iscrive quando la connessione è pronta. */
    fun stream(karooType: String): Flow<StreamState> = callbackFlow {
        connected.first { it }
        val id = karooSystem.addConsumer(
            OnStreamState.StartStreaming(karooType),
            { err -> log.log("ERR", "Stream $karooType: $err") },
            { close() },
        ) { event: OnStreamState -> trySend(event.state) }
        awaitClose { karooSystem.removeConsumer(id) }
    }

    override fun onDestroy() {
        log.log("SYS", "=== BigFields fermato")
        consumerIds.forEach { karooSystem.removeConsumer(it) }
        consumerIds.clear()
        karooSystem.disconnect()
        super.onDestroy()
    }
}
