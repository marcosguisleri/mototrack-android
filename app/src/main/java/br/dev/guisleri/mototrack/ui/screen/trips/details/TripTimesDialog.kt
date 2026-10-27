package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.WarmDialog
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

private enum class TimeField { STARTED, COMPLETED }

private val TimeFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm")

/**
 * Corrects when the trip started and, once it is done, when it finished: the
 * rider often only opens the app after leaving or arriving.
 */
@Composable
internal fun TripTimesDialog(
    trip: TripResponseDTO,
    isSaving: Boolean,
    errorMessage: String?,
    onSave: (startedAt: Instant?, completedAt: Instant?) -> Unit,
    onDismiss: () -> Unit
) {
    val completed = trip.status == "COMPLETED"

    var startedAt by remember(trip.startedAt) { mutableStateOf(parseInstant(trip.startedAt)) }
    var completedAt by remember(trip.completedAt) { mutableStateOf(parseInstant(trip.completedAt)) }
    var picking by remember { mutableStateOf<TimeField?>(null) }
    var localError by remember { mutableStateOf<String?>(null) }

    WarmDialog(
        icon = Icons.Outlined.Schedule,
        title = "Ajustar horários",
        message = if (completed) {
            "Esqueceu de iniciar ou concluir na hora? Corrija aqui quando a viagem começou e terminou."
        } else {
            "Esqueceu de iniciar na hora? Corrija aqui quando a viagem começou."
        },
        confirmText = if (isSaving) "Salvando..." else "Salvar horários",
        onConfirm = {
            val now = Instant.now()
            val start = startedAt
            val end = completedAt

            localError = when {
                start != null && start.isAfter(now) -> "O início não pode estar no futuro."
                end != null && end.isAfter(now) -> "A conclusão não pode estar no futuro."
                start != null && end != null && end.isBefore(start) ->
                    "A conclusão não pode ser anterior ao início."

                else -> null
            }

            if (localError == null) onSave(start, if (completed) end else null)
        },
        onDismissRequest = onDismiss,
        dismissText = "Cancelar",
        isLoading = isSaving
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TimeRow(
                label = "Início",
                value = startedAt,
                enabled = !isSaving,
                onClick = { picking = TimeField.STARTED }
            )

            if (completed) {
                TimeRow(
                    label = "Conclusão",
                    value = completedAt,
                    enabled = !isSaving,
                    onClick = { picking = TimeField.COMPLETED }
                )
            }
        }

        (localError ?: errorMessage)?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
    }

    picking?.let { field ->
        DateTimePickerFlow(
            initial = (if (field == TimeField.STARTED) startedAt else completedAt) ?: Instant.now(),
            onPicked = { picked ->
                if (field == TimeField.STARTED) startedAt = picked else completedAt = picked
                localError = null
                picking = null
            },
            onDismiss = { picking = null }
        )
    }
}

@Composable
private fun TimeRow(
    label: String,
    value: Instant?,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WarmCanvas)
            .border(BorderStroke(1.dp, WarmHairline), RoundedCornerShape(18.dp))
            .clickable(
                enabled = enabled,
                onClickLabel = "Escolher horário",
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = WarmMuted
        )

        Text(
            text = value?.atZone(ZoneId.systemDefault())?.format(TimeFormat) ?: "Definir",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = if (value == null) WarmEmber else WarmInk
        )
    }
}

/** Day first, then the time of day; neither can be in the future. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickerFlow(
    initial: Instant,
    onPicked: (Instant) -> Unit,
    onDismiss: () -> Unit
) {
    val zone = ZoneId.systemDefault()
    val initialLocal = initial.atZone(zone)
    val today = remember { LocalDate.now(zone) }
    var pickedDate by remember { mutableStateOf<LocalDate?>(null) }

    val date = pickedDate

    if (date == null) {
        val dateState = rememberDatePickerState(
            initialSelectedDateMillis = initialLocal.toLocalDate()
                .atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
            yearRange = (today.year - 5)..today.year,
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC)
                        .toLocalDate().isAfter(today)

                override fun isSelectableYear(year: Int): Boolean = year <= today.year
            }
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        dateState.selectedDateMillis?.let { millis ->
                            pickedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneOffset.UTC).toLocalDate()
                        }
                    }
                ) {
                    Text("Próximo", fontWeight = FontWeight.Bold, color = WarmEmber)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = WarmInkSoft)
                }
            }
        ) {
            DatePicker(
                state = dateState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = WarmEmber,
                    selectedYearContainerColor = WarmEmber,
                    todayDateBorderColor = WarmEmber,
                    todayContentColor = WarmEmberDeep
                )
            )
        }
    } else {
        val timeState = rememberTimePickerState(
            initialHour = initialLocal.hour,
            initialMinute = initialLocal.minute,
            is24Hour = true
        )

        Dialog(
            onDismissRequest = onDismiss,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.padding(horizontal = 24.dp),
                shape = RoundedCornerShape(32.dp),
                color = WarmCard,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TimePicker(
                        state = timeState,
                        colors = TimePickerDefaults.colors(
                            selectorColor = WarmEmber,
                            timeSelectorSelectedContainerColor = WarmEmber,
                            timeSelectorSelectedContentColor = Color.White
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = WarmInkSoft)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        TextButton(
                            onClick = {
                                onPicked(
                                    date.atTime(timeState.hour, timeState.minute)
                                        .atZone(zone)
                                        .toInstant()
                                )
                            }
                        ) {
                            Text("OK", fontWeight = FontWeight.Bold, color = WarmEmber)
                        }
                    }
                }
            }
        }
    }
}
