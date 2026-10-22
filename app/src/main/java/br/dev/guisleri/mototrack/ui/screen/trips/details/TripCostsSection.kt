package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.ChoicePill
import br.dev.guisleri.mototrack.ui.components.WarmTextField
import br.dev.guisleri.mototrack.ui.components.formatLiters
import br.dev.guisleri.mototrack.ui.components.formatMoney
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.ExpenseCategory
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.MAX_DESCRIPTION_LENGTH
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.MAX_LITERS_FRACTION_DIGITS
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.MAX_LITERS_INTEGER_DIGITS
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.MAX_MONEY_FRACTION_DIGITS
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.MAX_MONEY_INTEGER_DIGITS
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripCostsUiState
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripRecord
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripRecordEditor
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.formatDecimalInput
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.parseDecimalInput
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.sanitizeDecimalInput
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/** What the costs sections can ask of the screen's owner. */
class TripCostsActions(
    val onAddFuel: () -> Unit,
    val onAddExpense: (ExpenseCategory) -> Unit,
    val onRecordClick: (TripRecord) -> Unit,
    val onRetry: () -> Unit,
    val onDismissEditor: () -> Unit,
    val onSaveFuel: (liters: String, totalCost: String) -> Unit,
    val onSaveExpense: (ExpenseCategory, amount: String, description: String) -> Unit,
    val onDeleteRecord: () -> Unit
)

private val FuelColor = WarmEmber
private val CategoryColors = mapOf(
    ExpenseCategory.TOLL to WarmInk,
    ExpenseCategory.FOOD to WarmSuccess,
    ExpenseCategory.PARKING to Color(0xFF5B7DB1),
    ExpenseCategory.LODGING to Color(0xFFE0A100),
    ExpenseCategory.ADMISSION to WarmEmberDeep,
    ExpenseCategory.OTHER to WarmMuted
)

private val CardShape = RoundedCornerShape(28.dp)

// ---------------------------------------------------------------------------
// Summary
// ---------------------------------------------------------------------------

/** The money of the trip: the total, what it is made of and, once done, how it went. */
@Composable
internal fun TripCostsCard(
    trip: TripResponseDTO,
    costs: TripCostsUiState,
    stats: TripLiveStats,
    live: Boolean,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val summary = costs.summary

    SectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SectionLabel("GASTOS")

            Text(
                text = if (live) "Ao vivo" else "Registrado",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(if (live) WarmEmberTint else WarmHairline)
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = if (live) WarmEmberDeep else WarmInkSoft
            )
        }

        when {
            summary == null && costs.isLoading -> {
                Spacer(modifier = Modifier.height(18.dp))
                CostsLoading()
            }

            summary == null -> {
                Spacer(modifier = Modifier.height(12.dp))
                CostsError(
                    message = costs.errorMessage ?: "Não foi possível carregar os gastos.",
                    onRetry = onRetry
                )
            }

            else -> {
                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = formatMoney(stats.totalCost ?: 0.0),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (stats.totalCost == null) "nada registrado ainda" else "gastos no total",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmInkSoft
                    )

                    stats.costPerKm?.let { perKm ->
                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = "${formatMoney(perKm)}/km",
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(WarmEmberTint)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = WarmEmberDeep
                        )
                    }
                }

                val slices = costSlices(summary, stats)

                if (slices.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(18.dp))

                    CostBar(slices = slices)

                    Spacer(modifier = Modifier.height(18.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        slices.forEach { slice -> CostRow(slice) }
                    }
                }

                costs.refreshErrorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(14.dp))
                    CostsError(message = message, onRetry = onRetry)
                }
            }
        }
    }
}

private data class CostSlice(
    val icon: ImageVector,
    val color: Color,
    val title: String,
    val detail: String,
    val amount: Double
)

