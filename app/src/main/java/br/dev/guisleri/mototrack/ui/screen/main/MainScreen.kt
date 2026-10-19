package br.dev.guisleri.mototrack.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesViewModel
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsViewModel

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    motorcyclesViewModel: MotorcyclesViewModel,
    tripsViewModel: TripsViewModel,
    onLogoutClick: () -> Unit
) {

    val backStack = rememberNavBackStack(
        MainDestination.HOME
    )

    val currentDestination =
        backStack.last() as MainDestination

    val navigateTo: (MainDestination) -> Unit = { destination ->
        if (currentDestination != destination) {

            backStack.clear()
            backStack.add(MainDestination.HOME)

            if (destination != MainDestination.HOME) {
                backStack.add(destination)
            }

            if (destination == MainDestination.HOME) {
                homeViewModel.loadHome()
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            MotoTrackBottomBar(
                currentDestination = currentDestination,
                onDestinationSelected = navigateTo
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

                    MainDestination.HOME ->
                        NavEntry(destination) {

                            val homeUiState by
                            homeViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            HomeScreen(
                                uiState = homeUiState,
                                onRetryClick = {
                                    homeViewModel.loadHome()
                                },
                                onAddMotorcycleClick = {
                                    navigateTo(
                                        MainDestination.MOTORCYCLES
                                    )
                                },
                                onTripsClick = {
                                    navigateTo(
                                        MainDestination.TRIPS
                                    )
                                },
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            )
                        }

                    MainDestination.TRIPS -> {
                        NavEntry(key = destination) {

                            val tripsUiState by tripsViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            TripsScreen(
                                uiState = tripsUiState,
                                onRetryClick = {
                                    tripsViewModel.loadTrips()
                                },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    MainDestination.MOTORCYCLES ->
                        NavEntry(destination) {

                            val motorcyclesUiState by
                            motorcyclesViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            MotorcyclesScreen(
                                uiState = motorcyclesUiState,
                                onRetryClick = {
                                    motorcyclesViewModel.loadMotorcycles()
                                },
                                onMotorcycleSelected = { motorcycleId ->
                                    motorcyclesViewModel.selectMotorcycle(
                                        motorcycleId
                                    )
                                },
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            )
                        }

                    MainDestination.PROFILE ->
                        NavEntry(destination) {
                            ProfileScreen(
                                onLogoutClick = onLogoutClick,
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            )
                        }

                    else ->
                        error(
                            "Unsupported navigation destination: $destination"
                        )
                }
            }
        )
    }
}
