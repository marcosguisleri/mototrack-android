package br.dev.guisleri.mototrack.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.data.local.TokenStorage
import br.dev.guisleri.mototrack.data.network.RetrofitClient
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import br.dev.guisleri.mototrack.ui.navigation.AppDestination
import br.dev.guisleri.mototrack.ui.screen.auth.LoginScreen
import br.dev.guisleri.mototrack.ui.screen.auth.LoginUiState
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModel
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.auth.RegisterScreen
import br.dev.guisleri.mototrack.ui.screen.main.MainScreen
import kotlinx.coroutines.launch

@Composable
fun MotoTrackApp() {

    val context = LocalContext.current

    val tokenStorage = remember {
        TokenStorage(context.applicationContext)
    }

    var initialDestination by remember {
        mutableStateOf<AppDestination?>(null)
    }

    LaunchedEffect(Unit) {
        initialDestination =
            if (tokenStorage.hasStoredSession()) {
                AppDestination.MAIN
            } else {
                AppDestination.LOGIN
            }
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

    val authRepository = remember {
        AuthRepository(
            authApiService = RetrofitClient.authApiService,
            tokenStorage = tokenStorage
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

                        MainScreen(
                            onLogoutClick = {
                                coroutineScope.launch {
                                    try {
                                        authRepository.logout()
                                    } finally {
                                        backStack.clear()
                                        backStack.add(
                                            AppDestination.LOGIN
                                        )
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