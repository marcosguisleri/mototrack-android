package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data class MotorcycleEditDestination(
    val motorcycleId: Long
) : NavKey
