package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import br.dev.guisleri.mototrack.data.network.AuthApiService

class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenStorage: TokenStorage
) {

    suspend fun login(
        email: String,
        password: String
    ) {
        val request = LoginRequestDTO(
            email = email,
            password = password
        )

        val tokens = authApiService.login(request)

        tokenStorage.saveTokens(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken
        )
    }

    suspend fun logout() {
        val refreshToken = tokenStorage.getRefreshToken()

        try {
            if (!refreshToken.isNullOrBlank()) {
                authApiService.logout(
                    RefreshTokenRequestDTO(
                        refreshToken = refreshToken
                    )
                )
            }
        } finally {
            tokenStorage.clearTokens()
        }
    }
}