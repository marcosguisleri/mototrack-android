package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthTokenResponseDTO(
    val accessToken: String,
    val refreshToken: String
)