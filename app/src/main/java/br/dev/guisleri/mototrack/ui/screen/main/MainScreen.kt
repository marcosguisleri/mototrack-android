package br.dev.guisleri.mototrack.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen

@Composable
fun MainScreen(
    onLogoutClick: () -> Unit
) {

    val backStack = rememberNavBackStack(MainDestination.HOME)

    val currentDestination = backStack.last() as MainDestination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MotoTrackBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = { destination ->

                    if (currentDestination != destination) {

                        backStack.clear()
                        backStack.add(MainDestination.HOME)

                        if (destination != MainDestination.HOME) {
                            backStack.add(destination)
                        }
                    }
                }
            )
        }
    ) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = { destination ->
                when (destination) {

                    MainDestination.HOME -> NavEntry(destination) {
                        HomeScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainDestination.TRIPS -> NavEntry(destination) {
                        TripsScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainDestination.MOTORCYCLES -> NavEntry(destination) {
                        MotorcyclesScreen(
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    MainDestination.PROFILE -> NavEntry(destination) {
                        ProfileScreen(
                            onLogoutClick = onLogoutClick,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }

                    else -> error("Unsupported navigation destination: $destination")
                }
            }
        )
    }
}