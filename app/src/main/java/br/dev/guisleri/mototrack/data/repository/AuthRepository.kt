package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RegisterRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.network.AuthApiService
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

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

    suspend fun register(
        name: String,
        email: String,
        password: String
    ): UserResponseDTO {
        val request = RegisterRequestDTO(
            name = name,
            email = email,
            password = password
        )

        return authApiService.register(request)
    }

    suspend fun logout() {
        val refreshToken = withContext(NonCancellable) {
            tokenStorage.clearTokensAndGetRefreshToken()
        }
        currentCoroutineContext().ensureActive()

        if (!refreshToken.isNullOrBlank()) {
            authApiService.logout(
                RefreshTokenRequestDTO(
                    refreshToken = refreshToken
                )
            )
        }
    }
}
