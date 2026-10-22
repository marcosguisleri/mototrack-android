package br.dev.guisleri.mototrack.ui.screen.motorcycles

import br.dev.guisleri.mototrack.ui.components.BrandMark
import br.dev.guisleri.mototrack.ui.components.WarmDialog
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.layout.layout
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleTripSummaryDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.motorcycleType
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.components.MotorcycleHeroImage
import br.dev.guisleri.mototrack.ui.components.motorcycleColor
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceItem
import br.dev.guisleri.mototrack.data.maintenance.MaintenanceSummary
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val ScreenHorizontalPadding = 20.dp

private val AsphaltColor = WarmInkSoft
private val MixedColor = WarmEmber
private val OffRoadColor = WarmSuccess

private data class TerrainItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

private val TerrainItems = listOf(
    TerrainItem("ASPHALT", "Asfalto", Icons.Outlined.Route, AsphaltColor),
    TerrainItem("MIXED", "Misto", Icons.Outlined.Terrain, MixedColor),
    TerrainItem("OFF_ROAD", "Off-road", Icons.Outlined.Park, OffRoadColor)
)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun MotorcyclesScreen(
    uiState: MotorcyclesUiState,
    onRetryClick: () -> Unit,
    onRetryStatisticsClick: () -> Unit,
    onMotorcycleSelected: (Long) -> Unit,
    onSetDefaultClick: (Long) -> Unit,
    onPlanTripClick: (Long) -> Unit,
    onHistoryClick: (Long) -> Unit,
    onTripClick: (Long) -> Unit,
    onAddMotorcycleClick: () -> Unit,
    onDeleteMotorcycleClick: (Long) -> Unit,
    onDismissDeleteIssue: () -> Unit,
    tripCounts: Map<Long, Int>?,
    maintenanceSummary: MaintenanceSummary?,
    onMaintenanceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        MotorcyclesUiState.Loading -> {
            MotorcyclesLoading(
                modifier = modifier
            )
        }

        MotorcyclesUiState.Empty -> {
            MotorcyclesEmpty(
                onAddMotorcycleClick = onAddMotorcycleClick,
                modifier = modifier
            )
        }

        is MotorcyclesUiState.Error -> {
            MotorcyclesError(
                message = uiState.message,
                onRetryClick = onRetryClick,
                modifier = modifier
            )
        }

        is MotorcyclesUiState.Success -> {
            MotorcyclesContent(
                uiState = uiState,
                onRetryStatisticsClick = onRetryStatisticsClick,
                onMotorcycleSelected = onMotorcycleSelected,
                onSetDefaultClick = onSetDefaultClick,
                onPlanTripClick = onPlanTripClick,
                onHistoryClick = onHistoryClick,
                onTripClick = onTripClick,
                onAddMotorcycleClick = onAddMotorcycleClick,
                onDeleteMotorcycleClick = onDeleteMotorcycleClick,
                onDismissDeleteIssue = onDismissDeleteIssue,
                tripCounts = tripCounts,
                maintenanceSummary = maintenanceSummary,
                onMaintenanceClick = onMaintenanceClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MotorcyclesContent(
    uiState: MotorcyclesUiState.Success,
    onRetryStatisticsClick: () -> Unit,
    onMotorcycleSelected: (Long) -> Unit,
    onSetDefaultClick: (Long) -> Unit,
    onPlanTripClick: (Long) -> Unit,
    onHistoryClick: (Long) -> Unit,
    onTripClick: (Long) -> Unit,
    onAddMotorcycleClick: () -> Unit,
    onDeleteMotorcycleClick: (Long) -> Unit,
    onDismissDeleteIssue: () -> Unit,
    tripCounts: Map<Long, Int>?,
    maintenanceSummary: MaintenanceSummary?,
    onMaintenanceClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showHasTrips by remember { mutableStateOf(false) }

    val selectedMotorcycle =
        uiState.motorcycles.first {
            it.id == uiState.selectedMotorcycleId
        }

    val hasSeveralMotorcycles = uiState.motorcycles.size > 1

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = ScreenHorizontalPadding,
                end = ScreenHorizontalPadding,
                top = 20.dp,
                bottom = 28.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        MotorcyclesTopBar(
            motorcycleCount = uiState.motorcycles.size,
            onAddMotorcycleClick = onAddMotorcycleClick
        )

        if (hasSeveralMotorcycles) {
            MotorcycleSelector(
                motorcycles = uiState.motorcycles,
                selectedMotorcycleId = uiState.selectedMotorcycleId,
                defaultMotorcycleId = uiState.defaultMotorcycleId,
                onMotorcycleSelected = onMotorcycleSelected
            )
        }

        MotorcycleHero(
            motorcycle = selectedMotorcycle,
            showDefaultControl = hasSeveralMotorcycles,
            isDefault = selectedMotorcycle.id == uiState.defaultMotorcycleId,
            onSetDefaultClick = {
                onSetDefaultClick(selectedMotorcycle.id)
            }
        )

        when (val statisticsState = uiState.statisticsUiState) {
            MotorcycleStatisticsUiState.Loading -> {
                StatisticsLoading()
            }

            is MotorcycleStatisticsUiState.Error -> {
                StatisticsError(
                    message = statisticsState.message,
                    onRetryClick = onRetryStatisticsClick
                )
            }

            is MotorcycleStatisticsUiState.Success -> {
                StatisticsContent(
                    statistics = statisticsState.statistics,
                    onHistoryClick = {
                        onHistoryClick(selectedMotorcycle.id)
                    },
                    onTripClick = onTripClick
                )
            }
        }

        MaintenanceEntryCard(
            summary = maintenanceSummary,
            onClick = { onMaintenanceClick(selectedMotorcycle.id) }
        )

        MotorcycleActions(
            motorcycle = selectedMotorcycle,
            onPlanTripClick = {
                onPlanTripClick(selectedMotorcycle.id)
            },
            onHistoryClick = {
                onHistoryClick(selectedMotorcycle.id)
            }
        )

        DeleteMotorcycleButton(
            onClick = {
                // The server refuses motorcycles with trips, so say it before asking.
                if ((tripCounts?.get(selectedMotorcycle.id) ?: 0) > 0) {
                    showHasTrips = true
                } else {
                    showDeleteConfirmation = true
                }
            }
        )
    }

    val motorcycleName = "${selectedMotorcycle.brand} ${selectedMotorcycle.model}"
    val knownTrips = tripCounts?.get(selectedMotorcycle.id) ?: 0
    val issue = uiState.deleteIssue

    if (showHasTrips || issue == MotorcycleDeleteIssue.HasTrips) {
        WarmDialog(
            icon = Icons.Outlined.Route,
            title = "Esta moto tem viagens",
            message = if (knownTrips > 0) {
                "A $motorcycleName tem ${formatTripCount(knownTrips.toLong())}. " +
                        "Para excluir a moto, exclua antes as viagens dela."
            } else {
                "A $motorcycleName ainda tem viagens. " +
                        "Para excluir a moto, exclua antes as viagens dela."
            },
            confirmText = "Ver viagens",
            onConfirm = {
                showHasTrips = false
                showDeleteConfirmation = false
                onDismissDeleteIssue()
                onHistoryClick(selectedMotorcycle.id)
            },
            onDismissRequest = {
                showHasTrips = false
                showDeleteConfirmation = false
                onDismissDeleteIssue()
            },
            dismissText = "Entendi"
        )
    } else if (showDeleteConfirmation) {
        WarmDialog(
            icon = Icons.Outlined.DeleteOutline,
            title = "Excluir $motorcycleName?",
            message = "A moto será removida da sua garagem.",
            warning = "Esta ação é permanente e não pode ser desfeita.",
            confirmText = "Excluir moto",
            onConfirm = { onDeleteMotorcycleClick(selectedMotorcycle.id) },
            onDismissRequest = { showDeleteConfirmation = false },
            dismissText = "Cancelar",
            destructive = true,
            isLoading = uiState.isDeleting
        ) {
            (issue as? MotorcycleDeleteIssue.Failed)?.let { failed ->
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = failed.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DeleteMotorcycleButton(
    onClick: () -> Unit
) {
    val error = MaterialTheme.colorScheme.error

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50))
            .clickable(
                onClickLabel = "Excluir moto",
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.DeleteOutline,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = error
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = "Excluir moto",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = error
        )
    }
}

// ---------------------------------------------------------------------------
// Top bar and selector
// ---------------------------------------------------------------------------

@Composable
private fun MotorcyclesTopBar(
    motorcycleCount: Int,
    onAddMotorcycleClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BrandMark()

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (motorcycleCount == 1) "1 moto" else "$motorcycleCount motos",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmHairline)
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = WarmInkSoft
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(WarmInk)
                    .clickable(
                        onClickLabel = "Cadastrar moto",
                        role = Role.Button,
                        onClick = onAddMotorcycleClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = "Cadastrar moto",
                    modifier = Modifier.size(18.dp),
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
private fun MotorcycleSelector(
    motorcycles: List<MotorcycleResponseDTO>,
    selectedMotorcycleId: Long,
    defaultMotorcycleId: Long?,
    onMotorcycleSelected: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        motorcycles.forEach { motorcycle ->
            val selected = motorcycle.id == selectedMotorcycleId
            val shape = RoundedCornerShape(50)

            Row(
                modifier = Modifier
                    .clip(shape)
                    .background(if (selected) WarmInk else WarmCard)
                    .border(
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (selected) WarmInk else WarmHairline
                        ),
                        shape = shape
                    )
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onMotorcycleSelected(motorcycle.id) }
                    )
                    .padding(
                        horizontal = 14.dp,
                        vertical = 10.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (motorcycle.id == defaultMotorcycleId) {
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Moto padrão",
                        modifier = Modifier.size(16.dp),
                        tint = if (selected) Color(0xFFFFA463) else WarmEmber
                    )

                    Spacer(modifier = Modifier.width(6.dp))
                }

                Text(
                    text = "${motorcycle.brand} ${motorcycle.model}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) Color.White else WarmInkSoft,
                    maxLines = 1
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero
// ---------------------------------------------------------------------------

@Composable
private fun MotorcycleHero(
    motorcycle: MotorcycleResponseDTO,
    showDefaultControl: Boolean,
    isDefault: Boolean,
    onSetDefaultClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = motorcycle.brand.uppercase(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            color = WarmEmber,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = motorcycle.model,
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            MistyMountains(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
                    .bleedHorizontally(ScreenHorizontalPadding)
            )

            BasicText(
                text = motorcycle.model.uppercase(),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter),
                style = TextStyle(
                    fontWeight = FontWeight.Black,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    color = WarmInk.copy(alpha = 0.06f)
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 40.sp,
                    maxFontSize = 160.sp
                )
            )

            MotorcycleHeroImage(
                type = motorcycle.motorcycleType,
                colorName = motorcycle.color,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.BottomCenter)
            )

            if (showDefaultControl) {
                DefaultMotorcycleStar(
                    isDefault = isDefault,
                    onClick = onSetDefaultClick,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            motorcycle.motorcycleType?.let { type ->
                SpecChip(text = type.label)
            }
            SpecChip(text = motorcycle.year.toString())
            SpecChip(text = "${motorcycle.engineCapacity} cc")
            SpecChip(
                text = motorcycle.color,
                swatch = motorcycleColor(motorcycle.color)
            )
        }
    }
}

/** Small corner control, so a screenshot of the screen stays clean. */
@Composable
private fun DefaultMotorcycleStar(
    isDefault: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isDefault) WarmEmberTint else Color.Transparent)
            .clickable(
                enabled = !isDefault,
                onClickLabel = "Definir como moto padrão",
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDefault) Icons.Filled.Star else Icons.Outlined.StarBorder,
            contentDescription = if (isDefault) "Moto padrão" else "Definir como moto padrão",
            modifier = Modifier.size(20.dp),
            tint = if (isDefault) WarmEmber else WarmMuted.copy(alpha = 0.7f)
        )
    }
}

