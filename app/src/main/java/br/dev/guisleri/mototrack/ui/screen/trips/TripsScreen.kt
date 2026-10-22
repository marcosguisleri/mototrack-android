package br.dev.guisleri.mototrack.ui.screen.trips

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.HeroPill
import br.dev.guisleri.mototrack.ui.components.BrandMark
import br.dev.guisleri.mototrack.ui.components.FirstTripIllustration
import br.dev.guisleri.mototrack.ui.components.NoMotorcycleHero
import br.dev.guisleri.mototrack.ui.components.PlanTripHero
import br.dev.guisleri.mototrack.ui.components.PulsingDot
import br.dev.guisleri.mototrack.ui.components.RideButton
import br.dev.guisleri.mototrack.ui.components.RideOutlinedButton
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.RouteTimeline
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.components.TripFacts
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatNumber
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.theme.EmberHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import br.dev.guisleri.mototrack.ui.theme.WarmSuccessTint
import java.text.Normalizer
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** A search only appears once the list is long enough to need one. */
private const val SEARCH_MIN_TRIPS = 4

private enum class TripFilter(
    val label: String,
    val status: String?
) {
    ALL("Todas", null),
    PLANNED("Planejadas", "PLANNED"),
    IN_PROGRESS("Em andamento", "IN_PROGRESS"),
    COMPLETED("Concluídas", "COMPLETED")
}

