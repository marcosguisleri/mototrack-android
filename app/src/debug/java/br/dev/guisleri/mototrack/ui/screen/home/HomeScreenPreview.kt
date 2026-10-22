package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import br.dev.guisleri.mototrack.data.model.home.HomeMotorcycleDTO
import br.dev.guisleri.mototrack.data.model.home.HomeResponseDTO
import br.dev.guisleri.mototrack.data.model.home.HomeUserDTO
import br.dev.guisleri.mototrack.data.model.home.NextTripDTO
import br.dev.guisleri.mototrack.data.model.home.TripSummaryDTO
import br.dev.guisleri.mototrack.ui.theme.MotoTrackTheme

@Preview(
    name = "Loading",
    group = "Home states",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeLoadingPreview() {
    HomePreview(
        uiState = HomeUiState.Loading
    )
}

@Preview(
    name = "Error",
    group = "Home states",
    widthDp = 360,
    heightDp = 640,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeErrorPreview() {
    HomePreview(
        uiState = HomeUiState.Error(
            message = "Não foi possível comunicar com o servidor. Tente novamente."
        )
    )
}

@Preview(
    name = "Success — NoMotorcycle",
    group = "Home states",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeNoMotorcyclePreview() {
    HomePreview(
        uiState = HomeUiState.Success(
            home = previewHome(
                motorcycleCount = 0
            ),
            highlight = HomeHighlightState.NoMotorcycle
        )
    )
}

@Preview(
    name = "Success — InProgress",
    group = "Home states",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeInProgressPreview() {
    val inProgressTrip = previewTrip(
        id = 12,
        origin = "Curitiba",
        destination = "Serra do Rio do Rastro",
        distanceKm = 428.6,
        terrain = "MIXED",
        tripDate = "2026-10-12"
    )

    HomePreview(
        uiState = HomeUiState.Success(
            home = previewHome(
                inProgressTrip = inProgressTrip,
                lastCompletedTrip = previewLastCompletedTrip
            ),
            highlight = HomeHighlightState.InProgress(
                trip = inProgressTrip
            )
        )
    )
}

@Preview(
    name = "Success — UpcomingTrip",
    group = "Home states",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeUpcomingTripPreview() {
    val nextTrip = previewNextTrip()

    HomePreview(
        uiState = HomeUiState.Success(
            home = previewHome(
                nextTrip = nextTrip,
                lastCompletedTrip = previewLastCompletedTrip
            ),
            highlight = HomeHighlightState.UpcomingTrip(
                trip = nextTrip
            )
        )
    )
}

@Preview(
    name = "Success — NoUpcomingTrip",
    group = "Home states",
    widthDp = 360,
    heightDp = 800,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeNoUpcomingTripPreview() {
    HomePreview(
        uiState = HomeUiState.Success(
            home = previewHome(
                lastCompletedTrip = previewLastCompletedTrip
            ),
            highlight = HomeHighlightState.NoUpcomingTrip
        )
    )
}

@Preview(
    name = "Compacta, fonte 2x e textos longos",
    group = "Home accessibility",
    widthDp = 320,
    heightDp = 640,
    fontScale = 2f,
    showBackground = true,
    backgroundColor = 0xFFF7F4EF
)
@Composable
private fun HomeCompactLargeFontPreview() {
    val nextTrip = previewNextTrip(
        origin = "São José dos Campos",
        destination = "Parque Nacional da Serra da Bocaina",
        motorcycle = previewMotorcycle.copy(
            brand = "Harley-Davidson",
            model = "Pan America 1250 Special Adventure Touring"
        )
    )

    HomePreview(
        uiState = HomeUiState.Success(
            home = previewHome(
                nextTrip = nextTrip,
                completedTrips = 2_147_483_648,
                lastCompletedTrip = previewLastCompletedTrip
            ),
            highlight = HomeHighlightState.UpcomingTrip(
                trip = nextTrip
            )
        )
    )
}

@Composable
private fun HomePreview(
    uiState: HomeUiState
) {
    MotoTrackTheme {
        HomeScreen(
            uiState = uiState,
            onRetryClick = {},
            onRefresh = {},
            onAddMotorcycleClick = {},
            onTripsClick = {},
            onTripClick = {},
            onPlanTripClick = {},
            onRegisterCompletedTripClick = {}
        )
    }
}

private val previewMotorcycle = HomeMotorcycleDTO(
    id = 3,
    brand = "Honda",
    model = "CB 500X",
    color = "Vermelha",
    year = 2024,
    engineCapacity = 500,
    owner = HomeUserDTO(
        id = 1,
        name = "Guilherme",
        email = "guilherme@example.com"
    )
)

private fun previewHome(
    inProgressTrip: TripSummaryDTO? = null,
    nextTrip: NextTripDTO? = null,
    lastCompletedTrip: TripSummaryDTO? = null,
    totalCompletedDistanceKm: Double = 4_382.7,
    distanceThisYearKm: Double = 1_286.4,
    completedTrips: Long = 18,
    motorcycleCount: Long = 2
): HomeResponseDTO {
    return HomeResponseDTO(
        inProgressTrip = inProgressTrip,
        nextTrip = nextTrip,
        lastCompletedTrip = lastCompletedTrip,
        totalCompletedDistanceKm = totalCompletedDistanceKm,
        distanceThisYearKm = distanceThisYearKm,
        completedTrips = completedTrips,
        motorcycleCount = motorcycleCount
    )
}

private fun previewNextTrip(
    origin: String = "São Paulo",
    destination: String = "Campos do Jordão",
    motorcycle: HomeMotorcycleDTO = previewMotorcycle
): NextTripDTO {
    return NextTripDTO(
        id = 24,
        origin = origin,
        destination = destination,
        distanceKm = 181.5,
        terrain = "ASPHALT",
        tripDate = "2026-10-18",
        daysUntil = 6,
        motorcycle = motorcycle
    )
}

private fun previewTrip(
    id: Long,
    origin: String,
    destination: String,
    distanceKm: Double,
    terrain: String,
    tripDate: String
): TripSummaryDTO {
    return TripSummaryDTO(
        id = id,
        origin = origin,
        destination = destination,
        distanceKm = distanceKm,
        terrain = terrain,
        tripDate = tripDate,
        motorcycle = previewMotorcycle
    )
}

private val previewLastCompletedTrip = previewTrip(
    id = 7,
    origin = "Belo Horizonte",
    destination = "Ouro Preto",
    distanceKm = 97.0,
    terrain = "ASPHALT",
    tripDate = "2026-09-21"
)
