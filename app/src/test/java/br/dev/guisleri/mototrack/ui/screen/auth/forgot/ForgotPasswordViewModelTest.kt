@file:OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)

package br.dev.guisleri.mototrack.ui.screen.auth.forgot

import br.dev.guisleri.mototrack.data.model.auth.ForgotPasswordRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.ResetPasswordRequestDTO
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

class ForgotPasswordViewModelTest {

    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private class FakeRecoveryApi : AccountRecoveryApiService {
        val forgot = mutableListOf<ForgotPasswordRequestDTO>()
        val resets = mutableListOf<ResetPasswordRequestDTO>()
        var onForgot: suspend () -> Unit = {}
        var onReset: suspend () -> Unit = {}

        override suspend fun forgotPassword(request: ForgotPasswordRequestDTO) {
            forgot += request
            onForgot()
        }

        override suspend fun resetPassword(request: ResetPasswordRequestDTO) {
            resets += request
            onReset()
        }
    }

    private fun viewModel(api: FakeRecoveryApi = FakeRecoveryApi()) =
        ForgotPasswordViewModel(
            AccountRepository(api, NoUserApi, saveTokens = { _, _ -> })
        )

    @Test
    fun anInvalidEmailIsRefusedWithoutAskingTheServer() = runTest(dispatcher) {
        val api = FakeRecoveryApi()
        val viewModel = viewModel(api)

        viewModel.sendCode("not-an-email")

        assertEquals("Informe um e-mail válido.", viewModel.uiState.value.errorMessage)
        assertEquals(ForgotPasswordStep.EMAIL, viewModel.uiState.value.step)
        assertTrue(api.forgot.isEmpty())
    }

    @Test
    fun sendingTheCodeMovesOnAndStartsTheWaitToAskAgain() = runTest(dispatcher) {
        val api = FakeRecoveryApi()
        val viewModel = viewModel(api)

        viewModel.sendCode("  marcos@example.com ")

        val state = viewModel.uiState.value
        assertEquals(ForgotPasswordStep.CODE, state.step)
        assertEquals("marcos@example.com", state.email)
        assertEquals(listOf(ForgotPasswordRequestDTO("marcos@example.com")), api.forgot)
        assertEquals(60, state.resendSecondsLeft)

        testScheduler.advanceTimeBy(30_000)
        testScheduler.runCurrent()
        assertEquals(30, viewModel.uiState.value.resendSecondsLeft)

        testScheduler.advanceTimeBy(30_000)
        testScheduler.runCurrent()
        assertEquals(0, viewModel.uiState.value.resendSecondsLeft)
    }

    @Test
    fun theCodeCannotBeAskedAgainDuringTheWait() = runTest(dispatcher) {
        val api = FakeRecoveryApi()
        val viewModel = viewModel(api)

        viewModel.sendCode("marcos@example.com")
        viewModel.resendCode()

        assertEquals(1, api.forgot.size)

        testScheduler.advanceTimeBy(60_000)
        testScheduler.runCurrent()
        viewModel.resendCode()

        assertEquals(2, api.forgot.size)
    }

    @Test
    fun noConnectionKeepsTheRiderOnTheFirstStep() = runTest(dispatcher) {
        val api = FakeRecoveryApi().apply { onForgot = { throw IOException("offline") } }
        val viewModel = viewModel(api)

        viewModel.sendCode("marcos@example.com")

        assertEquals("Não foi possível conectar ao servidor.", viewModel.uiState.value.errorMessage)
        assertEquals(ForgotPasswordStep.EMAIL, viewModel.uiState.value.step)
        assertFalse(viewModel.uiState.value.isLoading)
    }

    @Test
    fun changingTheEmailGoesBackWithoutLosingIt() = runTest(dispatcher) {
        val viewModel = viewModel()

        viewModel.sendCode("marcos@example.com")
        viewModel.changeEmail()

        val state = viewModel.uiState.value
        assertEquals(ForgotPasswordStep.EMAIL, state.step)
        assertEquals("marcos@example.com", state.email)
        assertEquals(0, state.resendSecondsLeft)
    }

    @Test
    fun resettingValidatesTheFieldsBeforeAskingTheServer() = runTest(dispatcher) {
        val api = FakeRecoveryApi()
        val viewModel = viewModel(api)
        viewModel.sendCode("marcos@example.com")

        viewModel.resetPassword("12", "new-secret-1", "new-secret-1")
        assertTrue(viewModel.uiState.value.errorMessage!!.contains("código"))

        viewModel.resetPassword("123456", "short", "short")
        assertEquals("A senha deve ter pelo menos 8 caracteres.", viewModel.uiState.value.errorMessage)

        viewModel.resetPassword("123456", "new-secret-1", "different-1")
        assertEquals("As senhas não coincidem.", viewModel.uiState.value.errorMessage)

        assertTrue(api.resets.isEmpty())
    }

    @Test
    fun aGoodResetSendsTheEmailTheCodeAndThePassword() = runTest(dispatcher) {
        val api = FakeRecoveryApi()
        val viewModel = viewModel(api)
        viewModel.sendCode("marcos@example.com")

        viewModel.resetPassword(" 123456 ", "new-secret-1", "new-secret-1")

        assertEquals(
            listOf(ResetPasswordRequestDTO("marcos@example.com", "123456", "new-secret-1")),
            api.resets
        )
        assertTrue(viewModel.uiState.value.isReset)
        assertNull(viewModel.uiState.value.errorMessage)
    }

    @Test
    fun aWrongCodeShowsTheServersMessageAndKeepsTheRiderHere() = runTest(dispatcher) {
        val api = FakeRecoveryApi().apply {
            onReset = {
                throw HttpException(
                    Response.error<Any>(
                        400,
                        """{"status":400,"error":"Bad Request","message":"Código inválido ou expirado."}"""
                            .toResponseBody()
                    )
                )
            }
        }
        val viewModel = viewModel(api)
        viewModel.sendCode("marcos@example.com")

        viewModel.resetPassword("000000", "new-secret-1", "new-secret-1")

        val state = viewModel.uiState.value
        assertEquals("Código inválido ou expirado.", state.errorMessage)
        assertFalse(state.isReset)
        assertEquals(ForgotPasswordStep.CODE, state.step)
    }

    private object NoUserApi : UserApiService {
        override suspend fun getCurrentUser() = error("unused")
        override suspend fun updateName(
            request: br.dev.guisleri.mototrack.data.model.auth.UpdateUserNameRequestDTO
        ) = error("unused")
        override suspend fun requestEmailChange(
            request: br.dev.guisleri.mototrack.data.model.auth.ChangeEmailRequestDTO
        ) = error("unused")
        override suspend fun confirmEmailChange(
            request: br.dev.guisleri.mototrack.data.model.auth.ConfirmEmailChangeRequestDTO
        ) = error("unused")
    }
}
