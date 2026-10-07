package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class RefreshTokenRequestDTO(
    val refreshToken: String
)