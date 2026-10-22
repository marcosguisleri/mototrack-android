package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.ui.components.WarmDialog
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.formatDecimalInput
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.parseDecimalInput
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.sanitizeDecimalInput

private const val MAX_DISTANCE_INTEGER_DIGITS = 5

/**
 * Asked when finishing: the distance planned is rarely the one ridden, and the
 * average consumption and the stats depend on it. A distance equal to the
 * planned one is reported as null, meaning "keep it".
 */
@Composable
internal fun TripFinishDialog(
    plannedDistanceKm: Double,
    onConfirm: (actualDistanceKm: Double?) -> Unit,
    onDismiss: () -> Unit
) {
    var distance by remember { mutableStateOf(formatDecimalInput(plannedDistanceKm, 1)) }
    var error by remember { mutableStateOf<String?>(null) }

    WarmDialog(
        icon = Icons.Outlined.TaskAlt,
        title = "Concluir viagem",
        message = "Quantos km você rodou de verdade? Com a distância certa, o " +
            "consumo médio e as suas estatísticas ficam corretos.",
        confirmText = "Concluir viagem",
        onConfirm = {
            val parsed = parseDecimalInput(distance)

            if (parsed == null || parsed <= 0.0) {
                error = "Informe uma distância maior que zero."
            } else {
                onConfirm(parsed.takeIf { kotlin.math.abs(it - plannedDistanceKm) >= 0.05 })
            }
        },
        onDismissRequest = onDismiss,
        dismissText = "Cancelar"
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        WarmTextField(
            value = distance,
            onValueChange = {
                distance = sanitizeDecimalInput(it, MAX_DISTANCE_INTEGER_DIGITS, 1)
                error = null
            },
            label = "Distância percorrida",
            leadingIcon = Icons.Outlined.Route,
            suffix = "km",
            error = error,
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
        )
    }
}
