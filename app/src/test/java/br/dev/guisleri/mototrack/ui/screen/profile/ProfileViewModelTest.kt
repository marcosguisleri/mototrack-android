@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.profile

import br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
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
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertTrue
import retrofit2.HttpException
import retrofit2.Response
import kotlinx.coroutines.flow.first
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
        var stored = UserResponseDTO(1, "Marcos", "m@example.com")
        var onGet: suspend () -> UserResponseDTO = { stored }
        var onPatch: suspend (UpdateUserNameRequestDTO) -> UserResponseDTO = { request ->
            stored = stored.copy(name = request.name)
            stored
        }
        val patches = mutableListOf<UpdateUserNameRequestDTO>()

        override suspend fun getCurrentUser(): UserResponseDTO {
            calls++
            return onGet()
        }

        override suspend fun updateName(request: UpdateUserNameRequestDTO): UserResponseDTO {
            patches += request
            return onPatch(request)
        }

        override suspend fun requestEmailChange(
            request: br.dev.guisleri.mototrack.data.model.auth.ChangeEmailRequestDTO
        ) = throw AssertionError("Unexpected e-mail change")

        override suspend fun confirmEmailChange(
            request: br.dev.guisleri.mototrack.data.model.auth.ConfirmEmailChangeRequestDTO
        ): br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO =
            throw AssertionError("Unexpected e-mail change")
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

    @Test
    fun savingTheNameShowsItAndTellsTheOthers() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName("  Marcos Silva  ")

        val state = viewModel.uiState.value
        assertEquals("Marcos Silva", state.user?.name)
        assertFalse(state.isEditingName)
        assertFalse(state.isSavingName)
        assertEquals(listOf(UpdateUserNameRequestDTO("Marcos Silva")), api.patches)
        assertEquals("Marcos Silva", viewModel.nameUpdated.first())
    }

    @Test
    fun theNameShownIsWhatTheServerStoresEvenIfItsAnswerIsStale() = runTest(dispatcher) {
        // The change answers with the old name; the user read afterwards has the new one.
        val api = FakeUserApi()
        api.onPatch = { request ->
            val before = api.stored
            api.stored = before.copy(name = request.name)
            before
        }
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName("Marcos Silva")

        assertEquals("Marcos Silva", viewModel.uiState.value.user?.name)
    }

    @Test
    fun aBlankNameIsRefusedWithoutAskingTheServer() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName("   ")

        assertEquals("Informe seu nome.", viewModel.uiState.value.nameErrorMessage)
        assertTrue(viewModel.uiState.value.isEditingName)
        assertTrue(api.patches.isEmpty())
    }

    @Test
    fun theSameNameJustClosesTheDialog() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName(" Marcos ")

        assertFalse(viewModel.uiState.value.isEditingName)
        assertTrue(api.patches.isEmpty())
    }

    @Test
    fun aServerRefusalShowsItsMessageAndKeepsTheDialogOpen() = runTest(dispatcher) {
        val api = FakeUserApi()
        api.onPatch = {
            throw HttpException(
                Response.error<Any>(
                    400,
                    """{"status":400,"error":"Bad Request","message":"O nome deve ter no máximo 100 caracteres."}"""
                        .toResponseBody()
                )
            )
        }
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName("Outro nome")

        val state = viewModel.uiState.value
        assertEquals("O nome deve ter no máximo 100 caracteres.", state.nameErrorMessage)
        assertTrue(state.isEditingName)
        assertFalse(state.isSavingName)
        assertEquals("Marcos", state.user?.name)
    }

    @Test
    fun noConnectionKeepsTheOldNameAndSaysSo() = runTest(dispatcher) {
        val api = FakeUserApi()
        api.onPatch = { throw IOException("offline") }
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()
        viewModel.saveName("Outro nome")

        assertEquals(
            "Não foi possível comunicar com o servidor.",
            viewModel.uiState.value.nameErrorMessage
        )
        assertEquals("Marcos", viewModel.uiState.value.user?.name)
    }

    @Test
    fun theNameCannotBeEditedBeforeTheUserLoads() = runTest(dispatcher) {
        val api = FakeUserApi().apply { onGet = { throw IOException("offline") } }
        val viewModel = ProfileViewModel(UserRepository(api))

        viewModel.openNameEditor()

        assertFalse(viewModel.uiState.value.isEditingName)
    }
}
