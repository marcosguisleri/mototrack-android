@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.auth.UserResponseDTO
import br.dev.guisleri.mototrack.data.remote.user.UserApiService
import br.dev.guisleri.mototrack.data.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import java.io.IOException

class ProfileViewModelTest {

    private val dispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeUserApi : UserApiService {
        var calls = 0
        var onGet: suspend () -> UserResponseDTO = { UserResponseDTO(1, "Marcos", "m@example.com") }

        override suspend fun getCurrentUser(): UserResponseDTO {
            calls++
            return onGet()
        }
    }

    @Test
    fun loadsTheCurrentUserOnCreation() = runTest(dispatcher) {
        val viewModel = ProfileViewModel(UserRepository(FakeUserApi()))

        val state = viewModel.uiState.value
        assertEquals("Marcos", state.user?.name)
        assertFalse(state.isLoading)
        assertNull(state.errorMessage)
    }

    @Test
    fun failureShowsAMessageAndRetryRecovers() = runTest(dispatcher) {
        val api = FakeUserApi().apply { onGet = { throw IOException("offline") } }
        val viewModel = ProfileViewModel(UserRepository(api))

        assertEquals("Não foi possível comunicar com o servidor.", viewModel.uiState.value.errorMessage)
        assertNull(viewModel.uiState.value.user)

        api.onGet = { UserResponseDTO(1, "Marcos", "m@example.com") }
        viewModel.loadUser()

        assertEquals("m@example.com", viewModel.uiState.value.user?.email)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
