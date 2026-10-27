package br.dev.guisleri.mototrack.ui.screen.main

import br.dev.guisleri.mototrack.ui.components.WarmDialog
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.Icons
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateViewModel
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateScreen
import br.dev.guisleri.mototrack.ui.navigation.MotorcycleCreateDestination
import br.dev.guisleri.mototrack.ui.navigation.MotorcycleEditDestination
import br.dev.guisleri.mototrack.ui.screen.motorcycles.create.MotorcycleCreateUiState
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import android.net.ConnectivityManager
import android.net.Network
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
import br.dev.guisleri.mototrack.notification.MaintenanceAlertScheduler
import br.dev.guisleri.mototrack.ui.screen.profile.RiderLevelMemory
import br.dev.guisleri.mototrack.ui.screen.profile.riderLevelOf
import br.dev.guisleri.mototrack.notification.MaintenanceNotifier
import br.dev.guisleri.mototrack.notification.ReminderKind
import br.dev.guisleri.mototrack.notification.costsReminder
import br.dev.guisleri.mototrack.notification.forgottenTripReminder
import br.dev.guisleri.mototrack.notification.planTripReminders
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripCelebration
import br.dev.guisleri.mototrack.ui.navigation.MainDestination
import br.dev.guisleri.mototrack.ui.navigation.MotoTrackBottomBar
import br.dev.guisleri.mototrack.ui.navigation.TripCreateDestination
import br.dev.guisleri.mototrack.ui.navigation.TripDetailsDestination
import br.dev.guisleri.mototrack.ui.navigation.TripEditDestination
import br.dev.guisleri.mototrack.ui.navigation.TripGalleryDestination
import br.dev.guisleri.mototrack.ui.screen.trips.details.photos.TripGalleryScreen
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
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
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateUiState
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.create.TripCreateViewModelFactory
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripCostsActions
import br.dev.guisleri.mototrack.ui.screen.trips.details.TripDetailsScreen
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripCostsViewModel
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripCostsViewModelFactory
import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceStore
import br.dev.guisleri.mototrack.data.offline.OfflineServices
import br.dev.guisleri.mototrack.data.place.PlaceServices
import br.dev.guisleri.mototrack.data.maintenance.maintenanceOverview
import br.dev.guisleri.mototrack.data.maintenance.summarize
import br.dev.guisleri.mototrack.ui.navigation.MaintenanceDestination
import br.dev.guisleri.mototrack.ui.screen.maintenance.MaintenanceScreen
import br.dev.guisleri.mototrack.ui.screen.maintenance.MaintenanceViewModel
import br.dev.guisleri.mototrack.ui.screen.maintenance.MaintenanceViewModelFactory
import kotlinx.coroutines.flow.emptyFlow
import java.time.LocalDate
import br.dev.guisleri.mototrack.ui.screen.motorcycles.MotorcycleStatisticsUiState
import androidx.lifecycle.compose.LifecycleResumeEffect
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
    tripCostRepository: TripCostRepository,
    maintenanceStore: MaintenanceStore,
    placeServices: PlaceServices,
    offline: OfflineServices,
    motorcycleRepository: MotorcycleRepository,
    motorcyclePreferences: MotorcyclePreferences,
    isLoggingOut: Boolean = false,
    onLogoutClick: () -> Unit
) {

    val backStack = rememberNavBackStack(
        MainDestination.HOME
    )

    // The photos of a trip are shared by its details and its gallery, so their view model
    // lives here, not in either screen.
    val mainViewModelOwner = checkNotNull(LocalViewModelStoreOwner.current)

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

            // Trips completed or changed since the garage was last shown count in it.
            if (destination == MainDestination.MOTORCYCLES) {
                motorcyclesViewModel.refresh()
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

    // The name edited on the profile shows in the greeting of the Home at once.
    LaunchedEffect(profileViewModel) {
        profileViewModel.nameUpdated.collect { name ->
            homeViewModel.onUserNameChanged(name)
            showFeedback(Feedback("Nome atualizado"))
        }
    }

    // A level gained is celebrated once, when the trips show it.
    val levelMemory = remember { RiderLevelMemory(context) }

    LaunchedEffect(homeTrips) {
        val trips = homeTrips ?: return@LaunchedEffect
        val level = riderLevelOf(trips)

        if (levelMemory.reached(level.level)) {
            showFeedback(Feedback("Subiu de nível! Agora você é nível ${level.level}: ${level.name} 🏍️"))
        }
    }

    // The daily look at the motorcycles' maintenance follows the same switch.
    LaunchedEffect(remindersEnabled) {
        val scheduler = MaintenanceAlertScheduler(context)

        if (remindersEnabled) scheduler.ensureScheduled() else scheduler.cancel()
    }

    // How the selected motorcycle's maintenance stands, for the entry card on its tab.
    val selectedMotorcycleId =
        (motorcyclesUiState as? MotorcyclesUiState.Success)?.selectedMotorcycleId

    val selectedTripKm =
        ((motorcyclesUiState as? MotorcyclesUiState.Success)?.statisticsUiState
                as? MotorcycleStatisticsUiState.Success)
            ?.statistics
            ?.totalCompletedDistanceKm

    val selectedMaintenance by remember(selectedMotorcycleId) {
        selectedMotorcycleId
            ?.let { maintenanceStore.observe(it) }
            ?: emptyFlow()
    }.collectAsStateWithLifecycle(initialValue = null)

    // The notifications of maintenance work from what is saved, so the km of the trips and
    // the motorcycle's name are saved with it; and anything that just became due is told.
    val selectedMotorcycleName =
        (motorcyclesUiState as? MotorcyclesUiState.Success)?.let { state ->
            state.motorcycles.firstOrNull { it.id == state.selectedMotorcycleId }
                ?.let { "${it.brand} ${it.model}" }
        }

    LaunchedEffect(selectedMotorcycleId, selectedTripKm, selectedMotorcycleName, selectedMaintenance, remindersEnabled) {
        val id = selectedMotorcycleId ?: return@LaunchedEffect
        val current = selectedMaintenance ?: return@LaunchedEffect

        if (
            (selectedTripKm != null && current.tripKmSnapshot != selectedTripKm) ||
            (selectedMotorcycleName != null && current.motorcycleName != selectedMotorcycleName)
        ) {
            maintenanceStore.update(id) {
                it.copy(
                    tripKmSnapshot = selectedTripKm ?: it.tripKmSnapshot,
                    motorcycleName = selectedMotorcycleName ?: it.motorcycleName
                )
            }
        }

        if (remindersEnabled) {
            MaintenanceNotifier(context, maintenanceStore).check()
        }
    }

    val maintenanceSummary = remember(selectedMaintenance, selectedTripKm) {
        selectedMaintenance?.let {
            summarize(maintenanceOverview(it, selectedTripKm, LocalDate.now()))
        }
    }

    // Costs registered offline are sent as soon as there is a connection again: when the
    // app opens, when the network comes back, and the rider is told what happened.
    LaunchedEffect(offline) {
        launch { offline.syncer.flush() }

        offline.syncer.events.collect { event ->
            showFeedback(
                if (event.dropped > 0) {
                    Feedback(
                        message = buildString {
                            if (event.sent > 0) append("${event.sent} enviado(s). ")
                            append("${event.dropped} registro(s) não puderam ser enviados e foram descartados.")
                        },
                        kind = FeedbackKind.ERROR
                    )
                } else {
                    Feedback(
                        if (event.sent == 1) "1 registro enviado" else "${event.sent} registros enviados"
                    )
                }
            )
        }
    }

    DisposableEffect(offline, context) {
        val manager = context.getSystemService(ConnectivityManager::class.java)

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                feedbackScope.launch { offline.syncer.flush() }
            }
        }

        val registered = runCatching { manager?.registerDefaultNetworkCallback(callback) }.isSuccess

        onDispose {
            if (registered) runCatching { manager?.unregisterNetworkCallback(callback) }
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
                    motorcyclesViewModel.refresh()
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

                            MotorcycleFormScreen(
                                viewModel = motorcycleCreateViewModel,
                                uiState = motorcycleCreateUiState,
                                onBackClick = {
                                    backStack.removeLastOrNull()
                                },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            )
                        }

                    is MotorcycleEditDestination ->
                        NavEntry(destination) {

                            val motorcycleEditViewModel: MotorcycleCreateViewModel =
                                viewModel(
                                    key = "motorcycle-edit-${destination.motorcycleId}",
                                    factory = MotorcycleCreateViewModelFactory(
                                        motorcycleRepository = motorcycleRepository,
                                        editingMotorcycleId = destination.motorcycleId
                                    )
                                )

                            val motorcycleEditUiState by motorcycleEditViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(motorcycleEditUiState.isUpdated) {
                                if (motorcycleEditUiState.isUpdated) {
                                    showFeedback(
                                        Feedback(
                                            "Moto atualizada: " +
                                                    "${motorcycleEditUiState.brand.trim()} " +
                                                    motorcycleEditUiState.model.trim()
                                        )
                                    )
                                    motorcyclesViewModel.refresh()
                                    homeViewModel.loadHome()
                                    tripsViewModel.refreshTrips()
                                    backStack.removeLastOrNull()
                                }
                            }

                            MotorcycleFormScreen(
                                viewModel = motorcycleEditViewModel,
                                uiState = motorcycleEditUiState,
                                onBackClick = {
                                    backStack.removeLastOrNull()
                                },
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
                                    requestedMotorcycleId = destination.motorcycleId,
                                    places = placeServices
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
                                    motorcyclesViewModel.refresh()
                                    backStack.removeLastOrNull()
                                }
                            }

                            TripFormScreen(
                                viewModel = tripCreateViewModel,
                                uiState = tripCreateUiState,
                                onBackClick = { backStack.removeLastOrNull() },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                                    .consumeWindowInsets(innerPadding)
                            )
                        }

                    is TripEditDestination ->
                        NavEntry(destination) {

                            val tripEditViewModel: TripCreateViewModel = viewModel(
                                key = "trip-edit-${destination.tripId}",
                                factory = TripCreateViewModelFactory(
                                    tripRepository = tripRepository,
                                    motorcycleRepository = motorcycleRepository,
                                    motorcyclePreferences = motorcyclePreferences,
                                    editingTripId = destination.tripId,
                                    places = placeServices
                                )
                            )

                            val tripEditUiState by tripEditViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            LaunchedEffect(tripEditUiState.isUpdated) {
                                if (tripEditUiState.isUpdated) {
                                    showFeedback(Feedback("Viagem atualizada"))
                                    tripsViewModel.refreshTrips()
                                    homeViewModel.loadHome()
                                    motorcyclesViewModel.loadMotorcycles()
                                    backStack.removeLastOrNull()
                                }
                            }

                            TripFormScreen(
                                viewModel = tripEditViewModel,
                                uiState = tripEditUiState,
                                onBackClick = { backStack.removeLastOrNull() },
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
                                onRefresh = motorcyclesViewModel::refresh,
                                onEditMotorcycleClick = { motorcycleId ->
                                    backStack.add(MotorcycleEditDestination(motorcycleId))
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
                                maintenanceSummary = maintenanceSummary,
                                onMaintenanceClick = { motorcycleId ->
                                    backStack.add(MaintenanceDestination(motorcycleId))
                                },
                                modifier = Modifier
                                    .background(WarmCanvas)
                                    .padding(innerPadding)
                            )
                        }

                    is MaintenanceDestination ->
                        NavEntry(destination) {

                            val maintenanceViewModel: MaintenanceViewModel = viewModel(
                                key = "maintenance-${destination.motorcycleId}",
                                factory = MaintenanceViewModelFactory(
                                    motorcycleId = destination.motorcycleId,
                                    motorcycleRepository = motorcycleRepository,
                                    store = maintenanceStore
                                )
                            )

                            val maintenanceUiState by maintenanceViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            MaintenanceScreen(
                                uiState = maintenanceUiState,
                                intervalFor = maintenanceViewModel::intervalFor,
                                onBackClick = { backStack.removeLastOrNull() },
                                onRetryClick = maintenanceViewModel::retry,
                                onEditOdometerClick = maintenanceViewModel::openOdometerEditor,
                                onItemClick = maintenanceViewModel::openServiceEditor,
                                onDismissEditor = maintenanceViewModel::dismissEditor,
                                onSaveOdometer = maintenanceViewModel::saveOdometer,
                                onSaveService = maintenanceViewModel::saveService,
                                onClearService = maintenanceViewModel::clearService,
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
                                onEditNameClick = profileViewModel::openNameEditor,
                                onDismissNameEditor = profileViewModel::dismissNameEditor,
                                onSaveName = profileViewModel::saveName,
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

                    is TripGalleryDestination ->
                        NavEntry(destination) {

                            val tripPhotosViewModel: TripPhotosViewModel = viewModel(
                                viewModelStoreOwner = mainViewModelOwner,
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

                            TripGalleryScreen(
                                route = destination.route,
                                state = tripPhotosUiState,
                                onBackClick = { backStack.removeLastOrNull() },
                                onAddPhotos = tripPhotosViewModel::addPhotos,
                                onMakeCover = tripPhotosViewModel::makeCover,
                                onRemovePhoto = tripPhotosViewModel::removePhoto,
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

                            val tripCostsViewModel: TripCostsViewModel = viewModel(
                                key = "trip-costs-${destination.tripId}",
                                factory = TripCostsViewModelFactory(
                                    tripId = destination.tripId,
                                    repository = tripCostRepository,
                                    offline = offline
                                )
                            )

                            val tripCostsUiState by tripCostsViewModel
                                .uiState
                                .collectAsStateWithLifecycle()

                            val tripCostsActions = remember(tripCostsViewModel) {
                                TripCostsActions(
                                    onAddFuel = tripCostsViewModel::addFuel,
                                    onAddExpense = tripCostsViewModel::addExpense,
                                    onRecordClick = tripCostsViewModel::editRecord,
                                    onRetry = tripCostsViewModel::retry,
                                    onDismissEditor = tripCostsViewModel::dismissEditor,
                                    onSaveFuel = tripCostsViewModel::saveFuel,
                                    onSaveExpense = tripCostsViewModel::saveExpense,
                                    onDeleteRecord = tripCostsViewModel::deleteEditedRecord
                                )
                            }

                            LaunchedEffect(tripCostsViewModel) {
                                tripCostsViewModel.feedback.collect(showFeedback)
                            }

                            // The cost per km of what is pending needs the trip's distance.
                            LaunchedEffect(
                                (tripDetailsUiState as? TripDetailsUiState.Success)?.trip?.distanceKm
                            ) {
                                (tripDetailsUiState as? TripDetailsUiState.Success)?.trip?.let {
                                    tripCostsViewModel.onTripDistanceKnown(it.distanceKm)
                                }
                            }

                            // Nudges that follow this trip: one if it is left in progress for
                            // too long, one to register its costs after it is finished.
                            val shownTrip = (tripDetailsUiState as? TripDetailsUiState.Success)?.trip
                            val justCompleted =
                                (tripDetailsUiState as? TripDetailsUiState.Success)?.celebration ==
                                    TripCelebration.COMPLETED

                            LaunchedEffect(shownTrip?.id, shownTrip?.status, shownTrip?.startedAt) {
                                val current = shownTrip ?: return@LaunchedEffect

                                if (current.status == "IN_PROGRESS") {
                                    forgottenTripReminder(current, System.currentTimeMillis())
                                        ?.let(reminderScheduler::scheduleOne)
                                } else {
                                    reminderScheduler.cancelOne(current.id, ReminderKind.FORGOTTEN)
                                }
                            }

                            LaunchedEffect(justCompleted) {
                                val current = shownTrip ?: return@LaunchedEffect

                                if (justCompleted && tripCostsUiState.summary?.totalCost == null) {
                                    reminderScheduler.scheduleOne(
                                        costsReminder(current, System.currentTimeMillis())
                                    )
                                }
                            }

                            // Nothing left to nudge about once something is registered.
                            LaunchedEffect(tripCostsUiState.summary?.totalCost) {
                                if (tripCostsUiState.summary?.totalCost != null) {
                                    reminderScheduler.cancelOne(destination.tripId, ReminderKind.COSTS)
                                }
                            }

                            // Coming back from editing the trip, or from another app, what is
                            // on screen may be old: the panel of a trip on the road is live.
                            LifecycleResumeEffect(tripDetailsViewModel, tripCostsViewModel) {
                                tripDetailsViewModel.refresh()
                                tripCostsViewModel.refresh()

                                onPauseOrDispose { }
                            }

                            val tripPhotosViewModel: TripPhotosViewModel = viewModel(
                                viewModelStoreOwner = mainViewModelOwner,
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
                                    reminderScheduler.cancelOne(destination.tripId, ReminderKind.FORGOTTEN)
                                    reminderScheduler.cancelOne(destination.tripId, ReminderKind.COSTS)
                                    // Nothing waiting to be sent can belong to a trip that is gone.
                                    offline.store.removeForTrip(destination.tripId)
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
                                    motorcyclesViewModel.refresh()
                                    backStack.removeLastOrNull()
                                },
                                onStartTripClick = {
                                    tripDetailsViewModel.startTrip()
                                },
                                onCompleteTripClick = tripDetailsViewModel::completeTrip,
                                onConfirmTripDateUpdate =
                                    tripDetailsViewModel::confirmTripDateUpdate,
                                onDismissTripDateUpdate =
                                    tripDetailsViewModel::dismissTripDateUpdateConfirmation,
                                onDeleteTripClick = tripDetailsViewModel::deleteTrip,
                                onCelebrationFinished = tripDetailsViewModel::celebrationFinished,
                                onEditTripClick = {
                                    backStack.add(TripEditDestination(destination.tripId))
                                },
                                onEditTimesClick = tripDetailsViewModel::openTimesEditor,
                                onDismissTimesEditor = tripDetailsViewModel::dismissTimesEditor,
                                onSaveTimes = tripDetailsViewModel::correctTimes,
                                costs = tripCostsUiState,
                                costsActions = tripCostsActions,
                                photos = tripPhotosUiState,
                                onAddPhotos = tripPhotosViewModel::addPhotos,
                                onMakeCoverPhoto = tripPhotosViewModel::makeCover,
                                onRemovePhoto = tripPhotosViewModel::removePhoto,
                                onSeeAllPhotosClick = {
                                    (tripDetailsUiState as? TripDetailsUiState.Success)?.trip?.let { trip ->
                                        backStack.add(
                                            TripGalleryDestination(
                                                tripId = trip.id,
                                                route = "${trip.origin} → ${trip.destination}"
                                            )
                                        )
                                    }
                                },
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

/** The trip form, whether it creates a trip or edits one; the entries own what follows a save. */
@Composable
private fun TripFormScreen(
    viewModel: TripCreateViewModel,
    uiState: TripCreateUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TripCreateScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onModeSelected = viewModel::onModeSelected,
        onOriginChange = viewModel::onOriginChange,
        onDestinationChange = viewModel::onDestinationChange,
        onOriginCitySelected = viewModel::onOriginCitySelected,
        onDestinationCitySelected = viewModel::onDestinationCitySelected,
        onSwapRouteClick = viewModel::swapRoute,
        onDistanceChange = viewModel::onDistanceChange,
        onApplyDistanceSuggestionClick = viewModel::applyDistanceSuggestion,
        onTripDateSelected = viewModel::onTripDateSelected,
        onTerrainSelected = viewModel::onTerrainSelected,
        onMotorcycleSelected = viewModel::onMotorcycleSelected,
        onRetryMotorcyclesClick = viewModel::loadMotorcycles,
        onSubmitClick = viewModel::submit,
        onRetryTripClick = viewModel::loadTrip,
        modifier = modifier
    )
}

/** The motorcycle form, whether it registers a motorcycle or edits one. */
@Composable
private fun MotorcycleFormScreen(
    viewModel: MotorcycleCreateViewModel,
    uiState: MotorcycleCreateUiState,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    MotorcycleCreateScreen(
        uiState = uiState,
        onBackClick = onBackClick,
        onTypeSelected = viewModel::onTypeSelected,
        onBrandChange = viewModel::onBrandChange,
        onModelChange = viewModel::onModelChange,
        onYearChange = viewModel::onYearChange,
        onEngineCapacityChange = viewModel::onEngineCapacityChange,
        onColorSelected = viewModel::onColorSelected,
        onCustomColorSelected = viewModel::onCustomColorSelected,
        onCustomColorChange = viewModel::onCustomColorChange,
        onSubmitClick = viewModel::submit,
        onRetryLoadClick = viewModel::loadMotorcycle,
        modifier = modifier
    )
}
