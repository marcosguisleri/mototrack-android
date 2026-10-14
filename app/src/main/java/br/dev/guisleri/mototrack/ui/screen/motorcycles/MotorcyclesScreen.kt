package br.dev.guisleri.mototrack.ui.screen.motorcycles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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
            .padding(20.dp)
    ) {
        Text("Motos")

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

        Text(
            text = "${selectedMotorcycle.brand} ${selectedMotorcycle.model}"
        )

        Text(
            text = "${selectedMotorcycle.year} • " +
                    "${selectedMotorcycle.color} • " +
                    "${selectedMotorcycle.engineCapacity} cc"
        )

        when (val statisticsState = uiState.statisticsUiState) {

            MotorcycleStatisticsUiState.Loading -> {
                Text("Carregando estatísticas...")
            }

            is MotorcycleStatisticsUiState.Error -> {
                Text(statisticsState.message)
            }

            is MotorcycleStatisticsUiState.Success -> {
                val statistics = statisticsState.statistics

                Text(
                    text = "Distância total: " +
                            "${statistics.totalCompletedDistanceKm} km"
                )

                Text(
                    text = "Viagens concluídas: " +
                            statistics.completedTrips
                )

                Text(
                    text = "Média: " +
                            (statistics.averageCompletedDistanceKm ?: 0.0)
                )
            }
        }
    }
}