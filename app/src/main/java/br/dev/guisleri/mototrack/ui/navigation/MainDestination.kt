package br.dev.guisleri.mototrack.ui.navigation

import br.dev.guisleri.mototrack.R

enum class MainDestination(
    val label: String,
    val iconResId: Int
) {
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