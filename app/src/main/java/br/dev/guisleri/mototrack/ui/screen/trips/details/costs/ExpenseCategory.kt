package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ConfirmationNumber
import androidx.compose.material.icons.outlined.Hotel
import androidx.compose.material.icons.outlined.LocalParking
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.ui.graphics.vector.ImageVector

/** The categories of the backend's `ExpenseCategory`, with how the rider sees them. */
enum class ExpenseCategory(
    val apiValue: String,
    val label: String,
    val plural: String,
    val icon: ImageVector
) {
    TOLL("TOLL", "Pedágio", "Pedágios", Icons.Outlined.Toll),
    FOOD("FOOD", "Alimentação", "Alimentação", Icons.Outlined.Restaurant),
    PARKING("PARKING", "Estacionamento", "Estacionamento", Icons.Outlined.LocalParking),
    LODGING("LODGING", "Hospedagem", "Hospedagem", Icons.Outlined.Hotel),
    ADMISSION("ADMISSION", "Ingresso", "Ingressos", Icons.Outlined.ConfirmationNumber),
    OTHER("OTHER", "Outros", "Outros", Icons.Outlined.MoreHoriz);

    companion object {
        fun fromApi(value: String): ExpenseCategory =
            entries.firstOrNull { it.apiValue == value } ?: OTHER
    }
}
