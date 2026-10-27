package br.dev.guisleri.mototrack.data.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class ForgotPasswordRequestDTO(
    val email: String
)

@Serializable
data class ResetPasswordRequestDTO(
    val email: String,
    val code: String,
    val newPassword: String
)

@Serializable
data class ChangeEmailRequestDTO(
    val newEmail: String,
    val currentPassword: String
)

@Serializable
data class ConfirmEmailChangeRequestDTO(
    val code: String
)
