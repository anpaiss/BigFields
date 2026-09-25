# Handover — BigFields

Aggiornato: 2026-09-25. Ambiente, JAVA_HOME e convenzioni in `..\HANDOVER.md`.

## Perché esiste

Andrea senza occhiali fa fatica a leggere i campi nativi del Karoo in sella; col Wahoo leggeva meglio. Idea: un'estensione che ridisegna i dati nativi con il numero grande quanto la cella, negli stili di AxsRatio, da usare in pagine a due campi. Fase 1 solo dati nativi; i dati delle altre estensioni (quelle che pubblicano un numero) sono un'aggiunta successiva.

Cose escluse per limiti dell'SDK karoo-ext 1.1.9 (verificato leggendo il jar): un'estensione non può creare pagine né cambiare il layout del Karoo; il comando `ZoomPage` agisce solo su mappa e grafici. Quindi niente "più/meno campi al volo" come Wahoo.

## Com'è fatta (build 25/09, mai installata)

- Un campo Karoo per ogni voce di `Catalog.kt` (32), tutti grafici, id estensione `big-fields`. `extension_info.xml` e `strings.xml` sono generati dal catalogo; `CatalogTest` verifica che coincidano.
- `ValueFormatter`: da valore grezzo a testo, unità dal `UserProfile` (metrico/imperiale). Ora del giorno dall'orologio di sistema; arrivo = adesso + `TIME_TO_DESTINATION` (in ms, verificato su BetterEta).
- `NumberPadding`: il testo si adatta alla cella (autosize XML), ma viene completato con caratteri trasparenti fino a un template ("88.8", "188", "8:88:88"…) così la grandezza non salta quando cambia il numero di cifre. Il lato del riempimento segue l'allineamento scelto nell'editor Karoo.
- Due layout (con e senza didascalia) perché sul Karoo `setViewVisibility` e `setTextViewTextSize` non vengono applicati (lezione di BetterEta 04/09). Numero in `sans-serif-condensed` grassetto: a parità di larghezza le cifre vengono più alte.
- `FieldStyle`: Karoo dark (default), Karoo light, Vivid, Outline, Pastel, Ink; colori di AxsRatio; zona HR/potenza per i campi `hr`, `power`, `power-3s`. `FieldStyleTest` verifica il contrasto WCAG ≥ 4,5:1 per ogni stile e zona.
- Ridisegno solo quando cambia il frame e solo se il campo è sulla pagina visibile (`whileVisible`, copiata da BetterEta).
- App: stile, didascalia sì/no, anteprima (frequenza cardiaca in zona 3), elenco campi.

Test: 29, verdi.

## Pagine di Andrea (25/09) = scenario della prima prova

Tutte coperte dai 32 campi attuali:
- "Fatto": `distance` + `ascent`.
- "Da fare": `dist-to-dest` + `climb-left`.
- "ETA nativa" (confronto con BetterEta): `arrival` + `time-to-dest`. Attenzione: `arrival` è calcolato come adesso + `TIME_TO_DESTINATION`, non letto da `TIME_OF_ARRIVAL` (unità ignota). Alla prima prova confrontarlo con il campo nativo "ora di arrivo": se differisce, passare a `TIME_OF_ARRIVAL`.
- Pagina col grafico altimetrico nativo: `altitude`, `temperature`, `grade` accanto al grafico.

## Prima prova sul Karoo — cosa guardare

Log: `adb pull /sdcard/Android/data/com.anpaiss.bigfields/files/big_fields.log` (da PowerShell, vedi memoria su Git Bash).

1. **Misura del problema di partenza**: righe `VIEW startView ... size=(w, h) grid=(c, r) nativeTextSize=Nsp` per pagine da 2, 3 e 4 campi, e `SYS ... density`. `nativeTextSize` è il carattere che il Karoo userebbe per un campo nativo in quella cella: confrontarlo con l'altezza della cella dà quanto spazio sprecano i nativi. Più due screenshot della stessa pagina, nativa e BigFields (`adb exec-out screencap -p > x.png`).
2. **Unità dei dati da confermare** (righe `DATA <campo> <- <tipo> primo stato: valori=...`): durate (`RIDE_TIME`, `ELAPSED_TIME`, `ELAPSED_TIME_LAP`: assunte in ms), `TEMPERATURE` (°C), `ELEVATION_GRADE` (%), `DISTANCE_TO_TOP` e `ELEVATION_TO_TOP` (m), `POWER_LAP` (assunta potenza media del giro), `PRESSURE_ELEVATION_CORRECTION` come altitudine, `HR_ZONE`/`POWER_ZONE` (assunte 1..5/1..7).
3. **Resa**: il riempimento trasparente (span colorato dentro RemoteViews) deve funzionare; se il Karoo lo ignora si vedrebbero cifre "8" in più a sinistra. Piano B: disegnare il numero come bitmap.
4. **Anteprima nell'editor profili** (`preview=true`): valore d'esempio, zona 3.
5. **Banner "next cue" in basso**: l'SDK non ne dà presenza né altezza. Opzione "Leave room for the navigation banner" (default acceso): SOLO mentre la navigazione è attiva (`OnNavigationState` ≠ Idle, log `NAV`) OGNI campo lascia in fondo una fascia di `@dimen/banner_band` = 100px (stima da BetterEta), così tutti i numeri della pagina restano uguali (richiesta di Andrea: niente campo grande sopra e piccolo sotto). Da misurare: altezza vera del banner (screenshot in navigazione) e **se il banner resta per tutta la navigazione o compare solo vicino alle svolte**; nel secondo caso usare `DISTANCE_TO_NEXT_TURN` sotto soglia come indizio. Anteprima della pagina con banner simulato nell'app (celle stimate 478x376 px).
6. Righe `PAGE Pagina visibile: ...`: griglia di ogni pagina, utile per la misura del punto 1.

## Idee per dopo

- Dati delle altre estensioni: leggere l'elenco dalle estensioni installate (servizi con action `io.hammerhead.karooext.KAROO_EXTENSION` + meta-data `EXTENSION_INFO`), stream con id `TYPE_EXT::<ext>::<tipo>`, 4 campi "slot" configurabili nell'app. Funziona solo per le estensioni che implementano `startStream`.
- Campi e-bike (`LEV_*`): utili ad Andrea, unità ignote → prima leggerle dal log.
- Rapporto del cambio (multi-valore, come AxsRatio).
