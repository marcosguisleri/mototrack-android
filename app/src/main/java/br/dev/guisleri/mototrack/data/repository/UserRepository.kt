package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.remote.user.UserApiService

class UserRepository(
    private val userApiService: UserApiService
) {

    suspend fun getCurrentUser(): UserResponseDTO {
        return userApiService.getCurrentUser()
    }
}
