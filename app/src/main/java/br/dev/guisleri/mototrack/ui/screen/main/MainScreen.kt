package br.dev.guisleri.mototrack.ui.screen.main

import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateScreen
import br.dev.guisleri.mototrack.ui.navigation.MotorcycleCreateDestination
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import br.dev.guisleri.mototrack.data.local.MotorcyclePreferences
import br.dev.guisleri.mototrack.data.repository.MotorcycleRepository
import br.dev.guisleri.mototrack.data.repository.TripRepository
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.navigation.TripCreateDestination
import br.dev.guisleri.mototrack.ui.navigation.TripDetailsDestination
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.home.HomeUiState
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesUiState
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesViewModel
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsUiState
import br.dev.guisleri.mototrack.ui.screen.trips.TripsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateMode
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateScreen
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModelFactory

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    motorcyclesViewModel: MotorcyclesViewModel,
    tripsViewModel: TripsViewModel,
    tripRepository: TripRepository,
    motorcycleRepository: MotorcycleRepository,
    motorcyclePreferences: MotorcyclePreferences,
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

    // Set by "full history" on the motorcycles tab; any other way into Trips clears it.
    var tripsMotorcycleFilterId by rememberSaveable {
        mutableStateOf<Long?>(null)
    }

    val navigateTo: (MainDestination) -> Unit = { destination ->
        if (currentDestination != destination) {
            if (destination == MainDestination.TRIPS) {
                tripsMotorcycleFilterId = null
            }

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

    val homeUiState by homeViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val motorcyclesUiState by motorcyclesViewModel
        .uiState
        .collectAsStateWithLifecycle()

    val tripsUiState by tripsViewModel
        .uiState
        .collectAsStateWithLifecycle()

    // The navigation entries below are created once and keep their lambdas, so
    // anything they read from here must be observable state, never a plain value.

    // The rider's default motorcycle (or the first one) shown on Home.
    val homeMotorcycle by remember {
        derivedStateOf {
            (motorcyclesUiState as? MotorcyclesUiState.Success)?.let { state ->
                state.motorcycles.firstOrNull { it.id == state.defaultMotorcycleId }
                    ?: state.motorcycles.firstOrNull()
            }
        }
    }

    val homeTrips by remember {
        derivedStateOf {
            when (val state = tripsUiState) {
                is TripsUiState.Success -> state.trips
                TripsUiState.Empty -> emptyList()
                else -> null
            }
        }
    }

    // Unknown (loading/error) counts as "has motorcycles": the create screen
    // handles an empty garage on its own, so we only block when we are sure.
    val hasNoMotorcycles by remember {
        derivedStateOf {
            motorcyclesUiState == MotorcyclesUiState.Empty ||
                    (homeUiState as? HomeUiState.Success)
                        ?.home?.motorcycleCount == 0L
        }
    }

    var showNoMotorcycleDialog by rememberSaveable {
        mutableStateOf(false)
    }

    val openTripCreate: (Boolean) -> Unit = { startAsCompleted ->
        if (hasNoMotorcycles) {
            showNoMotorcycleDialog = true
        } else {
            backStack.add(
                TripCreateDestination(
                    startAsCompleted = startAsCompleted
                )
            )
        }
    }

    if (showNoMotorcycleDialog) {
        AlertDialog(
            onDismissRequest = { showNoMotorcycleDialog = false },
            title = { Text("Cadastre sua moto primeiro") },
            text = {
                Text("Para criar uma viagem, você precisa ter ao menos uma moto cadastrada.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showNoMotorcycleDialog = false
                    backStack.add(MotorcycleCreateDestination)
                }) {
                    Text("Cadastrar moto")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNoMotorcycleDialog = false }) {
                    Text("Agora não")
                }
            }
        )
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
                    homeViewModel.loadHome()
                }

                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = { destination ->

                when (destination) {

                    MainDestination.HOME ->
                        NavEntry(destination) {

                            HomeScreen(
                                uiState = homeUiState,
                                onRetryClick = {
                                    homeViewModel.loadHome()
                                },
                                onRefresh = {
                                    homeViewModel.loadHome()
                                },
                                onTripClick = { tripId ->
                                    backStack.add(
                                        TripDetailsDestination(
                                            tripId = tripId
                                        )
                                    )
                                },
                                onAddMotorcycleClick = {
                                    backStack.add(MotorcycleCreateDestination)
                                },
                                onTripsClick = {
                                    navigateTo(
                                        MainDestination.TRIPS
                                    )
                                },
                                onPlanTripClick = {
                                    openTripCreate(false)
                                },
                                onRegisterCompletedTripClick = {
                                    openTripCreate(true)
                                },
                                motorcycle = homeMotorcycle,
                                trips = homeTrips,
                                onGarageClick = {
                                    navigateTo(MainDestination.MOTORCYCLES)
                                },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                            )
                        }

                    MainDestination.TRIPS -> {
                        NavEntry(key = destination) {

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
                                onCreateTripClick = {
                                    openTripCreate(false)
                                },
                                onRegisterCompletedTripClick = {
                                    openTripCreate(true)
                                },
                                motorcycleFilterId = tripsMotorcycleFilterId,
                                motorcycleFilterName = tripsMotorcycleFilterId?.let { id ->
                                    (motorcyclesUiState as? MotorcyclesUiState.Success)
                                        ?.motorcycles
                                        ?.firstOrNull { it.id == id }
                                        ?.let { "${it.brand} ${it.model}" }
                                },
                                onClearMotorcycleFilterClick = {
                                    tripsMotorcycleFilterId = null
                                },
                                hasMotorcycles = !hasNoMotorcycles,
                                onAddMotorcycleClick = {
                                    backStack.add(MotorcycleCreateDestination)
                                },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                            )
                        }
                    }

                    MotorcycleCreateDestination ->
                        NavEntry(destination) {

                            val motorcycleCreateViewModel: MotorcycleCreateViewModel =
                                viewModel(
                                    factory = MotorcycleCreateViewModelFactory(
                                        motorcycleRepository = motorcycleRepository
                                    )
                                )

                            val motorcycleCreateUiState by motorcycleCreateViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(motorcycleCreateUiState.isCreated) {
                                if (motorcycleCreateUiState.isCreated) {
                                    motorcyclesViewModel.loadMotorcycles()
                                    homeViewModel.loadHome()
                                    backStack.removeLastOrNull()
                                }
                            }

                            MotorcycleCreateScreen(
                                uiState = motorcycleCreateUiState,
                                onBackClick = {
                                    backStack.removeLastOrNull()
                                },
                                onTypeSelected = motorcycleCreateViewModel::onTypeSelected,
                                onBrandChange = motorcycleCreateViewModel::onBrandChange,
                                onModelChange = motorcycleCreateViewModel::onModelChange,
                                onYearChange = motorcycleCreateViewModel::onYearChange,
                                onEngineCapacityChange =
                                    motorcycleCreateViewModel::onEngineCapacityChange,
                                onColorSelected =
                                    motorcycleCreateViewModel::onColorSelected,
                                onCustomColorSelected =
                                    motorcycleCreateViewModel::onCustomColorSelected,
                                onCustomColorChange =
                                    motorcycleCreateViewModel::onCustomColorChange,
                                onSubmitClick = motorcycleCreateViewModel::submit,
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            )
                        }

                    is TripCreateDestination ->
                        NavEntry(destination) {

                            val tripCreateViewModel: TripCreateViewModel = viewModel(
                                factory = TripCreateViewModelFactory(
                                    tripRepository = tripRepository,
                                    motorcycleRepository = motorcycleRepository,
                                    motorcyclePreferences = motorcyclePreferences,
                                    initialMode = if (destination.startAsCompleted) {
                                        TripCreateMode.COMPLETED
                                    } else {
                                        TripCreateMode.PLANNED
                                    },
                                    requestedMotorcycleId = destination.motorcycleId
                                )
                            )

                            val tripCreateUiState by tripCreateViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(tripCreateUiState.isCreated) {
                                if (tripCreateUiState.isCreated) {
                                    tripsViewModel.refreshTrips()
                                    homeViewModel.loadHome()
                                    backStack.removeLastOrNull()
                                }
                            }

                            TripCreateScreen(
                                uiState = tripCreateUiState,
                                onBackClick = {
                                    backStack.removeLastOrNull()
                                },
                                onModeSelected = tripCreateViewModel::onModeSelected,
                                onOriginChange = tripCreateViewModel::onOriginChange,
                                onDestinationChange =
                                    tripCreateViewModel::onDestinationChange,
                                onSwapRouteClick = tripCreateViewModel::swapRoute,
                                onDistanceChange = tripCreateViewModel::onDistanceChange,
                                onApplyDistanceSuggestionClick =
                                    tripCreateViewModel::applyDistanceSuggestion,
                                onTripDateSelected =
                                    tripCreateViewModel::onTripDateSelected,
                                onTerrainSelected = tripCreateViewModel::onTerrainSelected,
                                onMotorcycleSelected =
                                    tripCreateViewModel::onMotorcycleSelected,
                                onRetryMotorcyclesClick =
                                    tripCreateViewModel::loadMotorcycles,
                                onSubmitClick = tripCreateViewModel::submit,
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            )
                        }

                    MainDestination.MOTORCYCLES ->
                        NavEntry(destination) {

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
                                onSetDefaultClick = { motorcycleId ->
                                    motorcyclesViewModel.setDefaultMotorcycle(
                                        motorcycleId
                                    )
                                },
                                onPlanTripClick = { motorcycleId ->
                                    backStack.add(
                                        TripCreateDestination(
                                            motorcycleId = motorcycleId
                                        )
                                    )
                                },
                                onHistoryClick = { motorcycleId ->
                                    navigateTo(MainDestination.TRIPS)
                                    tripsMotorcycleFilterId = motorcycleId
                                },
                                onTripClick = { tripId ->
                                    backStack.add(
                                        TripDetailsDestination(
                                            tripId = tripId
                                        )
                                    )
                                },
                                onAddMotorcycleClick = {
                                    backStack.add(MotorcycleCreateDestination)
                                },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
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
                                    homeViewModel.loadHome()
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
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
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
