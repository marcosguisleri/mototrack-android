package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.runtime.getValue
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO
import br.dev.guisleri.mototrack.ui.components.GarageIllustration
import br.dev.guisleri.mototrack.ui.components.HeroPill
import br.dev.guisleri.mototrack.ui.components.PulsingDot
import br.dev.guisleri.mototrack.ui.components.NoMotorcycleHero
import br.dev.guisleri.mototrack.ui.components.PlanTripHero
import br.dev.guisleri.mototrack.ui.components.RideButton
import br.dev.guisleri.mototrack.ui.components.RideOutlinedButton
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.RouteTimeline
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.components.SunriseIllustration
import br.dev.guisleri.mototrack.ui.components.TripFacts
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatNumber
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.theme.EmberHeroBrush
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.SunriseHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import br.dev.guisleri.mototrack.ui.theme.WarmSuccessTint
import java.text.NumberFormat
import java.time.LocalTime
import java.util.Locale


// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRetryClick: () -> Unit,
    onRefresh: () -> Unit,
    onAddMotorcycleClick: () -> Unit,
    onTripsClick: () -> Unit,
    onTripClick: (Long) -> Unit,
    onPlanTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit,
    modifier: Modifier = Modifier,
    motorcycle: MotorcycleResponseDTO? = null,
    trips: List<TripResponseDTO>? = null,
    onGarageClick: () -> Unit = {}
) {
    when (uiState) {

        HomeUiState.Loading -> {
            HomeLoading(
                modifier = modifier
            )
        }

        is HomeUiState.Success -> {
            PullToRefreshBox(
                isRefreshing = uiState.isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                HomeContent(
                    home = uiState.home,
                    highlight = uiState.highlight,
                    refreshErrorMessage = uiState.refreshErrorMessage,
                    onAddMotorcycleClick = onAddMotorcycleClick,
                    onTripsClick = onTripsClick,
                    onTripClick = onTripClick,
                    onPlanTripClick = onPlanTripClick,
                    onRegisterCompletedTripClick = onRegisterCompletedTripClick,
                    motorcycle = motorcycle,
                    trips = trips,
                    userName = uiState.userName,
                    onGarageClick = onGarageClick
                )
            }
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

@Composable
private fun HomeContent(
    home: HomeResponseDTO,
    highlight: HomeHighlightState,
    refreshErrorMessage: String?,
    onAddMotorcycleClick: () -> Unit,
    onTripsClick: () -> Unit,
    onTripClick: (Long) -> Unit,
    onPlanTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit,
    motorcycle: MotorcycleResponseDTO?,
    trips: List<TripResponseDTO>?,
    userName: String?,
    onGarageClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isFirstTrip =
        home.completedTrips == 0L && home.lastCompletedTrip == null

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 20.dp,
                bottom = 28.dp
            )
    ) {

        val subtitle = when (highlight) {
            is HomeHighlightState.InProgress ->
                "Boa viagem! Siga firme na estrada."

            HomeHighlightState.NoMotorcycle ->
                "Vamos montar a sua garagem?"

            is HomeHighlightState.UpcomingTrip ->
                "Falta pouco para a próxima aventura."

            HomeHighlightState.NoUpcomingTrip ->
                if (isFirstTrip) {
                    "Sua primeira viagem está te esperando."
                } else {
                    "Para onde vamos agora?"
                }
        }

        // Beside the motorcycle there is room for a single short line only.
        val shortSubtitle = when (highlight) {
            is HomeHighlightState.InProgress -> "Boa viagem!"
            is HomeHighlightState.UpcomingTrip -> "Falta pouco!"
            else -> if (isFirstTrip) "Vamos viajar?" else "Para onde agora?"
        }

        if (motorcycle != null && home.motorcycleCount > 0) {
            HomeTopSection(
                subtitle = shortSubtitle,
                userName = userName,
                motorcycle = motorcycle,
                onGarageClick = onGarageClick
            )
        } else {
            HomeHeader(
                subtitle = subtitle,
                userName = userName
            )
        }

        refreshErrorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (highlight) {

            is HomeHighlightState.InProgress -> {
                InProgressHero(
                    trip = highlight.trip,
                    onClick = {
                        onTripClick(highlight.trip.id)
                    }
                )
            }

            HomeHighlightState.NoMotorcycle -> {
                NoMotorcycleHero(
                    onAddMotorcycleClick = onAddMotorcycleClick
                )
            }

            is HomeHighlightState.UpcomingTrip -> {
                UpcomingHero(
                    trip = highlight.trip,
                    onClick = {
                        onTripClick(highlight.trip.id)
                    }
                )
            }

            HomeHighlightState.NoUpcomingTrip -> {
                PlanTripHero(
                    isFirstTrip = isFirstTrip,
                    onPlanTripClick = onPlanTripClick,
                    onRegisterCompletedTripClick = onRegisterCompletedTripClick
                )
            }
        }

        if (
            highlight is HomeHighlightState.InProgress ||
            highlight is HomeHighlightState.UpcomingTrip
        ) {
            Spacer(modifier = Modifier.height(14.dp))

            QuickActions(
                onPlanTripClick = onPlanTripClick,
                onRegisterCompletedTripClick = onRegisterCompletedTripClick
            )
        }

        if (highlight == HomeHighlightState.NoMotorcycle) {
            Spacer(modifier = Modifier.height(20.dp))

            NoMotorcycleBenefits()
        }

        if (home.motorcycleCount > 0) {
            Spacer(modifier = Modifier.height(28.dp))

            RoadStatsCard(
                totalDistanceKm = home.totalCompletedDistanceKm,
                distanceThisYearKm = home.distanceThisYearKm,
                completedTrips = home.completedTrips,
                motorcycleCount = home.motorcycleCount,
                monthlyKm = trips?.let {
                    monthlyCompletedDistanceKm(it, java.time.LocalDate.now().year)
                }
            )
        }

        home.lastCompletedTrip?.let { trip ->
            Spacer(modifier = Modifier.height(28.dp))

            SectionHeader(
                title = "Última viagem",
                actionLabel = "Ver histórico",
                onActionClick = onTripsClick
            )

            Spacer(modifier = Modifier.height(12.dp))

            LastTripCard(
                trip = trip,
                onClick = {
                    onTripClick(trip.id)
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun HomeHeader(
    subtitle: String,
    userName: String? = null
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = WarmInk)) {
                    append("MOTO")
                }
                withStyle(SpanStyle(color = WarmEmber)) {
                    append("TRACK")
                }
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        HomeGreeting(userName = userName)

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = WarmInkSoft
        )
    }
}

// ---------------------------------------------------------------------------
// Heroes
// ---------------------------------------------------------------------------

@Composable
private fun InProgressHero(
    trip: TripSummaryDTO,
    onClick: () -> Unit
) {
    HeroCard(
        brush = EmberHeroBrush,
        onClick = onClick,
        onClickLabel = "Abrir viagem em andamento",
        decoration = {
            RoadDecoration(
                modifier = Modifier.matchParentSize(),
                color = Color.White
            )
        }
    ) {
        HeroPill(
            text = "EM ANDAMENTO",
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White,
            leading = {
                PulsingDot(color = Color.White)
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        RouteTimeline(
            origin = trip.origin,
            destination = trip.destination,
            textColor = Color.White,
            accentColor = Color.White,
            lineColor = Color.White.copy(alpha = 0.55f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        TripFacts(
            distance = formatDistance(trip.distanceKm),
            terrain = formatTerrain(trip.terrain),
            date = formatTripDate(trip.tripDate),
            motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
            chipColor = Color.White.copy(alpha = 0.18f),
            contentColor = Color.White
        )

        Spacer(modifier = Modifier.height(20.dp))

        RideButton(
            text = "Continuar viagem",
            onClick = onClick,
            containerColor = Color.White,
            contentColor = WarmEmberDeep
        )
    }
}

@Composable
private fun UpcomingHero(
    trip: NextTripDTO,
    onClick: () -> Unit
) {
    HeroCard(
        brush = GraphiteHeroBrush,
        onClick = onClick,
        onClickLabel = "Abrir próxima viagem",
        decoration = {
            RoadDecoration(
                modifier = Modifier.matchParentSize(),
                color = Color.White
            )
        }
    ) {
        HeroPill(
            text = "PRÓXIMA VIAGEM",
            containerColor = WarmEmber.copy(alpha = 0.2f),
            contentColor = WarmEmberOnDark,
            leading = {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = WarmEmberOnDark
                )
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        Countdown(daysUntil = trip.daysUntil)

        Spacer(modifier = Modifier.height(20.dp))

        RouteTimeline(
            origin = trip.origin,
            destination = trip.destination,
            textColor = Color.White,
            accentColor = WarmEmberOnDark,
            lineColor = Color.White.copy(alpha = 0.35f)
        )

        Spacer(modifier = Modifier.height(18.dp))

        TripFacts(
            distance = formatDistance(trip.distanceKm),
            terrain = formatTerrain(trip.terrain),
            date = formatTripDate(trip.tripDate),
            motorcycleName = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
            chipColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ver detalhes",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarmEmberOnDark
            )

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = WarmEmberOnDark
            )
        }
    }
}



/** Shared frame of the full-bleed heroes; the content is a free slot. */
@Composable
private fun HeroCard(
    brush: Brush,
    onClick: () -> Unit,
    onClickLabel: String,
    decoration: @Composable BoxScope.() -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(brush)
            .clickable(
                onClickLabel = onClickLabel,
                role = Role.Button,
                onClick = onClick
            )
    ) {
        decoration()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            content = content
        )
    }
}



@Composable
private fun Countdown(
    daysUntil: Long
) {
    when {
        daysUntil <= 0L -> {
            Text(
                text = "É hoje!",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = WarmEmberOnDark
            )
        }

        daysUntil == 1L -> {
            Text(
                text = "Amanhã",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = WarmEmberOnDark
            )
        }

        else -> {
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = NumberFormat
                        .getIntegerInstance(Locale.forLanguageTag("pt-BR"))
                        .format(daysUntil),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmEmberOnDark,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column(
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "dias",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        text = "para a partida",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.65f)
                    )
                }
            }
        }
    }
}




