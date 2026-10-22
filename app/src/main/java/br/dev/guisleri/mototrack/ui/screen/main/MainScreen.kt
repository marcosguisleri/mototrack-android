package br.dev.guisleri.mototrack.ui.screen.main

import br.dev.guisleri.mototrack.ui.components.WarmDialog
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import br.dev.guisleri.mototrack.ui.feedback.Feedback
import br.dev.guisleri.mototrack.ui.feedback.FeedbackHost
import br.dev.guisleri.mototrack.ui.feedback.FeedbackKind
import br.dev.guisleri.mototrack.ui.feedback.showFeedback
import br.dev.guisleri.mototrack.ui.components.formatTripDate
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
import androidx.compose.ui.platform.LocalContext
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
import br.dev.guisleri.mototrack.data.photo.TripPhotoStorage
import br.dev.guisleri.mototrack.notification.TripReminderScheduler
import br.dev.guisleri.mototrack.ui.screen.trips.details.photos.TripPhotosViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.photos.TripPhotosViewModelFactory
import br.dev.guisleri.mototrack.notification.canPostNotifications
import br.dev.guisleri.mototrack.notification.planTripReminders
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.navigation.TripCreateDestination
import br.dev.guisleri.mototrack.ui.navigation.TripDetailsDestination
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.screen.home.HomeHighlightState
import br.dev.guisleri.mototrack.ui.screen.home.HomeScreen
import br.dev.guisleri.mototrack.ui.screen.home.HomeUiState
import br.dev.guisleri.mototrack.ui.screen.home.HomeViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesScreen
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesUiState
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcyclesViewModel
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileScreen
import br.dev.guisleri.mototrack.ui.screen.profile.ProfileViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.TripsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.TripsUiState
import br.dev.guisleri.mototrack.ui.screen.trips.TripsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateMode
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateScreen
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsUiState
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsViewModelFactory

