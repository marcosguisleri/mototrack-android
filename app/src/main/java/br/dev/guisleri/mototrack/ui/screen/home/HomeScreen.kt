package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.SignalCellularAlt
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// ---------------------------------------------------------------------------
// Gradients of the dark trip cards (in-progress / upcoming)
// ---------------------------------------------------------------------------

private val InProgressBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF0F3B45), Color(0xFF0B1F2A))
)

private val UpcomingBrush = Brush.linearGradient(
    colors = listOf(Color(0xFF1E2A44), Color(0xFF0F172A))
)

private val InProgressGreen = Color(0xFF16A34A)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRetryClick: () -> Unit,
    onAddMotorcycleClick: () -> Unit,
    onTripsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        HomeUiState.Loading -> {
            HomeLoading(
                modifier = modifier
            )
        }

        is HomeUiState.Success -> {
            HomeContent(
                home = uiState.home,
                highlight = uiState.highlight,
                onAddMotorcycleClick = onAddMotorcycleClick,
                onTripsClick = onTripsClick,
                modifier = modifier
            )
        }

        is HomeUiState.Error -> {
            HomeError(
                message = uiState.message,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }
    }
}

@Preview(
    name = "Error",
    group = "Home states",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFFF5F7FA
)
@Composable
private fun HomeScreenErrorPreview() {
    MotoTrackTheme {
        HomeScreen(
            uiState = HomeUiState.Error(
                message = "Verifique sua conexão e tente novamente."
            ),
            onRetryClick = {},
            onAddMotorcycleClick = {},
            onTripsClick = {}
        )
    }
}

@Preview(
    name = "Loading",
    group = "Home states",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFFF5F7FA
)
@Composable
private fun HomeScreenLoadingPreview() {
    MotoTrackTheme {
        HomeScreen(
            uiState = HomeUiState.Loading,
            onRetryClick = {},
            onAddMotorcycleClick = {},
            onTripsClick = {}
        )
    }
}

@Composable
private fun HomeContent(
    home: HomeResponseDTO,
    highlight: HomeHighlightState,
    onAddMotorcycleClick: () -> Unit,
    onTripsClick: () -> Unit,
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

        HomeHeader()

        Spacer(modifier = Modifier.height(16.dp))

        HomeHighlightCard(
            highlight = highlight,
            onAddMotorcycleClick = onAddMotorcycleClick,
            onTripsClick = onTripsClick
        )

        if (highlight == HomeHighlightState.NoMotorcycle) {

            Spacer(modifier = Modifier.height(20.dp))

            NoMotorcycleBenefits()
        }

        if (home.motorcycleCount > 0) {

            Spacer(modifier = Modifier.height(24.dp))

            HomeSummarySection(
                totalDistanceKm = home.totalCompletedDistanceKm,
                completedTrips = home.completedTrips,
                distanceThisYearKm = home.distanceThisYearKm
            )
        }

        if (highlight is HomeHighlightState.UpcomingTrip) {

            Spacer(modifier = Modifier.height(12.dp))

            HomeSoftButton(
                text = "Registrar viagem realizada",
                icon = Icons.Outlined.Route,
                onClick = onTripsClick
            )
        }

        home.lastCompletedTrip?.let { trip ->

            Spacer(modifier = Modifier.height(24.dp))

            LastCompletedTripSection(
                trip = trip,
                onClick = onTripsClick
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeader() {
    val darkColor = MaterialTheme.colorScheme.onBackground
    val brandColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = darkColor)) {
                        append("Moto")
                    }
                    withStyle(SpanStyle(color = brandColor)) {
                        append("Track")
                    }
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Pronto para a próxima jornada?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        MountainIllustration(
            modifier = Modifier.size(
                width = 112.dp,
                height = 64.dp
            )
        )
    }
}

// ---------------------------------------------------------------------------
// Highlight card (4 states)
// ---------------------------------------------------------------------------

@Composable
private fun HomeHighlightCard(
    highlight: HomeHighlightState,
    onAddMotorcycleClick: () -> Unit,
    onTripsClick: () -> Unit
) {
    when (highlight) {

        is HomeHighlightState.InProgress -> {
            InProgressTripCard(
                trip = highlight.trip,
                onClick = onTripsClick
            )
        }

        HomeHighlightState.NoMotorcycle -> {
            NoMotorcycleCard(
                onAddMotorcycleClick = onAddMotorcycleClick
            )
        }

        is HomeHighlightState.UpcomingTrip -> {
            UpcomingTripCard(
                trip = highlight.trip,
                onClick = onTripsClick
            )
        }

        HomeHighlightState.NoUpcomingTrip -> {
            NoUpcomingTripCard(
                onTripsClick = onTripsClick
            )
        }
    }
}

