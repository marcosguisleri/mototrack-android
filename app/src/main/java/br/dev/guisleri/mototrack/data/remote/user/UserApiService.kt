package br.dev.guisleri.mototrack.data.remote.user

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import retrofit2.http.GET

interface UserApiService {

    @GET("users/me")
    suspend fun getCurrentUser(): UserResponseDTO
}
