package br.dev.guisleri.mototrack.ui.screen.home

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
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onRetryClick: () -> Unit,
    onAddMotorcycleClick: () -> Unit,
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

@Composable
private fun HomeContent(
    home: HomeResponseDTO,
    highlight: HomeHighlightState,
    onAddMotorcycleClick: () -> Unit,
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
            onAddMotorcycleClick = onAddMotorcycleClick
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
                trip = trip
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
    onAddMotorcycleClick: () -> Unit
) {
    when (highlight) {

        is HomeHighlightState.InProgress -> {
            InProgressTripCard(
                trip = highlight.trip
            )
        }

        HomeHighlightState.NoMotorcycle -> {
            NoMotorcycleCard(
                onAddMotorcycleClick = onAddMotorcycleClick
            )
        }

        is HomeHighlightState.UpcomingTrip -> {
            UpcomingTripCard(
                trip = highlight.trip
            )
        }

        HomeHighlightState.NoUpcomingTrip -> {
            NoUpcomingTripCard()
        }
    }
}

@Composable
private fun InProgressTripCard(
    trip: TripSummaryDTO
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
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
                modifier = Modifier.height(12.dp)
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
                color = MaterialTheme.colorScheme.onPrimary
            )

            Spacer(
                modifier = Modifier.height(18.dp)
            )

            Button(
                onClick = {
                    // Navegação será conectada depois.
                }
            ) {
                Text("Ver viagem")
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
                alpha = 0.08f
            )
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
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(24.dp)
            )

            Button(
                onClick = onAddMotorcycleClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "Cadastrar moto",
                    modifier = Modifier.padding(
                        vertical = 4.dp
                    ),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun UpcomingTripCard(
    trip: NextTripDTO
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
                text = "PRÓXIMA VIAGEM",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Text(
                text = formatTripDate(
                    trip.tripDate
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = when {
                    trip.daysUntil == 0L ->
                        "É hoje!"

                    trip.daysUntil == 1L ->
                        "Amanhã"

                    else ->
                        "Daqui a ${trip.daysUntil} dias"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = formatDistance(
                        trip.distanceKm
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = formatTerrain(
                        trip.terrain
                    ),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NoUpcomingTripCard() {
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
                onClick = {
                    // Navegação será conectada depois.
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Planejar viagem")
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            OutlinedButton(
                onClick = {
                    // Navegação será conectada depois.
                },
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
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            SummaryMetricCard(
                value = formatDistance(
                    totalDistanceKm
                ),
                label = "Distância total",
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                value = completedTrips.toString(),
                label = "Viagens concluídas",
                modifier = Modifier.weight(1f)
            )

            SummaryMetricCard(
                value = formatDistance(
                    distanceThisYearKm
                ),
                label = "Neste ano",
                modifier = Modifier.weight(1f)
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
        shape = RoundedCornerShape(16.dp),
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
                .padding(
                    horizontal = 10.dp,
                    vertical = 16.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(4.dp)
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LastCompletedTripSection(
    trip: TripSummaryDTO
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
            trip = trip
        )
    }
}

@Composable
private fun LastCompletedTripCard(
    trip: TripSummaryDTO
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = formatTripDate(
                    trip.tripDate
                ),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = formatDistance(
                        trip.distanceKm
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = formatTerrain(
                        trip.terrain
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                style = MaterialTheme.typography.bodyMedium,
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
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Não foi possível carregar a Home",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
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
            modifier = Modifier.height(20.dp)
        )

        Button(
            onClick = onRetryClick
        ) {
            Text("Tentar novamente")
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
            modifier = Modifier.height(16.dp)
        )

        EmptyStateBenefit(
            title = "Planeje suas viagens",
            description = "Associe sua moto às próximas rotas e organize suas jornadas."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        EmptyStateBenefit(
            title = "Registre seu histórico",
            description = "Guarde as viagens realizadas, distâncias e terrenos percorridos."
        )

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        EmptyStateBenefit(
            title = "Acompanhe sua evolução",
            description = "Veja quilômetros percorridos e o histórico das suas aventuras."
        )
    }
}

@Composable
private fun EmptyStateBenefit(
    title: String,
    description: String
) {
    Column {

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(
            modifier = Modifier.height(2.dp)
        )

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    return if (distanceKm % 1.0 == 0.0) {
        "${distanceKm.toInt()} km"
    } else {
        String.format(
            Locale("pt", "BR"),
            "%.1f km",
            distanceKm
        )
    }
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