@Composable
private fun InProgressTripCard(
    trip: TripSummaryDTO,
    onClick: () -> Unit
) {
    DarkTripCard(
        brush = InProgressBrush,
        badge = {
            TripPill(
                text = "VIAGEM EM ANDAMENTO",
                icon = Icons.Filled.PlayArrow,
                containerColor = InProgressGreen,
                contentColor = Color.White
            )
        },
        origin = trip.origin,
        destination = trip.destination,
        date = formatTripDate(trip.tripDate),
        distance = formatDistance(trip.distanceKm),
        terrain = formatTerrain(trip.terrain),
        motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
        onClick = onClick,
        footer = {
            Spacer(modifier = Modifier.height(16.dp))

            HomePrimaryButton(
                text = "Ver detalhes",
                onClick = onClick
            )
        }
    )
}

@Composable
private fun UpcomingTripCard(
    trip: NextTripDTO,
    onClick: () -> Unit
) {
    val accent = MaterialTheme.colorScheme.secondary

    DarkTripCard(
        brush = UpcomingBrush,
        badge = {
            TripPill(
                text = "PRÓXIMA VIAGEM",
                icon = Icons.Outlined.CalendarMonth,
                containerColor = accent.copy(alpha = 0.25f),
                contentColor = Color.White
            )
        },
        trailing = {
            Text(
                text = formatDaysUntil(trip.daysUntil),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
        },
        origin = trip.origin,
        destination = trip.destination,
        date = formatTripDate(trip.tripDate),
        distance = formatDistance(trip.distanceKm),
        terrain = formatTerrain(trip.terrain),
        motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
        onClick = onClick
    )
}

/**
 * Shared layout of the two dark cards. The badge, the trailing text
 * and the footer are slots, so each state only describes what is different.
 */
@Composable
private fun DarkTripCard(
    brush: Brush,
    badge: @Composable () -> Unit,
    origin: String,
    destination: String,
    date: String,
    distance: String,
    terrain: String,
    motorcycleName: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: @Composable () -> Unit = {},
    footer: @Composable () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(brush)
            .clickable(onClick = onClick)
            .padding(20.dp)
    ) {
        Column {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                badge()
                trailing()
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    RoutePoint(text = origin)

                    Spacer(modifier = Modifier.height(8.dp))

                    RoutePoint(text = destination)
                }

                Spacer(modifier = Modifier.width(12.dp))

                ChevronCircle()
            }

            Spacer(modifier = Modifier.height(16.dp))

            DarkDivider()

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TripFact(
                    icon = Icons.Outlined.CalendarMonth,
                    value = date,
                    caption = "Data",
                    modifier = Modifier.weight(1.3f)
                )

                TripFact(
                    icon = Icons.Outlined.Route,
                    value = distance,
                    caption = "Distância",
                    modifier = Modifier.weight(1f)
                )

                TripFact(
                    icon = Icons.Outlined.Terrain,
                    value = terrain,
                    caption = "Terreno",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            DarkDivider()

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = motorcycleName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }

            footer()
        }
    }
}

@Composable
private fun TripPill(
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
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun RoutePoint(
    text: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.LocationOn,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun ChevronCircle() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "›",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}

@Composable
private fun TripFact(
    icon: ImageVector,
    value: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.width(6.dp))

        Column {
            Text(
                text = value,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = caption,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.85f)
            )
        }
    }
}

@Composable
private fun DarkDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(Color.White.copy(alpha = 0.12f))
    )
}

@Composable
private fun NoMotorcycleCard(
    onAddMotorcycleClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(
                alpha = 0.06f
            )
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            GarageIllustration(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Sua garagem está vazia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Para começar a planejar suas viagens, primeiro cadastre a sua moto.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            HomePrimaryButton(
                text = "Cadastrar moto",
                onClick = onAddMotorcycleClick
            )
        }
    }
}

@Composable
private fun NoUpcomingTripCard(
    onTripsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Planeje sua próxima rota",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Que tal preparar sua próxima viagem? Ou registrar uma viagem que você já fez?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                MountainIllustration(
                    modifier = Modifier.size(
                        width = 88.dp,
                        height = 56.dp
                    )
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            HomePrimaryButton(
                text = "Planejar viagem",
                onClick = onTripsClick
            )

            Spacer(modifier = Modifier.height(10.dp))

            HomeSoftButton(
                text = "Registrar viagem feita",
                icon = Icons.Outlined.Route,
                onClick = onTripsClick
            )
        }
    }
}

@Composable
private fun NoMotorcycleBenefits() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {

            Text(
                text = "Por que cadastrar sua moto?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(16.dp))

            BenefitRow(
                icon = Icons.Outlined.Route,
                text = "Planeje viagens com mais precisão"
            )

            Spacer(modifier = Modifier.height(14.dp))

            BenefitRow(
                icon = Icons.Outlined.CalendarMonth,
                text = "Acompanhe o histórico de cada moto"
            )

            Spacer(modifier = Modifier.height(14.dp))

            BenefitRow(
                icon = Icons.Filled.LocationOn,
                text = "Tenha uma experiência personalizada"
            )
        }
    }
}

