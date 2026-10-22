package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TripEditDestination(
    val tripId: Long
) : NavKey