// ---------------------------------------------------------------------------
// Quick actions
// ---------------------------------------------------------------------------

@Composable
private fun QuickActions(
    onPlanTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionTile(
            icon = Icons.Outlined.CalendarMonth,
            title = "Planejar",
            subtitle = "Nova viagem",
            onClick = onPlanTripClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        QuickActionTile(
            icon = Icons.Outlined.TaskAlt,
            title = "Registrar",
            subtitle = "Viagem feita",
            onClick = onRegisterCompletedTripClick,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun QuickActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(
                role = Role.Button,
                onClick = onClick
            )
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = WarmInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Road stats
// ---------------------------------------------------------------------------



// ---------------------------------------------------------------------------
// Last trip
// ---------------------------------------------------------------------------

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .clickable(
                    role = Role.Button,
                    onClick = onActionClick
                )
                .padding(
                    start = 10.dp,
                    top = 6.dp,
                    bottom = 6.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = WarmEmber
            )

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = WarmEmber
            )
        }
    }
}

@Composable
private fun LastTripCard(
    trip: TripSummaryDTO,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                onClickLabel = "Abrir última viagem",
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
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmSuccessTint)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 5.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = WarmSuccess
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = "CONCLUÍDA",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp,
                    color = WarmSuccess
                )
            }
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

