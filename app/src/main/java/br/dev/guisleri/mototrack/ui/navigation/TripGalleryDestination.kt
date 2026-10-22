package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TripGalleryDestination(
    val tripId: Long,
    /** "Origin → Destination", shown under the title. */
    val route: String
) : NavKey
