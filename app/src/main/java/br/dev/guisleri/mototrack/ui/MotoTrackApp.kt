package br.dev.guisleri.mototrack.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.data.network.RetrofitClient
import br.dev.guisleri.mototrack.data.repository.AuthRepository
import br.dev.guisleri.mototrack.ui.navigation.AppDestination
import br.dev.guisleri.mototrack.ui.screen.auth.LoginScreen
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModel
import br.dev.guisleri.mototrack.ui.screen.auth.LoginViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.auth.RegisterScreen

@Composable
fun MotoTrackApp() {

    val backStack =
        rememberNavBackStack(
            AppDestination.LOGIN
        )

    val authRepository =
        remember {
            AuthRepository(
                RetrofitClient.authApiService
            )
        }

    val loginViewModel: LoginViewModel =
        viewModel(
            factory =
                LoginViewModelFactory(
                    authRepository
                )
        )

    val loginUiState by
    loginViewModel
        .uiState
        .collectAsStateWithLifecycle()

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

                            onLoginClick = {
                                    email,
                                    password ->

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

                else ->
                    error(
                        "Unsupported app destination: $destination"
                    )
            }
        }
    )
}