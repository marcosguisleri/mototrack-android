package br.dev.guisleri.mototrack.ui

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.ui.navigation.AppDestination
import br.dev.guisleri.mototrack.ui.screen.auth.LoginScreen
import br.dev.guisleri.mototrack.ui.screen.auth.RegisterScreen

@Composable
fun MotoTrackApp() {

    val backStack = rememberNavBackStack(AppDestination.LOGIN)

    NavDisplay(
        backStack = backStack,

        onBack = {
            if (backStack.size > 1) {
                backStack.removeLastOrNull()
            }
        },

        entryProvider = { destination ->

            when (destination) {

                AppDestination.LOGIN -> NavEntry(destination) {
                    LoginScreen(
                        onCreateAccountClick = {
                            backStack.add(AppDestination.REGISTER)
                        }
                    )
                }

                AppDestination.REGISTER -> NavEntry(destination) {
                    RegisterScreen(
                        onLoginClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                else -> error(
                    "Unsupported app destination: $destination"
                )
            }
        }
    )
}