@Composable
private fun SpecChip(
    text: String,
    swatch: Color? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        swatch?.let { color ->
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .background(color, CircleShape)
                    .border(1.dp, WarmHairline, CircleShape)
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = WarmInkSoft,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun MistyMountains(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Back range
        drawPolygon(
            fadeBottom(WarmEmber.copy(alpha = 0.07f)),
            0.00f to 1.00f,
            0.00f to 0.60f,
            0.14f to 0.30f,
            0.26f to 0.55f,
            0.40f to 0.10f,
            0.58f to 0.60f,
            0.72f to 0.30f,
            0.86f to 0.55f,
            1.00f to 0.40f,
            1.00f to 1.00f
        )
        // Front range
        drawPolygon(
            fadeBottom(WarmEmber.copy(alpha = 0.11f)),
            0.00f to 1.00f,
            0.00f to 0.78f,
            0.18f to 0.52f,
            0.34f to 0.80f,
            0.52f to 0.45f,
            0.70f to 0.82f,
            0.88f to 0.55f,
            1.00f to 0.75f,
            1.00f to 1.00f
        )
    }
}

/** Keeps the ranges solid behind the bike and melts them away at the bottom. */
private fun DrawScope.fadeBottom(color: Color): Brush =
    Brush.verticalGradient(
        0.0f to color,
        0.7f to color,
        1.0f to color.copy(alpha = 0f),
        startY = 0f,
        endY = size.height
    )

/** Lets a child draw past the horizontal padding of its parent, edge to edge. */
private fun Modifier.bleedHorizontally(padding: Dp): Modifier =
    layout { measurable, constraints ->
        val extra = padding.roundToPx() * 2
        val placeable = measurable.measure(
            constraints.copy(
                minWidth = constraints.maxWidth + extra,
                maxWidth = constraints.maxWidth + extra
            )
        )

        layout(constraints.maxWidth, placeable.height) {
            placeable.place(-padding.roundToPx(), 0)
        }
    }

private fun DrawScope.drawPolygon(
    brush: Brush,
    vararg points: Pair<Float, Float>
) {
    val width = size.width
    val height = size.height

    val path = Path().apply {
        points.forEachIndexed { index, point ->
            val x = width * point.first
            val y = height * point.second

            if (index == 0) moveTo(x, y) else lineTo(x, y)
        }
        close()
    }

    drawPath(
        path = path,
        brush = brush
    )
}

// ---------------------------------------------------------------------------
// Statistics
// ---------------------------------------------------------------------------

@Composable
private fun StatisticsContent(
    statistics: MotorcycleStatisticsResponseDTO,
    onHistoryClick: () -> Unit,
    onTripClick: (Long) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        MotorcycleSummary(
            statistics = statistics
        )

        if (statistics.completedTrips > 0) {
            TripHighlightsCard(
                statistics = statistics,
                onHistoryClick = onHistoryClick,
                onTripClick = onTripClick
            )

            TerrainCard(
                statistics = statistics
            )
        } else {
            NoCompletedTripsCard()
        }
    }
}

