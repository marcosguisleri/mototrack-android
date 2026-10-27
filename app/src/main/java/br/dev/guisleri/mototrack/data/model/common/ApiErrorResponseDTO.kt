package br.dev.guisleri.mototrack.data.model.common

import kotlinx.serialization.Serializable

@Serializable
data class ApiErrorResponseDTO(
    val status: Int,
    val error: String,
    val message: String,
    val errors: Map<String, String> = emptyMap()
)