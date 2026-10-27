package br.dev.guisleri.mototrack.data.repository

import br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.remote.user.UserApiService

class UserRepository(
    private val userApiService: UserApiService
) {

    suspend fun getCurrentUser(): UserResponseDTO {
        return userApiService.getCurrentUser()
    }

    /**
     * Changes the signed-in user's name and returns the user as the server now has it.
     * The user is read again instead of trusting the answer of the change, so what the
     * app shows is always what is stored.
     */
    suspend fun updateName(name: String): UserResponseDTO {
        userApiService.updateName(UpdateUserNameRequestDTO(name = name))

        return userApiService.getCurrentUser()
    }
}