// ---------------------------------------------------------------------------
// No-motorcycle benefits
// ---------------------------------------------------------------------------

@Composable
private fun NoMotorcycleBenefits() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Por que cadastrar sua moto?",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk
        )

        BenefitRow(
            icon = Icons.Outlined.Route,
            text = "Planeje viagens com mais precisão"
        )

        BenefitRow(
            icon = Icons.Outlined.CalendarMonth,
            text = "Acompanhe o histórico de cada moto"
        )

        BenefitRow(
            icon = Icons.Filled.LocationOn,
            text = "Tenha uma experiência personalizada"
        )
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
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInk
        )
    }
}

// ---------------------------------------------------------------------------
// Buttons
// ---------------------------------------------------------------------------



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
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {
        HomeHeader(subtitle = "Preparando a sua estrada...")

        Spacer(modifier = Modifier.height(20.dp))

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            cornerRadius = 28.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(2) {
                ShimmerBlock(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 20.dp
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 28.dp
        )
    }
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

        HomeHeader(subtitle = "Algo atrapalhou o caminho.")

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp),
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
                text = "Não foi possível carregar sua Home",
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
// Illustrations
// ---------------------------------------------------------------------------





// ---------------------------------------------------------------------------
// Formatters
// ---------------------------------------------------------------------------

internal fun greetingForNow(): String {
    val hour = LocalTime.now().hour

    return when {
        hour < 5 -> "Boa madrugada"
        hour < 12 -> "Bom dia"
        hour < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
}





