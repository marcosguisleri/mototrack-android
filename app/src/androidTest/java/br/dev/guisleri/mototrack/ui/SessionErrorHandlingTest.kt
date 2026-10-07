package br.dev.guisleri.mototrack.ui

import androidx.compose.material3.Text
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.model.auth.AuthTokenResponseDTO
import br.dev.guisleri.mototrack.data.model.auth.LoginRequestDTO
import br.dev.guisleri.mototrack.data.model.auth.RefreshTokenRequestDTO
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.network.AuthApiService
import br.dev.guisleri.mototrack.data.network.HomeApiService
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import br.dev.guisleri.mototrack.ui.screen.auth.LoginUiState
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModel
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.home.HomeUiState
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModelFactory
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@RunWith(AndroidJUnit4::class)
class SessionErrorHandlingTest {
    @get:Rule
    val compose = createComposeRule()

    private val viewModelStore = ViewModelStore()
    private val owner = object : ViewModelStoreOwner {
        override val viewModelStore = this@SessionErrorHandlingTest.viewModelStore
    }

    @After
    fun tearDown() {
        compose.runOnIdle { viewModelStore.clear() }
    }

    @Test
    fun initialStorageFailureShowsRetryAndDoesNotAssumeExpiredSession() {
        val dataStore = FailingPreferencesStore()
        val storage = TokenStorage(dataStore)
        compose.setContent { MotoTrackTheme { MotoTrackApp(storage) } }

        compose.onNodeWithText("Não foi possível ler a sessão").assertExists()
        compose.onNodeWithText("Entrar").assertDoesNotExist()

        dataStore.fail.set(false)
        compose.onNodeWithText("Tentar novamente").performClick()

        compose.onNodeWithText("Entrar").assertExists()
        compose.onNodeWithText("Não foi possível ler a sessão").assertDoesNotExist()
    }

    @Test
    fun sessionObservationFailureCanRetryWithoutDroppingNavigation() {
        val dataStore = FailingPreferencesStore().apply { fail.set(false) }
        val storage = TokenStorage(dataStore)
        compose.setContent { MotoTrackTheme { MotoTrackApp(storage) } }
        compose.onNodeWithText("Entrar").assertExists()

        dataStore.fail.set(true)
        dataStore.readAttempt.value++
        compose.onNodeWithText("Não foi possível ler a sessão").assertExists()

        dataStore.fail.set(false)
        compose.onNodeWithText("Tentar novamente").performClick()

        compose.onNodeWithText("Entrar").assertExists()
        compose.onNodeWithText("Não foi possível ler a sessão").assertDoesNotExist()
    }

    @Test
    fun homeCancellationDoesNotBecomeUiError() {
        compose.setContent { Text("Test") }
        compose.runOnIdle {
            val viewModel = homeViewModel(CancellationException("Cancelled"))
            assertSame(HomeUiState.Loading, viewModel.uiState.value)
        }
    }

    @Test
    fun loginCancellationDoesNotBecomeUiError() {
        val api = object : AuthApiService {
            override suspend fun login(request: LoginRequestDTO): AuthTokenResponseDTO =
                throw CancellationException("Cancelled")
            override suspend fun refresh(request: RefreshTokenRequestDTO): AuthTokenResponseDTO =
                throw AssertionError("Unexpected refresh")
            override suspend fun logout(request: RefreshTokenRequestDTO) =
                throw AssertionError("Unexpected logout")
        }
        compose.setContent { Text("Test") }
        compose.runOnIdle {
            val repository = AuthRepository(api, TokenStorage(FailingPreferencesStore()))
            val viewModel = ViewModelProvider(owner, LoginViewModelFactory(repository))
                .get(LoginViewModel::class.java)
            viewModel.login("test@example.test", "test-password")

            assertSame(LoginUiState.Loading, viewModel.uiState.value)
        }
    }

    @Test
    fun homeCommunicationFailureDoesNotShowInvalidSession() {
        compose.setContent { Text("Test") }
        compose.runOnIdle {
            val state = homeViewModel(IOException("Refresh unavailable")).uiState.value
            assertTrue(state is HomeUiState.Error)
            val message = (state as HomeUiState.Error).message
            assertTrue(message.contains("servidor"))
            assertFalse(message.contains("sessão"))
        }
    }

    private fun homeViewModel(exception: Exception): HomeViewModel {
        val repository = HomeRepository(object : HomeApiService {
            override suspend fun getHome(): HomeResponseDTO = throw exception
        })
        return ViewModelProvider(owner, HomeViewModelFactory(repository))[HomeViewModel::class.java]
    }

    private class FailingPreferencesStore : DataStore<Preferences> {
        val fail = AtomicBoolean(true)
        val readAttempt = MutableStateFlow(0)
        override val data = readAttempt.map {
            if (fail.get()) throw IOException("Storage unavailable")
            emptyPreferences()
        }

        override suspend fun updateData(transform: suspend (Preferences) -> Preferences): Preferences =
            throw AssertionError("Reading a session must not write or clear tokens")
    }
}
