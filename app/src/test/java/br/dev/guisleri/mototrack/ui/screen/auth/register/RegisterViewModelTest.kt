@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.auth.register

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
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class RegisterViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val api = FakeAuthApi()
    private val viewModel = RegisterViewModel(
        AuthRepository(
            authApiService = api,
            tokenStorage = TokenStorage(UnusedPreferencesStore())
        )
    )

    @Test
    fun eachRequiredFieldProducesFormErrorWithoutCallingApi() {
        val inputs = listOf(
            RegistrationInput("", "rider@example.com", "password", "password"),
            RegistrationInput("Rider", "", "password", "password"),
            RegistrationInput("Rider", "rider@example.com", "", "password"),
            RegistrationInput("Rider", "rider@example.com", "password", "")
        )

        inputs.forEach { input ->
            viewModel.register(
                input.name,
                input.email,
                input.password,
                input.confirmPassword
            )

            assertEquals(
                RegisterUiState.Error("Preencha todos os campos."),
                viewModel.uiState.value
            )
        }

        assertEquals(0, api.registerCalls)
    }

    @Test
    fun invalidEmailProducesFormError() {
        viewModel.register("Rider", "invalid-email", "password", "password")

        assertEquals(
            RegisterUiState.Error("Informe um e-mail válido."),
            viewModel.uiState.value
        )
        assertEquals(0, api.registerCalls)
    }

    @Test
    fun shortPasswordProducesFormError() {
        viewModel.register("Rider", "rider@example.com", "1234567", "1234567")

        assertEquals(
            RegisterUiState.Error("A senha deve ter pelo menos 8 caracteres."),
            viewModel.uiState.value
        )
        assertEquals(0, api.registerCalls)
    }

    @Test
    fun differentPasswordConfirmationProducesFormError() {
        viewModel.register("Rider", "rider@example.com", "password", "different")

        assertEquals(
            RegisterUiState.Error("As senhas não coincidem."),
            viewModel.uiState.value
        )
        assertEquals(0, api.registerCalls)
    }

    @Test
    fun validRegistrationMovesFromLoadingToSuccess() {
        val response = CompletableDeferred<UserResponseDTO>()
        api.onRegister = { response.await() }

        viewModel.register(
            name = "  Rider  ",
            email = "  rider@example.com  ",
            password = "password",
            confirmPassword = "password"
        )

        assertSame(RegisterUiState.Loading, viewModel.uiState.value)
        assertEquals(
            RegisterRequestDTO("Rider", "rider@example.com", "password"),
            api.lastRegisterRequest
        )

        response.complete(UserResponseDTO(1, "Rider", "rider@example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()

        assertSame(RegisterUiState.Success, viewModel.uiState.value)
    }

    @Test
    fun secondRegistrationIsIgnoredWhileLoading() {
        val response = CompletableDeferred<UserResponseDTO>()
        api.onRegister = { response.await() }

        viewModel.register("First", "first@example.com", "password", "password")
        viewModel.register("Second", "second@example.com", "password", "password")

        assertEquals(1, api.registerCalls)
        assertEquals("First", api.lastRegisterRequest?.name)

        response.complete(UserResponseDTO(1, "First", "first@example.com"))
        mainDispatcherRule.dispatcher.scheduler.advanceUntilIdle()
    }

    @Test
    fun conflictProducesRegisteredEmailError() {
        api.onRegister = { throw httpException(409) }

        registerValidInput()

        assertEquals(
            RegisterUiState.Error("Já existe um usuário cadastrado com este e-mail."),
            viewModel.uiState.value
        )
    }

    @Test
    fun badRequestProducesInvalidDataError() {
        api.onRegister = { throw httpException(400) }

        registerValidInput()

        assertEquals(
            RegisterUiState.Error("Verifique os dados informados."),
            viewModel.uiState.value
        )
    }

    @Test
    fun otherHttpFailureProducesGenericRegistrationError() {
        api.onRegister = { throw httpException(500) }

        registerValidInput()

        assertEquals(
            RegisterUiState.Error("Não foi possível realizar o cadastro."),
            viewModel.uiState.value
        )
    }

    @Test
    fun communicationFailureProducesConnectionError() {
        api.onRegister = { throw IOException("Unavailable") }

        registerValidInput()

        assertEquals(
            RegisterUiState.Error("Não foi possível conectar ao servidor."),
            viewModel.uiState.value
        )
    }

    @Test
    fun unexpectedFailureProducesUnexpectedError() {
        api.onRegister = { throw IllegalStateException("Unexpected") }

        registerValidInput()

        assertEquals(
            RegisterUiState.Error("Ocorreu um erro inesperado."),
            viewModel.uiState.value
        )
    }

    @Test
    fun cancellationDoesNotBecomeUiError() {
        api.onRegister = { throw CancellationException("Cancelled") }

        registerValidInput()

        assertSame(RegisterUiState.Loading, viewModel.uiState.value)
    }

    private fun registerValidInput() {
        viewModel.register("Rider", "rider@example.com", "password", "password")
    }

    private fun httpException(code: Int) = HttpException(
        Response.error<UserResponseDTO>(code, "{}".toResponseBody())
    )

    private data class RegistrationInput(
        val name: String,
        val email: String,
        val password: String,
        val confirmPassword: String
    )

    private class FakeAuthApi : AuthApiService {
        var registerCalls = 0
        var lastRegisterRequest: RegisterRequestDTO? = null
        var onRegister: suspend (RegisterRequestDTO) -> UserResponseDTO = {
            UserResponseDTO(1, it.name, it.email)
        }

        override suspend fun register(request: RegisterRequestDTO): UserResponseDTO {
            registerCalls++
            lastRegisterRequest = request
            return onRegister(request)
        }

        override suspend fun login(request: LoginRequestDTO): AuthTokenResponseDTO =
            throw AssertionError("Unexpected login")

        override suspend fun logout(request: RefreshTokenRequestDTO) =
            throw AssertionError("Unexpected logout")

        override suspend fun refresh(request: RefreshTokenRequestDTO): AuthTokenResponseDTO =
            throw AssertionError("Unexpected refresh")
    }

    private class UnusedPreferencesStore : DataStore<Preferences> {
        override val data: Flow<Preferences> = flowOf(emptyPreferences())

        override suspend fun updateData(
            transform: suspend (Preferences) -> Preferences
        ): Preferences = throw AssertionError("Registration must not write tokens")
    }
}

class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
