package br.dev.guisleri.mototrack.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RegisterRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.network.AuthApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Test

class AuthRepositoryRegisterTest {

    @Test
    fun registerForwardsExactRequestAndDoesNotAccessTokenStorage() = runTest {
        val api = RecordingAuthApi()
        val preferencesStore = RecordingPreferencesStore()
        val tokenStorage = TokenStorage(preferencesStore)
        val readsAfterConstruction = preferencesStore.dataReads
        val repository = AuthRepository(api, tokenStorage)

        val response = repository.register(
            name = "Rider",
            email = "rider@example.com",
            password = "password"
        )

        assertEquals(
            RegisterRequestDTO("Rider", "rider@example.com", "password"),
            api.request
        )
        assertSame(api.response, response)
        assertEquals(readsAfterConstruction, preferencesStore.dataReads)
        assertEquals(0, preferencesStore.updates)
    }

    private class RecordingAuthApi : AuthApiService {
        val response = UserResponseDTO(1, "Rider", "rider@example.com")
        var request: RegisterRequestDTO? = null

        override suspend fun register(request: RegisterRequestDTO): UserResponseDTO {
            this.request = request
            return response
        }

        override suspend fun login(request: LoginRequestDTO): AuthTokenResponseDTO =
            throw AssertionError("Unexpected login")

        override suspend fun logout(request: RefreshTokenRequestDTO) =
            throw AssertionError("Unexpected logout")

        override suspend fun refresh(request: RefreshTokenRequestDTO): AuthTokenResponseDTO =
            throw AssertionError("Unexpected refresh")
    }

    private class RecordingPreferencesStore : DataStore<Preferences> {
        private val preferences = MutableStateFlow<Preferences>(emptyPreferences())
        var dataReads = 0
        var updates = 0

        override val data: Flow<Preferences>
            get() {
                dataReads++
                return preferences
            }

        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences {
            updates++
            return transform(preferences.value).also {
                preferences.value = it
            }
        }
    }
}
