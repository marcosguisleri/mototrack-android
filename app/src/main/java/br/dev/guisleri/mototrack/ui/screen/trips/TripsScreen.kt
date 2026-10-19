package br.dev.guisleri.mototrack.ui.screen.trips

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO

@Composable
fun TripsScreen(
    uiState: TripsUiState,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        TripsUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        TripsUiState.Empty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Nenhuma viagem encontrada")
            }
        }

        is TripsUiState.Error -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = uiState.message,
                    textAlign = TextAlign.Center
                )

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Button(
                    onClick = onRetryClick
                ) {
                    Text("Tentar novamente")
                }
            }
        }

        is TripsUiState.Success -> {
            TripsContent(
                trips = uiState.trips,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun TripsContent(
    trips: List<TripResponseDTO>,
    modifier: Modifier = Modifier
) {
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

        items(
            items = trips,
            key = { trip -> trip.id }
        ) { trip ->
            TripItem(
                trip = trip
            )
        }
    }
}

@Composable
private fun TripItem(
    trip: TripResponseDTO
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "${trip.origin} → ${trip.destination}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(
                modifier = Modifier.height(8.dp)
            )

            Text(
                text = "${trip.tripDate} • ${trip.distanceKm} km"
            )

            Text(
                text = "${trip.terrain} • ${trip.status}"
            )

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}"
            )
        }
    }
}