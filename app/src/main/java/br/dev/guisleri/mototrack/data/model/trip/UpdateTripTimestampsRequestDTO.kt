package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

/** Both fields are always sent: a null clears the timestamp. */
@Serializable
data class UpdateTripTimestampsRequestDTO(
    val startedAt: String?,
    val completedAt: String?
)
