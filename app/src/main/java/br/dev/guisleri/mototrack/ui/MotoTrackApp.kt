package br.dev.guisleri.mototrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.network.HomeApiService
import br.dev.guisleri.mototrack.data.network.RetrofitClient
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import br.dev.guisleri.mototrack.data.repository.HomeRepository
import br.dev.guisleri.mototrack.ui.navigation.AppDestination
import br.dev.guisleri.mototrack.ui.screen.auth.LoginScreen
import br.dev.guisleri.mototrack.ui.screen.auth.LoginUiState
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModel
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.auth.RegisterScreen
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.main.MainScreen
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

@Composable
fun MotoTrackApp(providedTokenStorage: TokenStorage? = null) {

    val context = LocalContext.current

    val tokenStorage = remember(providedTokenStorage) {
        providedTokenStorage ?: TokenStorage(context.applicationContext)
    }

    var initialDestination by remember {
        mutableStateOf<AppDestination?>(null)
    }

    var sessionReadError by remember { mutableStateOf(false) }
    var sessionReadAttempt by remember { mutableIntStateOf(0) }

    LaunchedEffect(tokenStorage, sessionReadAttempt) {
        if (initialDestination != null) return@LaunchedEffect

        try {
            initialDestination =
                if (tokenStorage.hasStoredSession()) {
                    AppDestination.MAIN
                } else {
                    AppDestination.LOGIN
                }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            sessionReadError = true
        }
    }

    if (sessionReadError) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Não foi possível ler a sessão") },
            text = { Text("O armazenamento local está indisponível. Tente novamente.") },
            confirmButton = {
                TextButton(onClick = {
                    sessionReadError = false
                    sessionReadAttempt++
                }) {
                    Text("Tentar novamente")
                }
            }
        )
    }

    if (initialDestination == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    MaterialTheme.colorScheme.background
                )
        )

        return
    }

    val backStack = rememberNavBackStack(
        initialDestination!!
    )

    LaunchedEffect(tokenStorage, backStack, sessionReadAttempt) {
        try {
            tokenStorage.hasSession.collect { hasSession ->

                if (
                    !hasSession &&
                    backStack.lastOrNull() == AppDestination.MAIN
                ) {
                    backStack.clear()
                    backStack.add(AppDestination.LOGIN)
                }
            }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            sessionReadError = true
        }
    }

    val authRepository = remember {
        AuthRepository(
            authApiService = RetrofitClient.authApiService,
            tokenStorage = tokenStorage
        )
    }

    val authenticatedRetrofit = remember {
        RetrofitClient.createAuthenticatedRetrofit(
            tokenStorage = tokenStorage
        )
    }

    val homeApiService = remember {
        authenticatedRetrofit.create(
            HomeApiService::class.java
        )
    }

    val homeRepository = remember {
        HomeRepository(
            homeApiService = homeApiService
        )
    }

    val coroutineScope = rememberCoroutineScope()

    val loginViewModel: LoginViewModel = viewModel(
        factory = LoginViewModelFactory(
            authRepository
        )
    )

    val loginUiState by
    loginViewModel
        .uiState
        .collectAsStateWithLifecycle()

    LaunchedEffect(loginUiState) {
        if (loginUiState is LoginUiState.Success) {
            backStack.clear()
            backStack.add(AppDestination.MAIN)

            loginViewModel.resetState()
        }
    }

    NavDisplay(
        backStack = backStack,
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator()
        ),

        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },

        entryProvider = { destination ->

            when (destination) {

                AppDestination.LOGIN ->
                    NavEntry(destination) {

                        LoginScreen(
                            uiState = loginUiState,

                            onLoginClick = { email, password ->
                                loginViewModel.login(
                                    email = email,
                                    password = password
                                )
                            },

                            onCreateAccountClick = {
                                backStack.add(
                                    AppDestination.REGISTER
                                )
                            }
                        )
                    }

                AppDestination.REGISTER ->
                    NavEntry(destination) {

                        RegisterScreen(
                            onLoginClick = {
                                backStack.removeLastOrNull()
                            }
                        )
                    }

                AppDestination.MAIN ->
                    NavEntry(destination) {

                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModelFactory(
                                homeRepository = homeRepository
                            )
                        )

                        MainScreen(
                            homeViewModel = homeViewModel,
                            onLogoutClick = {
                                coroutineScope.launch {
                                    runCatching {
                                        authRepository.logout()
                                    }.onFailure { exception ->
                                        if (exception is CancellationException) {
                                            throw exception
                                        }
                                    }
                                }
                            }
                        )
                    }

                else ->
                    error(
                        "Unsupported app destination: $destination"
                    )
            }
        }
    )
}
