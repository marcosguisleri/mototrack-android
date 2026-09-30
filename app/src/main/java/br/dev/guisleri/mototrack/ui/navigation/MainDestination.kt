package br.dev.guisleri.mototrack.ui.navigation

import br.dev.guisleri.mototrack.R

enum class MainDestination(
    val label: String,
    val iconResId: Int
) {
    HOME("Home", R.drawable.ic_home),
    TRIPS("Viagens", R.drawable.ic_trips),
    MOTORCYCLES("Motos", R.drawable.ic_motorcycle),
    PROFILE("Perfil", R.drawable.ic_profile)
}