@Composable
private fun MotorcycleSummary(
    statistics: MotorcycleStatisticsResponseDTO
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        MotorcycleMetricCard(
            icon = R.drawable.ic_distance,
            value = formatDistanceValue(
                statistics.totalCompletedDistanceKm
            ),
            label = "km rodados",
            modifier = Modifier.weight(1f)
        )

        MotorcycleMetricCard(
            icon = R.drawable.ic_trips,
            value = statistics.completedTrips.toString(),
            label = "viagens concluídas",
            modifier = Modifier.weight(1f)
        )

        MotorcycleMetricCard(
            icon = R.drawable.ic_statistics,
            value = statistics.averageCompletedDistanceKm
                ?.let(::formatDistanceValue)
                ?: "—",
            label = "km por viagem",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MotorcycleMetricCard(
    icon: Int,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .height(112.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(
                horizontal = 8.dp,
                vertical = 12.dp
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = WarmMuted,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

// ---------------------------------------------------------------------------
// Trip highlights
// ---------------------------------------------------------------------------

private data class TripHighlight(
    val label: String,
    val icon: ImageVector,
    val trip: MotorcycleTripSummaryDTO
)

@Composable
private fun TripHighlightsCard(
    statistics: MotorcycleStatisticsResponseDTO,
    onHistoryClick: () -> Unit,
    onTripClick: (Long) -> Unit
) {
    val highlights = listOfNotNull(
        statistics.longestTrip?.let {
            TripHighlight("Maior viagem", Icons.Outlined.EmojiEvents, it)
        },
        statistics.lastCompletedTrip?.let {
            TripHighlight("Última viagem", Icons.Filled.LocationOn, it)
        }
    )

    if (highlights.isEmpty()) return

    WarmSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Destaques",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .clickable(
                        role = Role.Button,
                        onClick = onHistoryClick
                    )
                    .padding(
                        start = 10.dp,
                        top = 4.dp,
                        bottom = 4.dp
                    ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Ver todas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = WarmEmber
                )

                Icon(
                    imageVector = Icons.Outlined.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = WarmEmber
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            highlights.forEach { highlight ->
                TripHighlightRow(
                    highlight = highlight,
                    onClick = { onTripClick(highlight.trip.id) }
                )
            }
        }
    }
}

@Composable
private fun TripHighlightRow(
    highlight: TripHighlight,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(WarmCanvas)
            .clickable(
                onClickLabel = "Abrir viagem",
                role = Role.Button,
                onClick = onClick
            )
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = highlight.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = highlight.label,
                style = MaterialTheme.typography.labelMedium,
                color = WarmMuted
            )

            Text(
                text = "${highlight.trip.origin} → ${highlight.trip.destination}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = WarmInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = formatTripDate(highlight.trip.tripDate),
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkSoft
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatDistance(highlight.trip.distanceKm),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Terrains
// ---------------------------------------------------------------------------

@Composable
private fun TerrainCard(
    statistics: MotorcycleStatisticsResponseDTO
) {
    val totalTrips = statistics.completedTrips

    WarmSectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Terrenos",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )

            Text(
                text = "Total: ${formatTripCount(totalTrips)}",
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            TerrainItems.forEach { terrain ->
                val terrainStats = statistics.terrainStatistics[terrain.key]

                TerrainRow(
                    item = terrain,
                    tripCount = terrainStats?.tripCount ?: 0L,
                    distanceKm = terrainStats?.totalDistanceKm ?: 0.0,
                    totalTrips = totalTrips
                )
            }
        }
    }
}

@Composable
private fun TerrainRow(
    item: TerrainItem,
    tripCount: Long,
    distanceKm: Double,
    totalTrips: Long
) {
    val progress =
        if (totalTrips > 0) {
            (tripCount.toFloat() / totalTrips.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .background(item.color, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    modifier = Modifier.size(17.dp),
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = item.label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmInk
            )

            Text(
                text = if (tripCount == 0L) {
                    "—"
                } else {
                    "${formatTripCount(tripCount)} · ${formatDistance(distanceKm)}"
                },
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkSoft,
                textAlign = TextAlign.End,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(WarmHairline)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(item.color)
            )
        }
    }
}

@Composable
private fun NoCompletedTripsCard() {
    WarmSectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhuma viagem concluída ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Quando você concluir viagens com esta moto, os destaques e os terrenos aparecem aqui.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun WarmSectionCard(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(26.dp)
            )
            .padding(20.dp)
    ) {
        content()
    }
}

// ---------------------------------------------------------------------------
// Maintenance
// ---------------------------------------------------------------------------

/** The door to the maintenance of the selected motorcycle, with how it stands. */
@Composable
private fun MaintenanceEntryCard(
    summary: MaintenanceSummary?,
    onClick: () -> Unit
) {
    val attention = summary?.needsAttention == true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(26.dp)
            )
            .clickable(
                onClickLabel = "Abrir manutenção",
                role = Role.Button,
                onClick = onClick
            )
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Build,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Manutenção",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )

            Text(
                text = when {
                    summary == null || summary.unknown == MaintenanceItem.entries.size ->
                        "Óleo, corrente, pneus, freios e mais"

                    summary.overdue > 0 ->
                        if (summary.overdue == 1) "1 item vencido" else "${summary.overdue} itens vencidos"

                    summary.soon > 0 ->
                        if (summary.soon == 1) "1 item chegando" else "${summary.soon} itens chegando"

                    else -> "Tudo em dia"
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (attention) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    (summary?.overdue ?: 0) > 0 -> Color(0xFFC62828)
                    attention -> Color(0xFFB07C00)
                    else -> WarmInkSoft
                }
            )
        }

        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = WarmMuted
        )
    }
}

