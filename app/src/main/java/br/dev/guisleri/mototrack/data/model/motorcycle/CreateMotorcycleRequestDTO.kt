package br.dev.guisleri.mototrack.data.model.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class CreateMotorcycleRequestDTO(
    val brand: String,
    val model: String,
    val color: String,
    val year: Int,
    val engineCapacity: Int,
    val type: String
)
