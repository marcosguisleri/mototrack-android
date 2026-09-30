package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import br.dev.guisleri.mototrack.R
import kotlinx.serialization.Serializable

@Serializable
enum class MainDestination(
    val label: String,
    val iconResId: Int
) : NavKey {
    HOME(
        label = "Home",
        iconResId = R.drawable.ic_home
    ),

    TRIPS(
        label = "Viagens",
        iconResId = R.drawable.ic_trips
    ),

    MOTORCYCLES(
        label = "Motos",
        iconResId = R.drawable.ic_motorcycle
    ),

    PROFILE(
        label = "Perfil",
        iconResId = R.drawable.ic_profile
    )
}