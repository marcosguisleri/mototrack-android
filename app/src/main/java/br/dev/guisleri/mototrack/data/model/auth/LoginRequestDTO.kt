package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDTO(
    val email: String,
    val password: String
)