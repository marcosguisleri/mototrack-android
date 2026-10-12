package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UserResponseDTO(
    val id: Long,
    val name: String,
    val email: String
)