@Composable
private fun BenefitRow(
    icon: ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------

@Composable
private fun HomePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp),
        shape = RoundedCornerShape(50)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = text,
                modifier = Modifier.align(Alignment.Center),
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "›",
                modifier = Modifier.align(Alignment.CenterEnd),
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
private fun HomeSoftButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val brandColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(brandColor.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = brandColor
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = text,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = brandColor
        )

        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = brandColor
        )
    }
}

// ---------------------------------------------------------------------------
// Summary and last trip
// ---------------------------------------------------------------------------

@Composable
private fun HomeSummarySection(
    totalDistanceKm: Double,
    completedTrips: Long,
    distanceThisYearKm: Double
) {
    Column {

        Text(
            text = "Resumo das suas viagens",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                icon = Icons.Outlined.Speed,
                value = formatDistance(totalDistanceKm),
                label = "Distância total",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            SummaryMetricCard(
                icon = Icons.Outlined.Route,
                value = completedTrips.toString(),
                label = "Viagens concluídas",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            SummaryMetricCard(
                icon = Icons.Outlined.SignalCellularAlt,
                value = formatDistance(distanceThisYearKm),
                label = "Km neste ano",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LastCompletedTripSection(
    trip: TripSummaryDTO,
    onClick: () -> Unit
) {
    Column {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Última viagem concluída",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Text(
                text = "›",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        LastCompletedTripCard(
            trip = trip,
            onClick = onClick
        )
    }
}

@Composable
private fun LastCompletedTripCard(
    trip: TripSummaryDTO,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // Placeholder for the trip photo shown in the mockup
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF0F3B45),
                                MaterialTheme.colorScheme.primary
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Terrain,
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "${trip.origin} → ${trip.destination}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = buildString {
                        append(formatTripDate(trip.tripDate))
                        append("  •  ")
                        append(formatDistance(trip.distanceKm))
                        append("  •  ")
                        append(formatTerrain(trip.terrain))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Loading and error
// ---------------------------------------------------------------------------

@Composable
private fun HomeLoading(
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

        HomeHeader()

        Spacer(modifier = Modifier.height(16.dp))

        LoadingBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            cornerRadius = 24.dp
        )

        Spacer(modifier = Modifier.height(24.dp))

        LoadingBlock(
            modifier = Modifier
                .width(180.dp)
                .height(20.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(104.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                LoadingBlock(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 18.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        LoadingBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            cornerRadius = 18.dp
        )
    }
}

@Composable
private fun LoadingBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            shape = RoundedCornerShape(cornerRadius)
        )
    )
}

@Composable
private fun HomeError(
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

        HomeHeader()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(
                            alpha = 0.08f
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Não foi possível carregar sua Home",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

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

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Tentar novamente",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Illustrations (drawn with Canvas, no image assets needed)
// ---------------------------------------------------------------------------

@Composable
private fun MountainIllustration(
    modifier: Modifier = Modifier
) {
    val frontColor = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        // Back mountain
        drawPolygon(
            color = Color(0xFF1F3A4D),
            0.05f to 1f,
            0.40f to 0.12f,
            0.75f to 1f
        )
        // Back mountain snowcap
        drawPolygon(
            color = Color.White,
            0.40f to 0.12f,
            0.30f to 0.38f,
            0.35f to 0.33f,
            0.40f to 0.40f,
            0.45f to 0.33f,
            0.50f to 0.38f
        )
        // Front mountain
        drawPolygon(
            color = frontColor,
            0.35f to 1f,
            0.72f to 0.30f,
            1.00f to 1f
        )
        // Front mountain snowcap
        drawPolygon(
            color = Color.White,
            0.72f to 0.30f,
            0.614f to 0.50f,
            0.67f to 0.45f,
            0.72f to 0.52f,
            0.77f to 0.45f,
            0.80f to 0.50f
        )
    }
}

private fun DrawScope.drawPolygon(
    color: Color,
    vararg points: Pair<Float, Float>
) {
    val width = size.width
    val height = size.height

    val path = Path().apply {
        points.forEachIndexed { index, point ->
            val x = width * point.first
            val y = height * point.second

            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(
        path = path,
        color = color
    )
}

@Composable
private fun GarageIllustration(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.BottomCenter
    ) {
        MountainIllustration(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(90.dp)
        )

        Icon(
            imageVector = Icons.Outlined.TwoWheeler,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ---------------------------------------------------------------------------
// Formatters
// ---------------------------------------------------------------------------

private fun formatTripDate(
    date: String
): String {
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

private fun formatDaysUntil(
    daysUntil: Long
): String {
    return when (daysUntil) {
        0L -> "É hoje!"
        1L -> "Amanhã"
        else -> "Daqui a $daysUntil dias"
    }
}

private fun formatDistance(
    distanceKm: Double
): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return "${formatter.format(distanceKm)} km"
}

private fun formatTerrain(
    terrain: String
): String {
    return when (terrain) {
        "ASPHALT" -> "Asfalto"
        "MIXED" -> "Misto"
        "OFF_ROAD" -> "Off-road"
        else -> terrain
    }
}