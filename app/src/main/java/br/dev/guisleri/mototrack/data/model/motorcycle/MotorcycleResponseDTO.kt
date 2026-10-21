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
    // Null for bikes registered before the type existed.
    val type: String? = null,
    val owner: MotorcycleOwnerDTO
)

val MotorcycleResponseDTO.motorcycleType: MotorcycleType?
    get() = MotorcycleType.fromApi(type)

@Serializable
data class MotorcycleOwnerDTO(
    val id: Long,
    val name: String,
    val email: String
)
