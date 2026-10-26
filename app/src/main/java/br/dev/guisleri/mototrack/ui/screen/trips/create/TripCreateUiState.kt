package br.dev.guisleri.mototrack.ui.screen.trips.create

import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.place.City
import java.time.LocalDate

enum class TripCreateMode {
    PLANNED,
    COMPLETED
}

/** How the distance in the form came to be, when the app filled it in. */
enum class DistanceSource {
    /** The length of the drive between the two cities. */
    ROUTE,

    /** A straight line scaled to roads, when no route could be had. */
    ESTIMATE
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
    val isCreated: Boolean = false,
    // Set when the form edits an existing trip instead of creating one.
    val editingTripId: Long? = null,
    val editingStatus: String? = null,
    val isLoadingTrip: Boolean = false,
    val loadTripErrorMessage: String? = null,
    val isUpdated: Boolean = false,
    // Set when the origin or destination is a city picked from the suggestions.
    val originCity: City? = null,
    val destinationCity: City? = null,
    val originSuggestions: List<City> = emptyList(),
    val destinationSuggestions: List<City> = emptyList(),
    val isCalculatingDistance: Boolean = false,
    val distanceSource: DistanceSource? = null
) {
    val isEditing: Boolean get() = editingTripId != null
}
