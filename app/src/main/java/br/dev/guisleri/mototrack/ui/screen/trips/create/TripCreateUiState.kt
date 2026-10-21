package br.dev.guisleri.mototrack.ui.screen.trips.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import java.time.LocalDate

enum class TripCreateMode {
    PLANNED,
    COMPLETED
}

enum class TripCreateTerrain(
    val apiValue: String,
    val label: String
) {
    ASPHALT(
        apiValue = "ASPHALT",
        label = "Asfalto"
    ),

    MIXED(
        apiValue = "MIXED",
        label = "Misto"
    ),

    OFF_ROAD(
        apiValue = "OFF_ROAD",
        label = "Off-road"
    )
}

sealed interface TripCreateMotorcyclesState {

    data object Loading : TripCreateMotorcyclesState

    data class Loaded(
        val motorcycles: List<MotorcycleResponseDTO>
    ) : TripCreateMotorcyclesState

    data class Error(
        val message: String
    ) : TripCreateMotorcyclesState

}

data class TripCreateUiState(
    val mode: TripCreateMode = TripCreateMode.PLANNED,
    val origin: String = "",
    val destination: String = "",
    val distance: String = "",
    val tripDate: LocalDate = LocalDate.now(),
    val terrain: TripCreateTerrain = TripCreateTerrain.ASPHALT,
    val selectedMotorcycleId: Long? = null,
    val motorcycles: TripCreateMotorcyclesState =
        TripCreateMotorcyclesState.Loading,
    val originError: String? = null,
    val destinationError: String? = null,
    val distanceError: String? = null,
    val motorcycleError: String? = null,
    val recentPlaces: List<String> = emptyList(),
    val distanceSuggestion: Double? = null,
    val isSubmitting: Boolean = false,
    val submitErrorMessage: String? = null,
    val isCreated: Boolean = false
)
