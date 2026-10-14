package br.dev.guisleri.mototrack.ui.screen.motorcycles

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import br.dev.guisleri.mototrack.R
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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTextPrimary
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTextSecondary
import java.text.NumberFormat
import java.util.Locale

@Composable
fun MotorcyclesScreen(
    uiState: MotorcyclesUiState,
    onRetryClick: () -> Unit,
    onMotorcycleSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        MotorcyclesUiState.Loading -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
            }
        }

        MotorcyclesUiState.Empty -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Sua garagem está vazia")
            }
        }

        is MotorcyclesUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(uiState.message)

                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Tentar novamente")
                }
            }
        }

        is MotorcyclesUiState.Success -> {
            MotorcyclesContent(
                uiState = uiState,
                onMotorcycleSelected = onMotorcycleSelected,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MotorcyclesContent(
    uiState: MotorcyclesUiState.Success,
    onMotorcycleSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMotorcycle =
        uiState.motorcycles.first {
            it.id == uiState.selectedMotorcycleId
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        MotorcyclesHeader()

        if (uiState.motorcycles.size > 1) {
            Row {
                uiState.motorcycles.forEach { motorcycle ->
                    TextButton(
                        onClick = {
                            onMotorcycleSelected(motorcycle.id)
                        }
                    ) {
                        Text(motorcycle.model)
                    }
                }
            }
        }

        MotorcycleHero(
            motorcycle = selectedMotorcycle
        )

        when (val statisticsState = uiState.statisticsUiState) {
            MotorcycleStatisticsUiState.Loading -> {
                Text("Carregando estatísticas...")
            }

            is MotorcycleStatisticsUiState.Error -> {
                Text(statisticsState.message)
            }

            is MotorcycleStatisticsUiState.Success -> {
                MotorcycleSummary(
                    statistics = statisticsState.statistics
                )
            }
        }
    }
}

@Composable
private fun MotorcyclesHeader() {
    Column(
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = "Motos",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Sua garagem",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MotorcycleHero(
    motorcycle: MotorcycleResponseDTO
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${motorcycle.brand} ${motorcycle.model}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Minha motocicleta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            BasicText(
                text = motorcycle.model.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.06f)
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 40.sp,
                    maxFontSize = 160.sp
                )
            )

            Image(
                painter = painterResource(R.drawable.motorcycle_hero),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${motorcycle.year}  •  ${motorcycle.color}  •  " +
                    "${motorcycle.engineCapacity} cc",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun MotorcycleSummary(
    statistics: MotorcycleStatisticsResponseDTO
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MotorcycleMetricCard(
            icon = R.drawable.ic_distance,
            value = formatDistanceValue(
                statistics.totalCompletedDistanceKm
            ),
            label = "km rodados",
            modifier = Modifier.weight(1f)
        )

        MotorcycleMetricCard(
            icon = R.drawable.ic_trips,
            value = statistics.completedTrips.toString(),
            label = "viagens concluídas",
            modifier = Modifier.weight(1f)
        )

        MotorcycleMetricCard(
            icon = R.drawable.ic_statistics,
            value = statistics.averageCompletedDistanceKm
                ?.let(::formatDistanceValue)
                ?: "—",
            label = "km média",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MotorcycleMetricCard(
    icon: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(88.dp),
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
                .fillMaxSize()
                .padding(
                    horizontal = 6.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = value,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(1.dp)
            )

            Text(
                text = label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

private fun formatDistanceValue(
    distanceKm: Double
): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return formatter.format(distanceKm)
}