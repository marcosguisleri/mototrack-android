package br.dev.guisleri.mototrack.data.model.motorcycle

import kotlinx.serialization.Serializable

@Serializable
data class MotorcycleResponseDTO(
    val id: Long,
    val brand: String,
    val model: String,
    val color: String,
    val year: Int,
    val engineCapacity: Int,
    val owner: MotorcycleOwnerDTO
)

@Serializable
data class MotorcycleOwnerDTO(
    val id: Long,
    val name: String,
    val email: String
)