// ---------------------------------------------------------------------------
// Actions
// ---------------------------------------------------------------------------

@Composable
private fun MotorcycleActions(
    motorcycle: MotorcycleResponseDTO,
    onPlanTripClick: () -> Unit,
    onHistoryClick: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = onPlanTripClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(50),
            colors = ButtonDefaults.buttonColors(
                containerColor = WarmInk,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "Planejar viagem com a ${motorcycle.model}",
                modifier = Modifier.weight(1f, fill = false),
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }

        OutlinedButton(
            onClick = onHistoryClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp),
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.5.dp, WarmInk.copy(alpha = 0.55f)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = WarmInk
            )
        ) {
            Icon(
                imageVector = Icons.Outlined.Map,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Ver histórico completo",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Loading, empty and error
// ---------------------------------------------------------------------------

@Composable
private fun MotorcyclesLoading(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BrandMark()

        ShimmerBlock(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(200.dp)
                .height(36.dp),
            cornerRadius = 12.dp
        )

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 28.dp
        )

        StatisticsLoading()
    }
}

@Composable
private fun StatisticsLoading() {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(112.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                ShimmerBlock(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 22.dp
                )
            }
        }

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp),
            cornerRadius = 26.dp
        )
    }
}

@Composable
private fun StatisticsError(
    message: String,
    onRetryClick: () -> Unit
) {
    WarmSectionCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onRetryClick,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.5.dp, WarmInk.copy(alpha = 0.55f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = WarmInk
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Tentar novamente",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MotorcyclesEmpty(
    onAddMotorcycleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BrandMark()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(WarmEmberTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.TwoWheeler,
                    contentDescription = null,
                    modifier = Modifier.size(56.dp),
                    tint = WarmEmber
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Sua garagem está vazia",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Cadastre sua primeira moto para acompanhar quilômetros, viagens e terrenos.",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onAddMotorcycleClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmInk,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Add,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Cadastrar moto",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MotorcyclesError(
    message: String,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        BrandMark()

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(112.dp)
                    .background(WarmEmberTint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.WifiOff,
                    contentDescription = null,
                    modifier = Modifier.size(52.dp),
                    tint = WarmEmber
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Não foi possível carregar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onRetryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = WarmInk,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Tentar novamente",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Formatters
// ---------------------------------------------------------------------------

private fun formatDistanceValue(
    distanceKm: Double
): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return formatter.format(distanceKm)
}

private fun formatDistance(
    distanceKm: Double
): String {
    return "${formatDistanceValue(distanceKm)} km"
}

private fun formatTripCount(
    count: Long
): String {
    return if (count == 1L) "1 viagem" else "$count viagens"
}

private fun formatTripDate(
    date: String
): String {
    return runCatching {
        LocalDate
            .parse(date)
            .format(
                DateTimeFormatter.ofPattern(
                    "d MMM yyyy",
                    Locale.forLanguageTag("pt-BR")
                )
            )
            .replace(".", "")
    }.getOrDefault(date)
}
