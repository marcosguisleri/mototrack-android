@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.profile.email

import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.ChangeEmailRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ConfirmEmailChangeRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ForgotPasswordRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ResetPasswordRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
import br.dev.guisleri.mototrack.data.network.AccountRecoveryApiService
import br.dev.guisleri.mototrack.data.remote.user.UserApiService
import br.dev.guisleri.mototrack.data.repository.AccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class ChangeEmailViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeUserApi : UserApiService {
        val requests = mutableListOf<ChangeEmailRequestDTO>()
        val confirmations = mutableListOf<ConfirmEmailChangeRequestDTO>()
        var onRequest: suspend () -> Unit = {}
        var onConfirm: suspend () -> AuthTokenResponseDTO = { AuthTokenResponseDTO("new-access", "new-refresh") }

        override suspend fun requestEmailChange(request: ChangeEmailRequestDTO) {
            requests += request
            onRequest()
        }

        override suspend fun confirmEmailChange(request: ConfirmEmailChangeRequestDTO): AuthTokenResponseDTO {
            confirmations += request
            return onConfirm()
        }

        override suspend fun getCurrentUser() = error("unused")
        override suspend fun updateName(request: UpdateUserNameRequestDTO) = error("unused")
    }

    private object NoRecoveryApi : AccountRecoveryApiService {
        override suspend fun forgotPassword(request: ForgotPasswordRequestDTO) = error("unused")
        override suspend fun resetPassword(request: ResetPasswordRequestDTO) = error("unused")
    }

    private val savedTokens = mutableListOf<Pair<String, String>>()

    private fun viewModel(api: FakeUserApi = FakeUserApi(), currentEmail: String? = "marcos@example.com") =
        ChangeEmailViewModel(
            AccountRepository(NoRecoveryApi, api) { access, refresh -> savedTokens += access to refresh },
            currentEmail
        )

    private fun badRequest(message: String, code: Int = 400) = HttpException(
        Response.error<Any>(
            code,
            """{"status":$code,"error":"x","message":"$message"}""".toResponseBody()
        )
    )

    @Test
    fun theFieldsAreCheckedBeforeAskingTheServer() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = viewModel(api)

        viewModel.requestCode("nope", "secret123")
        assertEquals("Informe um e-mail válido.", viewModel.uiState.value.errorMessage)

        viewModel.requestCode("MARCOS@example.com", "secret123")
        assertEquals("O novo e-mail é igual ao atual.", viewModel.uiState.value.errorMessage)

        viewModel.requestCode("novo@example.com", "")
        assertEquals("Informe a sua senha atual.", viewModel.uiState.value.errorMessage)

        assertTrue(api.requests.isEmpty())
    }

    @Test
    fun askingForTheCodeMovesOnToConfirming() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = viewModel(api)

        viewModel.requestCode("  novo@example.com ", "secret123")

        val state = viewModel.uiState.value
        assertEquals(ChangeEmailStep.CONFIRM, state.step)
        assertEquals("novo@example.com", state.newEmail)
        assertEquals(listOf(ChangeEmailRequestDTO("novo@example.com", "secret123")), api.requests)
        assertEquals(60, state.resendSecondsLeft)
    }

    @Test
    fun theServersMessageIsShownForAWrongPasswordOrATakenAddress() = runTest(dispatcher) {
        val api = FakeUserApi().apply { onRequest = { throw badRequest("Senha atual incorreta.") } }
        val viewModel = viewModel(api)

        viewModel.requestCode("novo@example.com", "wrong")

        assertEquals("Senha atual incorreta.", viewModel.uiState.value.errorMessage)
        assertEquals(ChangeEmailStep.REQUEST, viewModel.uiState.value.step)

        api.onRequest = { throw badRequest("Já existe um usuário cadastrado com este e-mail.", 409) }
        viewModel.requestCode("outro@example.com", "secret123")
        assertEquals(
            "Já existe um usuário cadastrado com este e-mail.",
            viewModel.uiState.value.errorMessage
        )
    }

    @Test
    fun noConnectionIsSaidPlainly() = runTest(dispatcher) {
        val api = FakeUserApi().apply { onRequest = { throw IOException("offline") } }
        val viewModel = viewModel(api)

        viewModel.requestCode("novo@example.com", "secret123")

        assertEquals("Não foi possível conectar ao servidor.", viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aCodeAskedAgainReusesThePasswordAfterTheWait() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = viewModel(api)
        viewModel.requestCode("novo@example.com", "secret123")

        viewModel.resendCode()
        assertEquals(1, api.requests.size)

        testScheduler.advanceTimeBy(60_000)
        testScheduler.runCurrent()
        viewModel.resendCode()

        assertEquals(2, api.requests.size)
        assertEquals(ChangeEmailRequestDTO("novo@example.com", "secret123"), api.requests.last())
    }

    @Test
    fun confirmingKeepsTheNewSessionAndFinishes() = runTest(dispatcher) {
        val api = FakeUserApi()
        val viewModel = viewModel(api)
        viewModel.requestCode("novo@example.com", "secret123")

        viewModel.confirm(" 654321 ")

        assertEquals(listOf(ConfirmEmailChangeRequestDTO("654321")), api.confirmations)
        assertEquals(listOf("new-access" to "new-refresh"), savedTokens)
        assertTrue(viewModel.uiState.value.isChanged)
    }

    @Test
    fun aBadCodeIsRefusedLocallyOrByTheServerAndNothingIsSaved() = runTest(dispatcher) {
        val api = FakeUserApi().apply { onConfirm = { throw badRequest("Código inválido ou expirado.") } }
        val viewModel = viewModel(api)
        viewModel.requestCode("novo@example.com", "secret123")

        viewModel.confirm("12")
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("código"))
        assertTrue(api.confirmations.isEmpty())

        viewModel.confirm("000000")
        assertEquals("Código inválido ou expirado.", viewModel.uiState.value.errorMessage)
        assertFalse(viewModel.uiState.value.isChanged)
        assertTrue(savedTokens.isEmpty())
    }

    @Test
    fun correctingTheDataGoesBackToTheFirstStep() = runTest(dispatcher) {
        val viewModel = viewModel()
        viewModel.requestCode("novo@example.com", "secret123")

        viewModel.editRequest()

        assertEquals(ChangeEmailStep.REQUEST, viewModel.uiState.value.step)
        assertNull(viewModel.uiState.value.errorMessage)
    }
}
