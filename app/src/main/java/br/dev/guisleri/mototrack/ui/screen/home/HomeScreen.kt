package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.width
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

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
                message = "Não foi possível comunicar com o servidor. Tente novamente."
            ),
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
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            )
    ) {

        HomeHeader()

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        HomeHighlightCard(
            highlight = highlight,
            onAddMotorcycleClick = onAddMotorcycleClick,
            onTripsClick = onTripsClick
        )

        if (highlight == HomeHighlightState.NoMotorcycle) {

            Spacer(
                modifier = Modifier.height(32.dp)
            )

            NoMotorcycleBenefits()
        }

        if (home.motorcycleCount > 0) {

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            HomeSummarySection(
                totalDistanceKm = home.totalCompletedDistanceKm,
                completedTrips = home.completedTrips,
                distanceThisYearKm = home.distanceThisYearKm
            )
        }

        home.lastCompletedTrip?.let { trip ->

            Spacer(
                modifier = Modifier.height(28.dp)
            )

            LastCompletedTripSection(
                trip = trip,
                onClick = onTripsClick
            )
        }
    }
}

@Composable
private fun HomeHeader() {
    Column {

        Text(
            text = "MotoTrack",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(4.dp)
        )

        Text(
            text = "Pronto para a próxima jornada?",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "VIAGEM EM ANDAMENTO",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "${formatDistance(trip.distanceKm)}  •  ${
                    formatTerrain(trip.terrain)
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimary.copy(
                    alpha = 0.8f
                )
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onClick,
                modifier = Modifier.heightIn(
                    min = 48.dp
                ),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Retomar viagem",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
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
                alpha = 0.07f
            )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 24.dp,
                    vertical = 28.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "SUA GARAGEM",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Text(
                text = "Sua garagem está vazia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Cadastre sua primeira moto para começar a planejar e registrar suas jornadas.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onAddMotorcycleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cadastrar moto",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UpcomingTripCard(
    trip: NextTripDTO,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.inverseSurface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "PRÓXIMA VIAGEM",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = formatTripDate(trip.tripDate),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface.copy(
                    alpha = 0.75f
                )
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = when {
                    trip.daysUntil == 0L -> "É hoje!"
                    trip.daysUntil == 1L -> "Amanhã"
                    else -> "Daqui a ${trip.daysUntil} dias"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(
                modifier = Modifier.height(16.dp)
            )

            Text(
                text = "${formatDistance(trip.distanceKm)}  •  ${
                    formatTerrain(trip.terrain)
                }",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface
            )

            Spacer(
                modifier = Modifier.height(6.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.inverseOnSurface.copy(
                        alpha = 0.75f
                    )
                )

                Text(
                    text = "›",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Light,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun NoUpcomingTripCard(
    onTripsClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {

            Text(
                text = "Planeje sua próxima aventura",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "Você ainda não tem nenhuma viagem planejada. Que tal preparar sua próxima rota?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(20.dp)
            )

            Button(
                onClick = onTripsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Planejar viagem")
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                onClick = onTripsClick,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Registrar viagem realizada")
            }
        }
    }
}

@Composable
private fun HomeSummarySection(
    totalDistanceKm: Double,
    completedTrips: Long,
    distanceThisYearKm: Double
) {
    val metrics = listOf(
        formatDistance(totalDistanceKm) to "Distância total",
        completedTrips.toString() to "Viagens concluídas",
        formatDistance(distanceThisYearKm) to "Neste ano"
    )

    Column {

        Text(
            text = "Resumo das suas viagens",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SummaryMetricCard(
                value = formatDistance(totalDistanceKm),
                label = "Distância total",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            SummaryMetricCard(
                value = completedTrips.toString(),
                label = "Viagens",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            SummaryMetricCard(
                value = formatDistance(distanceThisYearKm),
                label = "Neste ano",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun SummaryMetricCard(
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
                    horizontal = 14.dp,
                    vertical = 16.dp
                )
        ) {

            Box(
                modifier = Modifier
                    .size(
                        width = 28.dp,
                        height = 4.dp
                    )
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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

        Text(
            text = "Última viagem concluída",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(12.dp)
        )

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
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(
                        width = 4.dp,
                        height = 56.dp
                    )
                    .background(
                        color = MaterialTheme.colorScheme.secondary,
                        shape = RoundedCornerShape(50)
                    )
            )

            Spacer(
                modifier = Modifier.width(14.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = "${trip.origin} → ${trip.destination}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

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

                Spacer(
                    modifier = Modifier.height(5.dp)
                )

                Text(
                    text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Text(
                text = "›",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Light,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

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

        LoadingBlock(
            modifier = Modifier
                .width(110.dp)
                .height(28.dp)
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        LoadingBlock(
            modifier = Modifier
                .width(210.dp)
                .height(18.dp)
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        LoadingBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 20.dp
        )

        Spacer(
            modifier = Modifier.height(28.dp)
        )

        LoadingBlock(
            modifier = Modifier
                .width(180.dp)
                .height(20.dp)
        )

        Spacer(
            modifier = Modifier.height(14.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(92.dp),
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
    }
}

@Composable
private fun LoadingBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.surfaceVariant,
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
                horizontal = 24.dp,
                vertical = 32.dp
            ),
        verticalArrangement = Arrangement.Center
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
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Box(
                    modifier = Modifier
                        .size(
                            width = 48.dp,
                            height = 6.dp
                        )
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = RoundedCornerShape(50)
                        )
                )

                Spacer(
                    modifier = Modifier.height(24.dp)
                )

                Text(
                    text = "Não foi possível carregar",
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
                    style = MaterialTheme.typography.bodyLarge,
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
                    shape = RoundedCornerShape(14.dp)
                ) {
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
private fun NoMotorcycleBenefits() {
    Column {

        Text(
            text = "Comece pelo básico",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        EmptyStateBenefit(
            number = "1",
            title = "Planeje suas viagens",
            description = "Associe sua moto às próximas rotas e organize suas jornadas."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        EmptyStateBenefit(
            number = "2",
            title = "Registre seu histórico",
            description = "Guarde as viagens realizadas, distâncias e terrenos percorridos."
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        EmptyStateBenefit(
            number = "3",
            title = "Acompanhe sua evolução",
            description = "Veja quilômetros percorridos e o histórico das suas aventuras."
        )
    }
}

@Composable
private fun EmptyStateBenefit(
    number: String,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.12f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 14.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

private fun formatTripDate(
    date: String
): String {
    return runCatching {
        LocalDate
            .parse(date)
            .format(
                DateTimeFormatter.ofPattern(
                    "dd MMM yyyy",
                    Locale("pt", "BR")
                )
            )
    }.getOrDefault(date)
}

private fun formatDistance(
    distanceKm: Double
): String {
    val formatter = java.text.NumberFormat.getNumberInstance(
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