private fun costSlices(
    summary: TripCostSummaryResponseDTO,
    stats: TripLiveStats
): List<CostSlice> = buildList {
    if (summary.fuel.entries > 0) {
        add(
            CostSlice(
                icon = Icons.Outlined.LocalGasStation,
                color = FuelColor,
                title = "Combustível",
                detail = buildString {
                    append(countOf(summary.fuel.entries, "abastecimento", "abastecimentos"))
                    stats.liters?.let { append(" · ${formatLiters(it)}") }
                    stats.averagePricePerLiter?.let { append(" · ${formatMoney(it)}/L") }
                },
                amount = summary.fuel.totalCost ?: 0.0
            )
        )
    }

    summary.expenses.forEach { expense ->
        val category = ExpenseCategory.fromApi(expense.category)

        add(
            CostSlice(
                icon = category.icon,
                color = CategoryColors.getValue(category),
                title = category.plural,
                detail = if (category == ExpenseCategory.TOLL) {
                    countOf(expense.count, "passagem", "passagens")
                } else {
                    countOf(expense.count, "registro", "registros")
                },
                amount = expense.total
            )
        )
    }
}

private fun countOf(count: Long, one: String, many: String): String =
    if (count == 1L) "1 $one" else "$count $many"

@Composable
private fun CostBar(slices: List<CostSlice>) {
    // An all-zero trip (only exempt tolls) has nothing to proportion.
    val total = slices.sumOf { it.amount }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(10.dp)
            .clip(RoundedCornerShape(50))
            .background(WarmHairline)
    ) {
        if (total > 0.0) {
            slices.filter { it.amount > 0.0 }.forEach { slice ->
                Box(
                    modifier = Modifier
                        .weight(slice.amount.toFloat())
                        .fillMaxHeight()
                        .background(slice.color)
                )
            }
        }
    }
}

