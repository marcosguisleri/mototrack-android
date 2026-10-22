package br.dev.guisleri.mototrack.ui.screen.maintenance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Album
import androidx.compose.material.icons.outlined.Air
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.OilBarrel
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TireRepair
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.dev.guisleri.mototrack.data.maintenance.ItemInterval
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceItem
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceLevel
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceProgress
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.components.formatNumber
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

private val SoonColor = Color(0xFFE0A100)
private val OverdueColor = Color(0xFFC62828)

private val CardShape = RoundedCornerShape(26.dp)

@Composable
fun MaintenanceScreen(
    uiState: MaintenanceUiState,
    intervalFor: (MaintenanceItem) -> ItemInterval,
    onBackClick: () -> Unit,
    onRetryClick: () -> Unit,
    onEditOdometerClick: () -> Unit,
    onItemClick: (MaintenanceItem) -> Unit,
    onDismissEditor: () -> Unit,
    onSaveOdometer: (km: String) -> Unit,
    onSaveService: (MaintenanceItem, LocalDate, km: String, intervalKm: String) -> Unit,
    onClearService: (MaintenanceItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            CircleActionButton(
                icon = Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                onClick = onBackClick
            )

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = "Manutenção",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )

                if (uiState.motorcycleName.isNotBlank()) {
                    Text(
                        text = uiState.motorcycleName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkSoft
                    )
                }
            }
        }

        uiState.errorMessage?.let { message ->
            ErrorBanner(message = message, onRetryClick = onRetryClick)
        }

        OdometerCard(
            uiState = uiState,
            onEditClick = onEditOdometerClick
        )

        SummaryLine(uiState = uiState)

        uiState.items.forEach { progress ->
            MaintenanceItemCard(
                progress = progress,
                onClick = { onItemClick(progress.item) }
            )
        }

        Text(
            text = "Os intervalos são os mais comuns; confira o manual da sua moto. " +
                "Você pode ajustá-los ao registrar cada item.",
            style = MaterialTheme.typography.bodySmall,
            color = WarmMuted
        )
    }

    when (val editor = uiState.editor) {
        MaintenanceEditor.Odometer -> OdometerDialog(
            currentKm = uiState.odometerKm,
            errorMessage = uiState.editorErrorMessage,
            onSave = onSaveOdometer,
            onDismiss = onDismissEditor
        )

        is MaintenanceEditor.Service -> ServiceDialog(
            progress = uiState.items.first { it.item == editor.item },
            interval = intervalFor(editor.item),
            odometerKm = uiState.odometerKm,
            errorMessage = uiState.editorErrorMessage,
            onSave = { date, km, intervalKm -> onSaveService(editor.item, date, km, intervalKm) },
            onClear = { onClearService(editor.item) },
            onDismiss = onDismissEditor
        )

        null -> Unit
    }
}

// ---------------------------------------------------------------------------
// Header
// ---------------------------------------------------------------------------

@Composable
private fun OdometerCard(
    uiState: MaintenanceUiState,
    onEditClick: () -> Unit
) {
    val known = uiState.odometerKm != null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(30.dp))
            .background(GraphiteHeroBrush)
    ) {
        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Text(
                text = "QUILOMETRAGEM ATUAL",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = WarmEmberOnDark
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = uiState.odometerKm?.let { "${formatNumber(it.toDouble())} km" } ?: "—",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = uiState.maintenance.odometer?.let {
                    "Você informou ${formatNumber(it.km.toDouble())} km em " +
                        "${formatTripDate(it.date)}; o app soma as viagens feitas desde então."
                } ?: "Informe o km do painel da moto uma vez. A partir daí o app " +
                    "acompanha sozinho, somando as suas viagens.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.72f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onEditClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = WarmEmberDeep
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Speed,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = if (known) "Atualizar km" else "Informar km",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SummaryLine(uiState: MaintenanceUiState) {
    val summary = uiState.summary
    val registered = uiState.items.any { it.level != MaintenanceLevel.UNKNOWN }

    if (!registered) {
        Text(
            text = "Registre quando cada item foi feito pela última vez e o app " +
                "avisa quanto falta para o próximo.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkSoft
        )
        return
    }

    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            summary.overdue > 0 -> SummaryChip(
                text = if (summary.overdue == 1) "1 vencida" else "${summary.overdue} vencidas",
                color = OverdueColor
            )
        }

        if (summary.soon > 0) {
            SummaryChip(
                text = if (summary.soon == 1) "1 chegando" else "${summary.soon} chegando",
                color = SoonColor
            )
        }

        if (!summary.needsAttention) {
            SummaryChip(text = "Tudo em dia", color = WarmSuccess)
        }
    }
}

