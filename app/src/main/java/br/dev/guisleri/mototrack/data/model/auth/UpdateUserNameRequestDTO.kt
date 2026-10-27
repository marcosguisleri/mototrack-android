package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserNameRequestDTO(
    val name: String
)
