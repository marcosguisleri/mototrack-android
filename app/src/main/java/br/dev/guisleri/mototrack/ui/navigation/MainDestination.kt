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
        iconResId = R.drawable.ic_mototrack_home
    ),

    TRIPS(
        label = "Viagens",
        iconResId = R.drawable.ic_mototrack_trips
    ),

    MOTORCYCLES(
        label = "Motos",
        iconResId = R.drawable.ic_mototrack_motorcycles
    ),

    PROFILE(
        label = "Perfil",
        iconResId = R.drawable.ic_mototrack_profile
    )
}