@Composable
private fun SummaryChip(text: String, color: Color) {
    Text(
        text = text,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.Bold,
        color = color
    )
}

@Composable
private fun ErrorBanner(message: String, onRetryClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.08f))
            .padding(start = 16.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )

        TextButton(onClick = onRetryClick) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = WarmEmber
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(text = "Tentar", color = WarmEmber, fontWeight = FontWeight.Bold)
        }
    }
}

// ---------------------------------------------------------------------------
// Items
// ---------------------------------------------------------------------------

@Composable
private fun MaintenanceItemCard(
    progress: MaintenanceProgress,
    onClick: () -> Unit
) {
    val color = levelColor(progress.level)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(WarmCard)
            .border(BorderStroke(1.dp, WarmHairline), CardShape)
            .clickable(
                onClickLabel = "Registrar ${progress.item.label}",
                role = Role.Button,
                onClick = onClick
            )
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        if (progress.level == MaintenanceLevel.UNKNOWN) WarmEmberTint else color.copy(alpha = 0.14f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = progress.item.icon(),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (progress.level == MaintenanceLevel.UNKNOWN) WarmEmber else color
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = progress.item.label,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk
                )

                Text(
                    text = statusText(progress),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (progress.level == MaintenanceLevel.UNKNOWN) WarmMuted else color
                )
            }
        }

        if (progress.level != MaintenanceLevel.UNKNOWN) {
            Spacer(modifier = Modifier.height(14.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(50))
                    .background(WarmHairline)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress.usedFraction.coerceAtLeast(0.03f))
                        .background(color, RoundedCornerShape(50))
                )
            }

            progress.lastService?.let { service ->
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = buildString {
                        append("Última vez: ${formatTripDate(service.date)}")
                        service.doneAtKm?.let { append(" · ${formatNumber(it.toDouble())} km") }
                        intervalText(progress.interval)?.let { append(" · $it") }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = WarmMuted
                )
            }
        } else {
            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = progress.item.hint,
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted
            )
        }
    }
}

private fun levelColor(level: MaintenanceLevel): Color =
    when (level) {
        MaintenanceLevel.OK -> WarmSuccess
        MaintenanceLevel.SOON -> SoonColor
        MaintenanceLevel.OVERDUE -> OverdueColor
        MaintenanceLevel.UNKNOWN -> WarmMuted
    }

internal fun MaintenanceItem.icon(): ImageVector =
    when (this) {
        MaintenanceItem.OIL -> Icons.Outlined.OilBarrel
        MaintenanceItem.CHAIN -> Icons.Outlined.Link
        MaintenanceItem.TIRES -> Icons.Outlined.TireRepair
        MaintenanceItem.BRAKES -> Icons.Outlined.Album
        MaintenanceItem.AIR_FILTER -> Icons.Outlined.Air
        MaintenanceItem.SERVICE -> Icons.Outlined.Build
    }

/** "Faltam 320 km", "Venceu há 3 semanas": the most useful thing to know about an item. */
internal fun statusText(progress: MaintenanceProgress): String {
    if (progress.level == MaintenanceLevel.UNKNOWN) return "Nunca registrado"

    val parts = listOfNotNull(
        progress.kmLeft?.let { left ->
            if (left > 0) "faltam ${formatNumber(left.toDouble())} km" else "passou ${formatNumber((-left).toDouble())} km"
        },
        progress.daysLeft?.let { left -> daysText(left) }
    )

    return parts
        .joinToString(" · ")
        .replaceFirstChar { it.uppercase() }
        .ifBlank { "Registrado" }
}

private fun daysText(days: Long): String =
    when {
        days == 0L -> "vence hoje"
        days == 1L -> "vence amanhã"
        days in 2..60 -> "em $days dias"
        days > 60 -> "em ${days / 30} meses"
        days == -1L -> "venceu ontem"
        days >= -60 -> "venceu há ${-days} dias"
        else -> "venceu há ${-days / 30} meses"
    }

private fun intervalText(interval: ItemInterval): String? {
    val parts = listOfNotNull(
        interval.km?.let { "${formatNumber(it.toDouble())} km" },
        interval.months?.let { "$it meses" }
    )

    return parts.takeIf { it.isNotEmpty() }?.joinToString(" ou ", prefix = "a cada ")
}

// ---------------------------------------------------------------------------
// Dialogs
// ---------------------------------------------------------------------------

@Composable
private fun DialogFrame(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(32.dp),
            color = WarmCard,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(WarmEmberTint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = WarmEmber
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmInk
                    )
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )

                content()
            }
        }
    }
}

