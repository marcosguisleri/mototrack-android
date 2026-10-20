package br.dev.guisleri.mototrack.ui.screen.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.data.repository.TripRepository
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.navigation.TripDetailsDestination
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesViewModel
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModelFactory

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    motorcyclesViewModel: MotorcyclesViewModel,
    tripsViewModel: TripsViewModel,
    tripRepository: TripRepository,
    isLoggingOut: Boolean = false,
    onLogoutClick: () -> Unit
) {

    val backStack = rememberNavBackStack(
        MainDestination.HOME
    )

    val currentDestination =
        backStack
            .filterIsInstance<MainDestination>()
            .lastOrNull()
            ?: MainDestination.HOME

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

    val showBottomBar =
        backStack.lastOrNull() is MainDestination

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                MotoTrackBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = navigateTo
                )
            }
        }
    ) { innerPadding ->

        NavDisplay(
            backStack = backStack,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator()
            ),
            onBack = {
                if (backStack.lastOrNull() is TripDetailsDestination) {
                    tripsViewModel.refreshTrips()
                }

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

                            val isTripsRefreshing by tripsViewModel
                                .isRefreshing
                                .collectAsStateWithLifecycle()

                            TripsScreen(
                                uiState = tripsUiState,
                                isRefreshing = isTripsRefreshing,
                                onRefresh = {
                                    tripsViewModel.refreshTrips()
                                },
                                onRetryClick = {
                                    tripsViewModel.loadTrips()
                                },
                                onTripClick = { tripId ->
                                    backStack.add(
                                        TripDetailsDestination(
                                            tripId = tripId
                                        )
                                    )
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
                                onRetryStatisticsClick = {
                                    motorcyclesViewModel.retrySelectedStatistics()
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
                                isLoggingOut = isLoggingOut,
                                onLogoutClick = onLogoutClick,
                                modifier = Modifier.padding(
                                    innerPadding
                                )
                            )
                        }

                    is TripDetailsDestination ->
                        NavEntry(destination) {

                            val tripDetailsViewModel: TripDetailsViewModel = viewModel(
                                key = "trip-details-${destination.tripId}",
                                factory = TripDetailsViewModelFactory(
                                    tripId = destination.tripId,
                                    tripRepository = tripRepository
                                )
                            )

                            val tripDetailsUiState by tripDetailsViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            TripDetailsScreen(
                                uiState = tripDetailsUiState,
                                onRetryClick = {
                                    tripDetailsViewModel.loadTrip()
                                },
                                onBackClick = {
                                    tripsViewModel.refreshTrips()
                                    backStack.removeLastOrNull()
                                },
                                onStartTripClick = {
                                    tripDetailsViewModel.startTrip()
                                },
                                onCompleteTripClick = {
                                    tripDetailsViewModel.completeTrip()
                                },
                                onConfirmTripDateUpdate =
                                    tripDetailsViewModel::confirmStartTripWithDateUpdate,
                                onDismissTripDateUpdate =
                                    tripDetailsViewModel::dismissTripDateUpdateConfirmation,
                                modifier = Modifier.padding(innerPadding)
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
