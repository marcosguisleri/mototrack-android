package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(
    uiState: HomeUiState,
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

        when (uiState) {

            HomeUiState.Loading -> {
                CircularProgressIndicator()
            }

            is HomeUiState.Success -> {
                Text(
                    text = "Resumo"
                )

                Text(
                    text = "Motos: ${uiState.home.motorcycleCount}"
                )

                Text(
                    text = "Viagens concluídas: ${uiState.home.completedTrips}"
                )

                Text(
                    text = "Distância total: ${uiState.home.totalCompletedDistanceKm} km"
                )
            }

            is HomeUiState.Error -> {
                Text(
                    text = uiState.message
                )

                Button(
                    onClick = onRetryClick,
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    Text("Tentar novamente")
                }
            }
        }
    }
}