@Composable
private fun DialogButtons(
    confirmText: String,
    errorMessage: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    extra: @Composable () -> Unit = {}
) {
    errorMessage?.let { message ->
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }

    Button(
        onClick = onConfirm,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmInk,
            contentColor = Color.White
        )
    ) {
        Text(text = confirmText, fontWeight = FontWeight.Bold)
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        TextButton(onClick = onDismiss) {
            Text(text = "Cancelar", color = WarmInkSoft)
        }

        extra()
    }
}

@Composable
private fun OdometerDialog(
    currentKm: Int?,
    errorMessage: String?,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var km by remember { mutableStateOf(currentKm?.toString().orEmpty()) }

    DialogFrame(
        title = "Quilometragem",
        subtitle = "Leia o hodômetro no painel da moto e informe aqui. " +
            "Dali em diante o app soma as viagens que você concluir.",
        icon = Icons.Outlined.Speed,
        onDismiss = onDismiss
    ) {
        WarmTextField(
            value = km,
            onValueChange = { km = it.filter(Char::isDigit).take(MAX_ODOMETER_DIGITS) },
            label = "Km no painel",
            leadingIcon = Icons.Outlined.Speed,
            suffix = "km",
            error = null,
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        DialogButtons(
            confirmText = "Salvar",
            errorMessage = errorMessage,
            onConfirm = { onSave(km) },
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun ServiceDialog(
    progress: MaintenanceProgress,
    interval: ItemInterval,
    odometerKm: Int?,
    errorMessage: String?,
    onSave: (LocalDate, km: String, intervalKm: String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    val item = progress.item
    val last = progress.lastService

    var date by remember {
        mutableStateOf(last?.date?.let { runCatching { LocalDate.parse(it) }.getOrNull() } ?: LocalDate.now())
    }
    var km by remember { mutableStateOf((last?.doneAtKm ?: odometerKm)?.toString().orEmpty()) }
    var intervalKm by remember { mutableStateOf(interval.km?.toString().orEmpty()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var confirmClear by remember { mutableStateOf(false) }

    DialogFrame(
        title = item.label,
        subtitle = "${item.hint}. Registre quando foi feito pela última vez.",
        icon = item.icon(),
        onDismiss = onDismiss
    ) {
        DateField(
            label = "Feito em",
            value = formatTripDate(date.toString()),
            onClick = { showDatePicker = true }
        )

        WarmTextField(
            value = km,
            onValueChange = { km = it.filter(Char::isDigit).take(MAX_ODOMETER_DIGITS) },
            label = "Km no painel (opcional)",
            leadingIcon = Icons.Outlined.Speed,
            suffix = "km",
            error = null,
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        WarmTextField(
            value = intervalKm,
            onValueChange = { intervalKm = it.filter(Char::isDigit).take(MAX_INTERVAL_DIGITS) },
            label = "Repetir a cada",
            leadingIcon = Icons.Outlined.Refresh,
            suffix = "km",
            error = null,
            enabled = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )

        DialogButtons(
            confirmText = if (last == null) "Registrar" else "Atualizar",
            errorMessage = errorMessage,
            onConfirm = { onSave(date, km, intervalKm) },
            onDismiss = onDismiss,
            extra = {
                if (last != null) {
                    TextButton(
                        onClick = { if (confirmClear) onClear() else confirmClear = true }
                    ) {
                        Text(
                            text = if (confirmClear) "Toque para confirmar" else "Remover registro",
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        )
    }

    if (showDatePicker) {
        PastDatePickerDialog(
            selected = date,
            onSelected = { date = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
private fun DateField(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.Transparent)
            .border(BorderStroke(1.dp, WarmHairline), RoundedCornerShape(16.dp))
            .clickable(onClickLabel = "Escolher data", role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = WarmMuted
        )

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium,
            color = WarmMuted
        )

        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = WarmInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PastDatePickerDialog(
    selected: LocalDate,
    onSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = remember { LocalDate.now() }

    val state = rememberDatePickerState(
        initialSelectedDateMillis = selected.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli(),
        yearRange = (today.year - 15)..today.year,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                !Instant.ofEpochMilli(utcTimeMillis).atZone(ZoneOffset.UTC).toLocalDate().isAfter(today)

            override fun isSelectableYear(year: Int): Boolean = year <= today.year
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { millis ->
                        onSelected(Instant.ofEpochMilli(millis).atZone(ZoneOffset.UTC).toLocalDate())
                    }
                    onDismiss()
                }
            ) {
                Text("Confirmar", fontWeight = FontWeight.Bold, color = WarmEmber)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = WarmInkSoft)
            }
        }
    ) {
        DatePicker(
            state = state,
            colors = DatePickerDefaults.colors(
                selectedDayContainerColor = WarmEmber,
                selectedYearContainerColor = WarmEmber,
                todayDateBorderColor = WarmEmber,
                todayContentColor = WarmEmberDeep
            )
        )
    }
}
