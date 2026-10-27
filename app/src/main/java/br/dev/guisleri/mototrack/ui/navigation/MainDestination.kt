package br.dev.guisleri.mototrack.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class MainDestination(
    val label: String,
    val icon: ImageVector,
    val selectedIcon: ImageVector
) : NavKey {
    HOME(
        label = "Home",
        icon = Icons.Outlined.Home,
        selectedIcon = Icons.Filled.Home
    ),

    TRIPS(
        label = "Viagens",
        icon = Icons.Outlined.Route,
        selectedIcon = Icons.Filled.Route
    ),

    MOTORCYCLES(
        label = "Motos",
        icon = Icons.Outlined.TwoWheeler,
        selectedIcon = Icons.Filled.TwoWheeler
    ),

    PROFILE(
        label = "Perfil",
        icon = Icons.Outlined.Person,
        selectedIcon = Icons.Filled.Person
    )
}
