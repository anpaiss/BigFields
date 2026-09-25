package com.anpaiss.bigfields

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map

/**
 * Niente ridisegni mentre il campo non è sulla pagina visibile; al ritorno
 * sulla pagina l'ultimo valore esce subito. Copiata da BetterEta.
 *
 * Aperta per scelta: [visibleDataTypeIds] null = pagina non nota, si disegna
 * tutto. Un filtro sbagliato congelerebbe il campo per tutto il giro.
 */
fun <T> Flow<T>.whileVisible(
    visibleDataTypeIds: StateFlow<Set<String>?>,
    dataTypeId: String,
): Flow<T> = combine(visibleDataTypeIds) { value, visible ->
    value to (visible == null || dataTypeId in visible)
}.filter { it.second }.map { it.first }
