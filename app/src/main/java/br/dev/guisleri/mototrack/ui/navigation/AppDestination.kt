package br.dev.guisleri.mototrack.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
enum class AppDestination : NavKey {
    LOGIN,
    REGISTER
}