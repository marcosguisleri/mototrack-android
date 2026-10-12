package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequestDTO(
    val name: String,
    val email: String,
    val password: String
)