@Composable
fun MainScreen(
    homeViewModel: HomeViewModel,
    motorcyclesViewModel: MotorcyclesViewModel,
    tripsViewModel: TripsViewModel,
    profileViewModel: ProfileViewModel,
    reminderScheduler: TripReminderScheduler,
    tripPhotoStorage: TripPhotoStorage,
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

    val snackbarHostState = remember { SnackbarHostState() }
    val feedbackScope = rememberCoroutineScope()

    // Lives in this scope, not in a screen's, so a message survives the screen closing.
    val showFeedback: (Feedback) -> Unit = remember {
        { feedback -> feedbackScope.launch { snackbarHostState.showFeedback(feedback) } }
    }

    LaunchedEffect(motorcyclesViewModel) {
        motorcyclesViewModel.feedback.collect(showFeedback)
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

    val profileUiState by profileViewModel
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

    // Trip reminders follow the planned trips, so they are re-planned whenever the trips change.
    val context = LocalContext.current

    var remindersEnabled by remember {
        mutableStateOf(reminderScheduler.isEnabled && canPostNotifications(context))
    }

    LaunchedEffect(homeTrips, remindersEnabled) {
        homeTrips?.let { trips ->
            reminderScheduler.sync(planTripReminders(trips, System.currentTimeMillis()))
        }
    }

    val garageSize by remember {
        derivedStateOf {
            when (val state = motorcyclesUiState) {
                is MotorcyclesUiState.Success -> state.motorcycles.size
                MotorcyclesUiState.Empty -> 0
                else -> null
            }
        }
    }

    // How many trips each motorcycle has, when the trips are known.
    val tripCountsByMotorcycle by remember {
        derivedStateOf {
            homeTrips
                ?.groupingBy { it.motorcycle.id }
                ?.eachCount()
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
        WarmDialog(
            icon = Icons.Outlined.TwoWheeler,
            title = "Cadastre sua moto primeiro",
            message = "Para criar uma viagem, você precisa ter ao menos uma moto cadastrada.",
            confirmText = "Cadastrar moto",
            onConfirm = {
                showNoMotorcycleDialog = false
                backStack.add(MotorcycleCreateDestination)
            },
            onDismissRequest = { showNoMotorcycleDialog = false },
            dismissText = "Agora não"
        )
    }

    val showBottomBar =
        backStack.lastOrNull() is MainDestination

    // A trip in progress puts a live dot on the trips tab.
    val liveDestinations by remember {
        derivedStateOf {
            val inProgress =
                (homeUiState as? HomeUiState.Success)?.highlight is HomeHighlightState.InProgress

            if (inProgress) setOf(MainDestination.TRIPS) else emptySet()
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = WarmCanvas,
        snackbarHost = { FeedbackHost(snackbarHostState) },
        bottomBar = {
            if (showBottomBar) {
                MotoTrackBottomBar(
                    currentDestination = currentDestination,
                    onDestinationSelected = navigateTo,
                    liveDestinations = liveDestinations
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
                                onGoHomeClick = {
                                    navigateTo(MainDestination.HOME)
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
                                    showFeedback(
                                        Feedback(
                                            "Moto cadastrada: " +
                                                    "${motorcycleCreateUiState.brand.trim()} " +
                                                    motorcycleCreateUiState.model.trim()
                                        )
                                    )
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
                                    showFeedback(
                                        Feedback(
                                            when (tripCreateUiState.mode) {
                                                TripCreateMode.PLANNED ->
                                                    "Viagem planejada para " +
                                                            formatTripDate(
                                                                tripCreateUiState.tripDate.toString()
                                                            )

                                                TripCreateMode.COMPLETED ->
                                                    "Viagem registrada no seu histórico"
                                            }
                                        )
                                    )
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
                                onDeleteMotorcycleClick = { motorcycleId ->
                                    motorcyclesViewModel.deleteMotorcycle(motorcycleId)
                                },
                                onDismissDeleteIssue =
                                    motorcyclesViewModel::dismissDeleteIssue,
                                tripCounts = tripCountsByMotorcycle,
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                            )
                        }

                    MainDestination.PROFILE ->
                        NavEntry(destination) {
                            ProfileScreen(
                                uiState = profileUiState,
                                trips = homeTrips,
                                motorcycleCount = garageSize,
                                onRetryUserClick = profileViewModel::loadUser,
                                remindersEnabled = remindersEnabled,
                                onRemindersChange = { enabled ->
                                    reminderScheduler.isEnabled = enabled
                                    remindersEnabled = enabled
                                    showFeedback(
                                        Feedback(
                                            if (enabled) {
                                                "Lembretes de viagem ativados"
                                            } else {
                                                "Lembretes de viagem desativados"
                                            },
                                            FeedbackKind.INFO
                                        )
                                    )
                                },
                                isLoggingOut = isLoggingOut,
                                onLogoutClick = onLogoutClick,
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
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

                            val tripPhotosViewModel: TripPhotosViewModel = viewModel(
                                key = "trip-photos-${destination.tripId}",
                                factory = TripPhotosViewModelFactory(
                                    tripId = destination.tripId,
                                    storage = tripPhotoStorage
                                )
                            )

                            val tripPhotosUiState by tripPhotosViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(tripPhotosViewModel) {
                                tripPhotosViewModel.feedback.collect(showFeedback)
                            }

                            val tripWasDeleted =
                                (tripDetailsUiState as? TripDetailsUiState.Success)
                                    ?.isDeleted == true

                            LaunchedEffect(tripWasDeleted) {
                                if (tripWasDeleted) {
                                    // The photos only make sense with their trip.
                                    feedbackScope.launch { tripPhotoStorage.clear(destination.tripId) }
                                    (tripDetailsUiState as? TripDetailsUiState.Success)?.trip?.let { trip ->
                                        showFeedback(
                                            Feedback("Viagem excluída: ${trip.origin} → ${trip.destination}")
                                        )
                                    }
                                    tripsViewModel.refreshTrips()
                                    homeViewModel.loadHome()
                                    motorcyclesViewModel.loadMotorcycles()
                                    backStack.removeLastOrNull()
                                }
                            }

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
                                    tripDetailsViewModel::confirmTripDateUpdate,
                                onDismissTripDateUpdate =
                                    tripDetailsViewModel::dismissTripDateUpdateConfirmation,
                                onDeleteTripClick = tripDetailsViewModel::deleteTrip,
                                onCelebrationFinished = tripDetailsViewModel::celebrationFinished,
                                photos = tripPhotosUiState,
                                onAddPhotos = tripPhotosViewModel::addPhotos,
                                onMakeCoverPhoto = tripPhotosViewModel::makeCover,
                                onRemovePhoto = tripPhotosViewModel::removePhoto,
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
