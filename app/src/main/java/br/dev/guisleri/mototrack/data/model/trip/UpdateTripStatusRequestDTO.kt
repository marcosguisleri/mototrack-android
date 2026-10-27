package br.dev.guisleri.mototrack.data.model.trip

import kotlinx.serialization.Serializable

@Serializable
data class UpdateTripStatusRequestDTO(
    val status: String,
    val updateTripDateToToday: Boolean = false
)
