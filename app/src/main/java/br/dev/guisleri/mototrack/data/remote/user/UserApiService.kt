package br.dev.guisleri.mototrack.data.remote.user

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH

interface UserApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): UserResponseDTO

    @PATCH("users/me")
    suspend fun updateName(
        @Body request: UpdateUserNameRequestDTO
    ): UserResponseDTO
}
