package br.dev.guisleri.mototrack.data.network

import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDTO
    ): AuthTokenResponseDTO

    @POST("auth/logout")
    suspend fun logout(
        @Body request: RefreshTokenRequestDTO
    )

    @POST("auth/refresh")
    suspend fun refresh(
        @Body request: RefreshTokenRequestDTO
    ): AuthTokenResponseDTO

}