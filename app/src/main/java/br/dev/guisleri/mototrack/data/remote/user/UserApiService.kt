package br.dev.guisleri.mototrack.data.remote.user

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.ChangeEmailRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ConfirmEmailChangeRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

interface UserApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): UserResponseDTO

    @POST("users/me/email-change")
    suspend fun requestEmailChange(
        @Body request: ChangeEmailRequestDTO
    )

    @POST("users/me/email-change/confirm")
    suspend fun confirmEmailChange(
        @Body request: ConfirmEmailChangeRequestDTO
    ): AuthTokenResponseDTO

    @PATCH("users/me")
    suspend fun updateName(
        @Body request: UpdateUserNameRequestDTO
    ): UserResponseDTO
}