@Composable
fun TripsScreen(
    uiState: TripsUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    onTripClick: (Long) -> Unit,
    onCreateTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit,
    hasMotorcycles: Boolean,
    onAddMotorcycleClick: () -> Unit,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    motorcycleFilterId: Long? = null,
    motorcycleFilterName: String? = null,
    onClearMotorcycleFilterClick: () -> Unit = {}
) {
    when (uiState) {

        TripsUiState.Loading -> {
            TripsLoading(
                modifier = modifier
            )
        }

        TripsUiState.Empty -> {
            TripsEmpty(
                hasMotorcycles = hasMotorcycles,
                onGoHomeClick = onGoHomeClick,
                onAddMotorcycleClick = onAddMotorcycleClick,
                modifier = modifier
            )
        }

        is TripsUiState.Error -> {
            TripsError(
                message = uiState.message,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }

        is TripsUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                TripsContent(
                    trips = uiState.trips,
                    refreshErrorMessage = uiState.refreshErrorMessage,
                    onTripClick = onTripClick,
                    onCreateTripClick = onCreateTripClick,
                    onRegisterCompletedTripClick = onRegisterCompletedTripClick,
                    motorcycleFilterId = motorcycleFilterId,
                    motorcycleFilterName = motorcycleFilterName,
                    onClearMotorcycleFilterClick = onClearMotorcycleFilterClick,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Content
// ---------------------------------------------------------------------------

@Composable
private fun TripsContent(
    trips: List<TripResponseDTO>,
    refreshErrorMessage: String?,
    onTripClick: (Long) -> Unit,
    onCreateTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit,
    motorcycleFilterId: Long?,
    motorcycleFilterName: String?,
    onClearMotorcycleFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Coming from "full history" of a motorcycle narrows the whole screen to it.
    val scopedTrips = remember(trips, motorcycleFilterId) {
        if (motorcycleFilterId == null) {
            trips
        } else {
            trips.filter { it.motorcycle.id == motorcycleFilterId }
        }
    }

    var selectedFilter by rememberSaveable {
        mutableStateOf(TripFilter.ALL)
    }

    var query by rememberSaveable {
        mutableStateOf("")
    }

    val inProgressAll = remember(scopedTrips) {
        scopedTrips.filter { it.status == "IN_PROGRESS" }
            .sortedByDescending { it.tripDate }
    }
    val plannedAll = remember(scopedTrips) {
        scopedTrips.filter { it.status == "PLANNED" }
            .sortedBy { it.tripDate }
    }
    val completedAll = remember(scopedTrips) {
        scopedTrips.filter { it.status == "COMPLETED" }
            .sortedByDescending { it.tripDate }
    }

    val counts = mapOf(
        TripFilter.ALL to scopedTrips.size,
        TripFilter.PLANNED to plannedAll.size,
        TripFilter.IN_PROGRESS to inProgressAll.size,
        TripFilter.COMPLETED to completedAll.size
    )

    val totalCompletedKm = remember(completedAll) {
        completedAll.sumOf { it.distanceKm }
    }

    val showSearch = trips.size >= SEARCH_MIN_TRIPS
    val activeQuery = if (showSearch) query.trim() else ""

    val inProgress = inProgressAll.matching(activeQuery)
    val planned = plannedAll.matching(activeQuery)
    val completed = completedAll.matching(activeQuery)

    val showInProgress = selectedFilter.status.let { it == null || it == "IN_PROGRESS" }
    val showPlanned = selectedFilter.status.let { it == null || it == "PLANNED" }
    val showCompleted = selectedFilter.status.let { it == null || it == "COMPLETED" }

    val visibleCount =
        (if (showInProgress) inProgress.size else 0) +
                (if (showPlanned) planned.size else 0) +
                (if (showCompleted) completed.size else 0)

    val showSectionHeaders = selectedFilter == TripFilter.ALL

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(
            start = 20.dp,
            end = 20.dp,
            top = 20.dp,
            bottom = 28.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        item(key = "header") {
            TripsHeader(
                subtitle = tripsSummary(
                    count = scopedTrips.size,
                    completedKm = totalCompletedKm
                ),
                onCreateTripClick = onCreateTripClick
            )
        }

        if (motorcycleFilterId != null) {
            item(key = "motorcycle-filter") {
                MotorcycleFilterPill(
                    name = motorcycleFilterName ?: "Moto selecionada",
                    onClearClick = onClearMotorcycleFilterClick
                )
            }
        }

        refreshErrorMessage?.let { message ->
            item(key = "refresh-error") {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        if (showSearch) {
            item(key = "search") {
                TripsSearchField(
                    query = query,
                    onQueryChange = { query = it }
                )
            }
        }

        item(key = "filters") {
            TripsFilterBar(
                selectedFilter = selectedFilter,
                counts = counts,
                onFilterSelected = { selectedFilter = it }
            )
        }

        if (visibleCount == 0) {
            item(key = "filter-empty") {
                TripsFilterEmptyState(
                    filter = selectedFilter,
                    query = activeQuery,
                    onClearSearchClick = { query = "" },
                    onCreateTripClick = onCreateTripClick,
                    onRegisterCompletedTripClick = onRegisterCompletedTripClick
                )
            }
        }

        if (showInProgress && inProgress.isNotEmpty()) {
            if (showSectionHeaders) {
                item(key = "section-in-progress") {
                    SectionTitle("Em andamento", inProgress.size)
                }
            }

            items(inProgress, key = { "trip-${it.id}" }) { trip ->
                InProgressTripCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        if (showPlanned && planned.isNotEmpty()) {
            if (showSectionHeaders) {
                item(key = "section-planned") {
                    SectionTitle("Próximas", planned.size)
                }
            }

            items(planned, key = { "trip-${it.id}" }) { trip ->
                PlannedTripCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }

        if (showCompleted && completed.isNotEmpty()) {
            if (showSectionHeaders) {
                item(key = "section-completed") {
                    SectionTitle("Concluídas", completed.size)
                }
            }

            items(completed, key = { "trip-${it.id}" }) { trip ->
                CompletedTripCard(
                    trip = trip,
                    onClick = { onTripClick(trip.id) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

@Composable
private fun LazyItemScope.SectionTitle(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = count.toString(),
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(WarmHairline)
                .padding(
                    horizontal = 10.dp,
                    vertical = 2.dp
                ),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = WarmInkSoft
        )
    }
}

// ---------------------------------------------------------------------------
// Header, search and filters
// ---------------------------------------------------------------------------

@Composable
private fun TripsHeader(
    subtitle: String,
    onCreateTripClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            BrandMark(modifier = Modifier.padding(bottom = 10.dp))

            Text(
                text = "Viagens",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Button(
            onClick = onCreateTripClick,
            shape = RoundedCornerShape(50),
            contentPadding = PaddingValues(
                start = 14.dp,
                end = 18.dp
            ),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmInk,
                contentColor = Color.White
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                text = "Nova",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MotorcycleFilterPill(
    name: String,
    onClearClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(WarmEmberTint)
            .clickable(
                onClickLabel = "Remover filtro de moto",
                role = Role.Button,
                onClick = onClearClick
            )
            .padding(
                start = 14.dp,
                end = 10.dp,
                top = 8.dp,
                bottom = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.TwoWheeler,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = WarmEmberDeep
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Só viagens da $name",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = WarmEmberDeep,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Icon(
            imageVector = Icons.Outlined.Close,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = WarmEmberDeep
        )
    }
}

@Composable
private fun TripsSearchField(
    query: String,
    onQueryChange: (String) -> Unit
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        placeholder = {
            Text(text = "Buscar por cidade ou moto")
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Outlined.Search,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Outlined.Close,
                        contentDescription = "Limpar busca",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            null
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = { keyboardController?.hide() }
        ),
        shape = RoundedCornerShape(18.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = WarmCard,
            unfocusedContainerColor = WarmCard,
            focusedBorderColor = WarmEmber,
            unfocusedBorderColor = WarmHairline,
            focusedLeadingIconColor = WarmEmber,
            unfocusedLeadingIconColor = WarmMuted,
            focusedTrailingIconColor = WarmInkSoft,
            unfocusedTrailingIconColor = WarmInkSoft,
            focusedPlaceholderColor = WarmMuted,
            unfocusedPlaceholderColor = WarmMuted,
            focusedTextColor = WarmInk,
            unfocusedTextColor = WarmInk,
            cursorColor = WarmEmber
        )
    )
}

@Composable
private fun TripsFilterBar(
    selectedFilter: TripFilter,
    counts: Map<TripFilter, Int>,
    onFilterSelected: (TripFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TripFilter.entries.forEach { filter ->
            FilterPill(
                label = filter.label,
                count = counts[filter] ?: 0,
                selected = filter == selectedFilter,
                onClick = { onFilterSelected(filter) }
            )
        }
    }
}

@Composable
private fun FilterPill(
    label: String,
    count: Int,
    selected: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(50)

    Row(
        modifier = Modifier
            .clip(shape)
            .background(if (selected) WarmInk else WarmCard)
            .border(
                border = BorderStroke(
                    width = 1.dp,
                    color = if (selected) WarmInk else WarmHairline
                ),
                shape = shape
            )
            .selectable(
                selected = selected,
                role = Role.Tab,
                onClick = onClick
            )
            .padding(
                horizontal = 14.dp,
                vertical = 9.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else WarmInkSoft,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = if (selected) Color.White.copy(alpha = 0.7f) else WarmMuted
        )
    }
}

// ---------------------------------------------------------------------------
// Trip cards
// ---------------------------------------------------------------------------

@Composable
private fun InProgressTripCard(
    trip: TripResponseDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(EmberHeroBrush)
            .clickable(
                onClickLabel = "Abrir viagem em andamento",
                role = Role.Button,
                onClick = onClick
            )
    ) {
        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeroPill(
                    text = "EM ANDAMENTO",
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White,
                    leading = {
                        PulsingDot(color = Color.White)
                    }
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            RouteTimeline(
                origin = trip.origin,
                destination = trip.destination,
                textColor = Color.White,
                accentColor = Color.White,
                lineColor = Color.White.copy(alpha = 0.55f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            TripFacts(
                distance = formatDistance(trip.distanceKm),
                terrain = formatTerrain(trip.terrain),
                date = formatTripDate(trip.tripDate),
                motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                chipColor = Color.White.copy(alpha = 0.18f),
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun PlannedTripCard(
    trip: TripResponseDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val daysUntil = daysUntil(trip.tripDate)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                onClickLabel = "Abrir viagem planejada",
                role = Role.Button,
                onClick = onClick
            )
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusPill(
                text = "PLANEJADA",
                icon = Icons.Outlined.CalendarMonth,
                containerColor = WarmEmberTint,
                contentColor = WarmEmberDeep
            )

            Text(
                text = formatCountdown(daysUntil),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (daysUntil != null && daysUntil < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    WarmEmber
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        RouteTimeline(
            origin = trip.origin,
            destination = trip.destination,
            textColor = WarmInk,
            accentColor = WarmEmber,
            lineColor = WarmHairline
        )

        Spacer(modifier = Modifier.height(16.dp))

        TripFacts(
            distance = formatDistance(trip.distanceKm),
            terrain = formatTerrain(trip.terrain),
            date = formatTripDate(trip.tripDate),
            motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
            chipColor = WarmCanvas,
            contentColor = WarmInkSoft
        )
    }
}

/** Finished trips are the long tail of the list, so they stay compact. */
@Composable
private fun CompletedTripCard(
    trip: TripResponseDTO,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(22.dp)
            )
            .clickable(
                onClickLabel = "Abrir viagem concluída",
                role = Role.Button,
                onClick = onClick
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(WarmSuccessTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Concluída",
                modifier = Modifier.size(24.dp),
                tint = WarmSuccess
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = WarmInk,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${formatTripDate(trip.tripDate)} · " +
                        "${formatDistance(trip.distanceKm)} · " +
                        formatTerrain(trip.terrain),
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkSoft,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = WarmMuted
        )
    }
}

@Composable
private fun StatusPill(
    text: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = contentColor
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.sp,
            color = contentColor
        )
    }
}

// ---------------------------------------------------------------------------
// Empty, loading and error
// ---------------------------------------------------------------------------

@Composable
private fun TripsEmpty(
    hasMotorcycles: Boolean,
    onGoHomeClick: () -> Unit,
    onAddMotorcycleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {
        BrandMark(modifier = Modifier.padding(bottom = 10.dp))

        Text(
            text = "Viagens",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = "Suas aventuras em um só lugar.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkSoft
        )

        // The message sits in the middle of what is left, not stuck under the title.
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (hasMotorcycles) {
                    FirstTripIllustration(
                        modifier = Modifier.size(width = 240.dp, height = 160.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Nenhuma viagem no seu histórico",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmInk,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Quando você planejar ou registrar uma viagem, ela aparece " +
                                "aqui. Que tal começar pela Home?",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkSoft,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    RideButton(
                        text = "Ir para a Home",
                        onClick = onGoHomeClick,
                        containerColor = WarmInk,
                        contentColor = Color.White
                    )
                } else {
                    NoMotorcycleHero(
                        onAddMotorcycleClick = onAddMotorcycleClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TripsFilterEmptyState(
    filter: TripFilter,
    query: String,
    onClearSearchClick: () -> Unit,
    onCreateTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit
) {
    val title: String
    val message: String

    when {
        query.isNotEmpty() -> {
            title = "Nada encontrado"
            message = "Não encontramos viagens para “$query”."
        }

        filter == TripFilter.PLANNED -> {
            title = "Nenhuma viagem planejada"
            message = "Que tal preparar a próxima aventura?"
        }

        filter == TripFilter.ALL -> {
            title = "Nenhuma viagem por aqui"
            message = "Esta moto ainda não tem viagens. Que tal planejar a primeira?"
        }

        filter == TripFilter.IN_PROGRESS -> {
            title = "Nenhuma viagem em andamento"
            message = "Quando você iniciar uma viagem planejada, ela aparece aqui."
        }

        else -> {
            title = "Nenhuma viagem concluída"
            message = "Registre uma viagem que você já fez para montar o seu histórico."
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.SearchOff,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkSoft,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            query.isNotEmpty() -> RideOutlinedButton(
                text = "Limpar busca",
                icon = Icons.Outlined.Close,
                onClick = onClearSearchClick,
                color = WarmInk
            )

            filter == TripFilter.PLANNED || filter == TripFilter.ALL -> RideButton(
                text = "Planejar viagem",
                onClick = onCreateTripClick,
                containerColor = WarmInk,
                contentColor = Color.White
            )

            filter == TripFilter.COMPLETED -> RideButton(
                text = "Registrar viagem feita",
                onClick = onRegisterCompletedTripClick,
                containerColor = WarmInk,
                contentColor = Color.White
            )
        }
    }
}

@Composable
private fun TripsLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        BrandMark()

        Text(
            text = "Viagens",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(3) {
                ShimmerBlock(
                    modifier = Modifier
                        .width(92.dp)
                        .height(38.dp),
                    cornerRadius = 50.dp
                )
            }
        }

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            cornerRadius = 28.dp
        )

        repeat(2) {
            ShimmerBlock(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp),
                cornerRadius = 22.dp
            )
        }
    }
}

@Composable
private fun TripsError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {
        Text(
            text = "Viagens",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(WarmEmberTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = WarmEmber
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Não foi possível carregar suas viagens",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmInk,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Tentar novamente",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun tripsSummary(
    count: Int,
    completedKm: Double
): String {
    val trips = if (count == 1) "1 viagem" else "$count viagens"

    return if (completedKm > 0) {
        "$trips · ${formatNumber(completedKm)} km percorridos"
    } else {
        trips
    }
}

private fun daysUntil(date: String): Long? =
    runCatching {
        ChronoUnit.DAYS.between(LocalDate.now(), LocalDate.parse(date))
    }.getOrNull()

private fun formatCountdown(daysUntil: Long?): String =
    when {
        daysUntil == null -> ""
        daysUntil < 0 -> "Data passada"
        daysUntil == 0L -> "É hoje!"
        daysUntil == 1L -> "Amanhã"
        else -> "Em $daysUntil dias"
    }

private fun List<TripResponseDTO>.matching(query: String): List<TripResponseDTO> {
    if (query.isEmpty()) return this

    val needle = query.normalizedForSearch()

    return filter { trip ->
        listOf(
            trip.origin,
            trip.destination,
            "${trip.motorcycle.brand} ${trip.motorcycle.model}"
        ).any { it.normalizedForSearch().contains(needle) }
    }
}

private fun String.normalizedForSearch(): String =
    Normalizer.normalize(this, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "")
        .lowercase()