@Composable
private fun CostRow(slice: CostSlice) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = slice.icon, size = 40.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(slice.color, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = slice.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmInk
                )
            }

            Text(
                text = slice.detail,
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatMoney(slice.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Log of records
// ---------------------------------------------------------------------------

/** Every fuel stop and expense, the latest first; tapping one opens it for correction. */
@Composable
internal fun TripRecordsCard(
    records: List<TripRecord>,
    onRecordClick: (TripRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    SectionCard(modifier = modifier) {
        SectionLabel("REGISTROS")

        Spacer(modifier = Modifier.height(12.dp))

        if (records.isEmpty()) {
            Text(
                text = "Nada registrado ainda. Abasteça ou anote um pedágio e " +
                    "os números aparecem aqui, na hora.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )
        } else {
            records.forEachIndexed { index, record ->
                if (index > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(WarmHairline)
                    )
                }

                RecordRow(record = record, onClick = { onRecordClick(record) })
            }
        }
    }
}

@Composable
private fun RecordRow(
    record: TripRecord,
    onClick: () -> Unit
) {
    val (icon, title, detail) = when (record) {
        is TripRecord.Fuel -> Triple(
            Icons.Outlined.LocalGasStation,
            "Abastecimento",
            "${formatLiters(record.entry.liters)} · ${formatMoney(record.entry.pricePerLiter)}/L"
        )

        is TripRecord.Expense -> Triple(
            record.category.icon,
            record.category.label,
            record.expense.description.orEmpty()
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = "Corrigir registro",
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconBadge(icon = icon, size = 40.dp)

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = WarmInk
            )

            Text(
                text = listOf(formatRecordTime(record.createdAt), detail)
                    .filter { it.isNotBlank() }
                    .joinToString(" · "),
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = if (record.amount == 0.0) "Isento" else formatMoney(record.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1
        )
    }
}

private fun formatRecordTime(instant: Instant): String {
    val zone = ZoneId.systemDefault()
    val zoned = instant.atZone(zone)
    val today = LocalDate.now(zone)
    val time = zoned.format(DateTimeFormatter.ofPattern("HH:mm"))

    return when (zoned.toLocalDate()) {
        today -> time
        today.minusDays(1) -> "ontem $time"
        else -> zoned.format(
            DateTimeFormatter.ofPattern("d MMM HH:mm", Locale.forLanguageTag("pt-BR"))
        ).replace(".", "")
    }
}

// ---------------------------------------------------------------------------
// Editor dialog
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TripRecordEditorDialog(
    editor: TripRecordEditor,
    isSaving: Boolean,
    errorMessage: String?,
    actions: TripCostsActions
) {
    val isFuel = editor is TripRecordEditor.NewFuel || editor is TripRecordEditor.EditFuel
    val isEditing = editor is TripRecordEditor.EditFuel || editor is TripRecordEditor.EditExpense
    // A toll has a window of its own; the other expenses never offer it as a category.
    val isToll = !isFuel && when (editor) {
        is TripRecordEditor.NewExpense -> editor.category == ExpenseCategory.TOLL
        is TripRecordEditor.EditExpense -> ExpenseCategory.fromApi(editor.expense.category) == ExpenseCategory.TOLL
        else -> false
    }

    var liters by remember(editor) {
        mutableStateOf(
            (editor as? TripRecordEditor.EditFuel)
                ?.let { formatDecimalInput(it.entry.liters, MAX_LITERS_FRACTION_DIGITS) }
                .orEmpty()
        )
    }
    var fuelCost by remember(editor) {
        mutableStateOf(
            (editor as? TripRecordEditor.EditFuel)
                ?.let { formatDecimalInput(it.entry.totalCost, MAX_MONEY_FRACTION_DIGITS) }
                .orEmpty()
        )
    }
    var category by remember(editor) {
        mutableStateOf(
            when (editor) {
                is TripRecordEditor.NewExpense -> editor.category
                is TripRecordEditor.EditExpense -> ExpenseCategory.fromApi(editor.expense.category)
                else -> ExpenseCategory.OTHER
            }
        )
    }
    var amount by remember(editor) {
        mutableStateOf(
            (editor as? TripRecordEditor.EditExpense)
                ?.let { formatDecimalInput(it.expense.amount, MAX_MONEY_FRACTION_DIGITS) }
                .orEmpty()
        )
    }
    var description by remember(editor) {
        mutableStateOf(
            (editor as? TripRecordEditor.EditExpense)?.expense?.description.orEmpty()
        )
    }
    var confirmDelete by remember(editor) { mutableStateOf(false) }

    Dialog(
        onDismissRequest = actions.onDismissEditor,
        properties = DialogProperties(
            dismissOnBackPress = !isSaving,
            dismissOnClickOutside = !isSaving,
            usePlatformDefaultWidth = false
        )
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
                    IconBadge(
                        icon = if (isFuel) Icons.Outlined.LocalGasStation else category.icon,
                        size = 44.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = when {
                            isFuel && isEditing -> "Corrigir abastecimento"
                            isFuel -> "Abastecimento"
                            isToll && isEditing -> "Corrigir pedágio"
                            isToll -> "Pedágio"
                            isEditing -> "Corrigir gasto"
                            else -> "Novo gasto"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = WarmInk
                    )
                }

                Text(
                    text = when {
                        isFuel ->
                            "Informe os litros e o valor pago. O preço por litro e o " +
                                "consumo da viagem são calculados para você."

                        isToll ->
                            "Anote a passagem por uma praça de pedágio. Moto isenta? " +
                                "Use Isento: a passagem conta, com R$ 0,00."

                        else ->
                            "Alimentação, estacionamento, hospedagem e outros gastos da viagem."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )

                if (isFuel) {
                    WarmTextField(
                        value = liters,
                        onValueChange = {
                            liters = sanitizeDecimalInput(
                                it,
                                MAX_LITERS_INTEGER_DIGITS,
                                MAX_LITERS_FRACTION_DIGITS
                            )
                        },
                        label = "Litros abastecidos",
                        leadingIcon = Icons.Outlined.LocalGasStation,
                        error = null,
                        enabled = !isSaving,
                        suffix = "L",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    WarmTextField(
                        value = fuelCost,
                        onValueChange = {
                            fuelCost = sanitizeDecimalInput(
                                it,
                                MAX_MONEY_INTEGER_DIGITS,
                                MAX_MONEY_FRACTION_DIGITS
                            )
                        },
                        label = "Valor pago",
                        leadingIcon = Icons.Outlined.Payments,
                        error = null,
                        enabled = !isSaving,
                        suffix = "R$",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    val litersValue = parseDecimalInput(liters)
                    val costValue = parseDecimalInput(fuelCost)

                    if (litersValue != null && litersValue > 0.0 && costValue != null) {
                        Text(
                            text = "≈ ${formatMoney(costValue / litersValue)} por litro",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmInkSoft
                        )
                    }
                } else {
                    if (!isToll) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ExpenseCategory.entries
                                .filter { it != ExpenseCategory.TOLL }
                                .forEach { option ->
                                    ChoicePill(
                                        label = option.label,
                                        icon = option.icon,
                                        selected = option == category,
                                        enabled = !isSaving,
                                        onClick = { category = option }
                                    )
                                }
                        }
                    }

                    WarmTextField(
                        value = amount,
                        onValueChange = {
                            amount = sanitizeDecimalInput(
                                it,
                                MAX_MONEY_INTEGER_DIGITS,
                                MAX_MONEY_FRACTION_DIGITS
                            )
                        },
                        label = "Valor",
                        leadingIcon = Icons.Outlined.Payments,
                        error = null,
                        enabled = !isSaving,
                        suffix = "R$",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )

                    if (isToll) {
                        // Motorcycles are often exempt: the passage still counts.
                        ChoicePill(
                            label = "Isento (R$ 0,00)",
                            selected = parseDecimalInput(amount) == 0.0,
                            enabled = !isSaving,
                            onClick = { amount = "0" }
                        )
                    }

                    WarmTextField(
                        value = description,
                        onValueChange = { description = it.take(MAX_DESCRIPTION_LENGTH) },
                        label = "Descrição (opcional)",
                        leadingIcon = Icons.Outlined.Description,
                        error = null,
                        enabled = !isSaving,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }

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
                    onClick = {
                        if (isFuel) {
                            actions.onSaveFuel(liters, fuelCost)
                        } else {
                            actions.onSaveExpense(category, amount, description)
                        }
                    },
                    enabled = !isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmInk,
                        contentColor = Color.White,
                        disabledContainerColor = WarmInk.copy(alpha = 0.7f),
                        disabledContentColor = Color.White
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Text(
                        text = if (isSaving) "Salvando..." else if (isEditing) "Salvar" else "Registrar",
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    TextButton(
                        onClick = actions.onDismissEditor,
                        enabled = !isSaving
                    ) {
                        Text(text = "Cancelar", color = WarmInkSoft)
                    }

                    if (isEditing) {
                        TextButton(
                            onClick = {
                                if (confirmDelete) actions.onDeleteRecord() else confirmDelete = true
                            },
                            enabled = !isSaving
                        ) {
                            Text(
                                text = if (confirmDelete) "Toque para confirmar" else "Excluir",
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Pieces
// ---------------------------------------------------------------------------

@Composable
internal fun SectionCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(WarmCard)
            .border(BorderStroke(1.dp, WarmHairline), CardShape)
            .padding(22.dp)
    ) {
        content()
    }
}

@Composable
internal fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.ExtraBold,
        letterSpacing = 1.6.sp,
        color = WarmEmber
    )
}

@Composable
internal fun IconBadge(
    icon: ImageVector,
    size: Dp
) {
    Box(
        modifier = Modifier
            .size(size)
            .background(WarmEmberTint, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size * 0.55f),
            tint = WarmEmber
        )
    }
}

@Composable
private fun CostsLoading() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp,
            color = WarmEmber
        )
    }
}

@Composable
private fun CostsError(
    message: String,
    onRetry: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = message,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error
        )

        TextButton(onClick = onRetry) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = WarmEmber
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(text = "Atualizar", color = WarmEmber, fontWeight = FontWeight.Bold)
        }
    }
}

internal fun formatDecimal(value: Double, fractionDigits: Int): String =
    NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR")).apply {
        minimumFractionDigits = fractionDigits
        maximumFractionDigits = fractionDigits
    }.format(value)
