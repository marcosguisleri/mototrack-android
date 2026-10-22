package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class TripCreateDestination(
    val startAsCompleted: Boolean = false,
    val motorcycleId: Long? = null
) : NavKey
