package br.dev.guisleri.mototrack.ui.screen.trips

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PaintingStyle.Companion.Stroke
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.Path
import androidx.compose.ui.unit.Dp

private val InProgressContainer = Color(0xFFE7F6EC)
private val InProgressContent = Color(0xFF16803D)

private val CompletedContainer = Color(0xFFE9EEF3)
private val CompletedContent = Color(0xFF475569)

@Composable
fun TripsScreen(
    uiState: TripsUiState,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        TripsUiState.Loading -> {
            TripsLoading(
                modifier = modifier
            )
        }

        TripsUiState.Empty -> {
            TripsEmpty(
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
                onRefresh = onRefresh
            ) {
                TripsContent(
                    trips = uiState.trips,
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun TripsContent(
    trips: List<TripResponseDTO>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by rememberSaveable {
        mutableStateOf(TripFilter.ALL)
    }

    val filteredTrips =
        if (selectedFilter.status == null) {
            trips
        } else {
            trips.filter { trip ->
                trip.status == selectedFilter.status
            }
        }

    val inProgressTrips = filteredTrips.filter { trip ->
        trip.status == "IN_PROGRESS"
    }

    val regularTrips = filteredTrips.filterNot { trip ->
        trip.status == "IN_PROGRESS"
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = PaddingValues(
            top = 20.dp,
            bottom = 20.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Viagens",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            TripsFilterBar(
                selectedFilter = selectedFilter,
                onFilterSelected = { filter ->
                    selectedFilter = filter
                }
            )
        }

        if (filteredTrips.isEmpty()) {
            item {
                TripsFilterEmptyState(
                    filter = selectedFilter
                )
            }
        } else {
            items(
                items = inProgressTrips,
                key = { trip -> trip.id }
            ) { trip ->
                InProgressTripCard(
                    trip = trip
                )
            }

            items(
                items = regularTrips,
                key = { trip -> trip.id }
            ) { trip ->
                TripItem(
                    trip = trip
                )
            }
        }
    }
}

@Composable
private fun InProgressTripCard(
    trip: TripResponseDTO
) {
    val backgroundBrush = Brush.linearGradient(
        colors = listOf(
            Color(0xFF0F3B45),
            Color(0xFF0B1F2A)
        )
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundBrush)
            .padding(18.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TripStatusBadge(
                    status = trip.status
                )

                Text(
                    text = "Viagem atual",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = trip.origin,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Text(
                text = trip.destination,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.75f)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            TripRoutePreview()

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            InProgressTripFacts(
                trip = trip
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(
                    modifier = Modifier.width(8.dp)
                )

                Text(
                    text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

@Composable
private fun TripRoutePreview() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(
                Color.White.copy(alpha = 0.06f)
            )
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            val roadColor =
                Color.White.copy(alpha = 0.07f)

            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.25f),
                end = Offset(width, height * 0.55f),
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = roadColor,
                start = Offset(width * 0.15f, 0f),
                end = Offset(width * 0.65f, height),
                strokeWidth = 2.dp.toPx()
            )

            drawLine(
                color = roadColor,
                start = Offset(0f, height * 0.8f),
                end = Offset(width, height * 0.35f),
                strokeWidth = 2.dp.toPx()
            )

            val route = Path().apply {
                moveTo(
                    width * 0.12f,
                    height * 0.75f
                )

                cubicTo(
                    width * 0.30f,
                    height * 0.30f,
                    width * 0.60f,
                    height * 0.80f,
                    width * 0.88f,
                    height * 0.25f
                )
            }

            drawPath(
                path = route,
                color = Color(0xFF48D6D2),
                style = Stroke(
                    width = 5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }

        RouteMarker(
            text = "A",
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(
                    start = 14.dp,
                    bottom = 18.dp
                )
        )

        RouteMarker(
            text = "B",
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(
                    top = 18.dp,
                    end = 14.dp
                )
        )
    }
}

@Composable
private fun RouteMarker(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(28.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF0B1F2A)
        )
    }
}

@Composable
private fun InProgressTripFacts(
    trip: TripResponseDTO
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InProgressTripFact(
            icon = Icons.Outlined.CalendarMonth,
            value = formatTripDate(trip.tripDate),
            modifier = Modifier.weight(1.3f)
        )

        InProgressTripFact(
            icon = Icons.Outlined.Route,
            value = formatDistance(trip.distanceKm),
            modifier = Modifier.weight(1f)
        )

        InProgressTripFact(
            icon = Icons.Outlined.Terrain,
            value = formatTerrain(trip.terrain),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun InProgressTripFact(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.8f),
            maxLines = 1
        )
    }
}

@Composable
private fun TripsLoading(
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
        TripsSkeletonBlock(
            modifier = Modifier
                .width(110.dp)
                .height(32.dp),
            cornerRadius = 8.dp
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(
                    rememberScrollState()
                ),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TripsSkeletonChip(width = 74.dp)
            TripsSkeletonChip(width = 104.dp)
            TripsSkeletonChip(width = 126.dp)
            TripsSkeletonChip(width = 104.dp)
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        TripsSkeletonCard()

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        TripsSkeletonCard()
    }
}

@Composable
private fun TripsSkeletonChip(
    width: Dp
) {
    TripsSkeletonBlock(
        modifier = Modifier
            .width(width)
            .height(34.dp),
        cornerRadius = 50.dp
    )
}

@Composable
private fun TripsSkeletonCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            TripsSkeletonBlock(
                modifier = Modifier
                    .width(88.dp)
                    .height(24.dp),
                cornerRadius = 50.dp
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            TripsSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(18.dp)
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            TripsSkeletonBlock(
                modifier = Modifier
                    .fillMaxWidth(0.75f)
                    .height(18.dp)
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = 0.4f
                )
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    TripsSkeletonBlock(
                        modifier = Modifier
                            .weight(1f)
                            .height(18.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            TripsSkeletonBlock(
                modifier = Modifier
                    .width(140.dp)
                    .height(16.dp)
            )
        }
    }
}

@Composable
private fun TripsSkeletonBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.06f
            ),
            shape = RoundedCornerShape(cornerRadius)
        )
    )
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
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {
        Text(
            text = "Viagens",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.WifiOff,
                        contentDescription = null,
                        modifier = Modifier.size(54.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Não foi possível carregar suas viagens",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Button(
                    onClick = onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(50)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier = Modifier.width(8.dp)
                    )

                    Text(
                        text = "Tentar novamente",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun TripsFilterEmptyState(
    filter: TripFilter
) {
    val message = when (filter) {
        TripFilter.ALL ->
            "Nenhuma viagem encontrada"

        TripFilter.PLANNED ->
            "Nenhuma viagem planejada"

        TripFilter.IN_PROGRESS ->
            "Nenhuma viagem em andamento"

        TripFilter.COMPLETED ->
            "Nenhuma viagem concluída"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                top = 48.dp,
                bottom = 24.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Outlined.Route,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.7f
            )
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun TripsEmpty(
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
        Text(
            text = "Viagens",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary
                                .copy(alpha = 0.08f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Route,
                        contentDescription = null,
                        modifier = Modifier.size(58.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Você ainda não tem nenhuma viagem",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(8.dp)
                )

                Text(
                    text = "Planeje sua próxima aventura ou registre uma viagem que você já realizou.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun TripItem(
    trip: TripResponseDTO
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TripStatusBadge(
                    status = trip.status
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            TripRoute(
                origin = trip.origin,
                destination = trip.destination
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = 0.5f
                )
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            TripFacts(
                trip = trip
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            MotorcycleInfo(
                motorcycleName =
                    "${trip.motorcycle.brand} ${trip.motorcycle.model}"
            )
        }
    }
}

@Composable
private fun TripsFilterBar(
    selectedFilter: TripFilter,
    onFilterSelected: (TripFilter) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(
                rememberScrollState()
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TripFilter.entries.forEach { filter ->
            TripFilterChip(
                label = filter.label,
                selected = filter == selectedFilter,
                onClick = {
                    onFilterSelected(filter)
                }
            )
        }

        Spacer(
            modifier = Modifier.width(8.dp)
        )
    }
}

@Composable
private fun TripFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val primary = MaterialTheme.colorScheme.primary

    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                fontWeight = if (selected) {
                    FontWeight.SemiBold
                } else {
                    FontWeight.Normal
                }
            )
        },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            containerColor =
                MaterialTheme.colorScheme.surface,

            labelColor =
                MaterialTheme.colorScheme.onSurfaceVariant,

            selectedContainerColor =
                primary.copy(alpha = 0.12f),

            selectedLabelColor = primary
        )
    )
}

@Composable
private fun TripFacts(
    trip: TripResponseDTO
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TripFact(
            icon = Icons.Outlined.CalendarMonth,
            value = formatTripDate(trip.tripDate),
            modifier = Modifier.weight(1.3f)
        )

        TripFact(
            icon = Icons.Outlined.Route,
            value = formatDistance(trip.distanceKm),
            modifier = Modifier.weight(1f)
        )

        TripFact(
            icon = Icons.Outlined.Terrain,
            value = formatTerrain(trip.terrain),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TripFact(
    icon: ImageVector,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(17.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.width(5.dp)
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
    }
}

@Composable
private fun TripRoute(
    origin: String,
    destination: String
) {
    Column {
        TripRoutePoint(
            text = origin
        )

        Box(
            modifier = Modifier
                .padding(start = 9.dp)
                .width(2.dp)
                .height(14.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.35f
                    )
                )
        )

        TripRoutePoint(
            text = destination
        )
    }
}

@Composable
private fun TripRoutePoint(
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.12f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MotorcycleInfo(
    motorcycleName: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.TwoWheeler,
            contentDescription = null,
            modifier = Modifier.size(19.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(
            text = motorcycleName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TripStatusBadge(
    status: String
) {
    val containerColor: Color
    val contentColor: Color

    when (status) {
        "PLANNED" -> {
            containerColor = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.12f
            )
            contentColor = MaterialTheme.colorScheme.primary
        }

        "IN_PROGRESS" -> {
            containerColor = InProgressContainer
            contentColor = InProgressContent
        }

        "COMPLETED" -> {
            containerColor = CompletedContainer
            contentColor = CompletedContent
        }

        else -> {
            containerColor = MaterialTheme.colorScheme.surfaceVariant
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        }
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(
                horizontal = 10.dp,
                vertical = 5.dp
            )
    ) {
        Text(
            text = formatTripStatus(status).uppercase(),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

private enum class TripFilter(
    val label: String,
    val status: String?
) {
    ALL(
        label = "Todas",
        status = null
    ),
    PLANNED(
        label = "Planejadas",
        status = "PLANNED"
    ),
    IN_PROGRESS(
        label = "Em andamento",
        status = "IN_PROGRESS"
    ),
    COMPLETED(
        label = "Concluídas",
        status = "COMPLETED"
    )
}

private fun formatTripDate(date: String): String {
    return runCatching {
        LocalDate
            .parse(date)
            .format(
                DateTimeFormatter.ofPattern(
                    "d MMM yyyy",
                    Locale.forLanguageTag("pt-BR")
                )
            )
            .replace(".", "")
    }.getOrDefault(date)
}

private fun formatDistance(distanceKm: Double): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return "${formatter.format(distanceKm)} km"
}

private fun formatTerrain(terrain: String): String {
    return when (terrain) {
        "ASPHALT" -> "Asfalto"
        "MIXED" -> "Misto"
        "OFF_ROAD" -> "Off-road"
        else -> terrain
    }
}

private fun formatTripStatus(status: String): String {
    return when (status) {
        "PLANNED" -> "Planejada"
        "IN_PROGRESS" -> "Em andamento"
        "COMPLETED" -> "Concluída"
        else -> status
    }
}