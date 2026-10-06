package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.network.AuthApiService

class AuthRepository(
    private val authApiService: AuthApiService
) {

    suspend fun login(
        email: String,
        password: String
    ): AuthTokenResponseDTO {
        val request = LoginRequestDTO(
            email = email,
            password = password
        )

        return authApiService.login(request)
    }
}