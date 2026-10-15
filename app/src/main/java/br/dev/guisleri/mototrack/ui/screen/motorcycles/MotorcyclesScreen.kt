package br.dev.guisleri.mototrack.ui.screen.motorcycles

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.Park
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleStatisticsResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleTripSummaryDTO
import java.text.NumberFormat
import java.util.Locale

// ---------------------------------------------------------------------------
// Colors that only exist on this screen (highlight tiles and terrain bars)
// ---------------------------------------------------------------------------

private val TrophyContainer = Color(0xFFFFF4D6)
private val TrophyTint = Color(0xFFF59E0B)
private val PinContainer = Color(0xFFFDE8E8)
private val PinTint = Color(0xFFEF4444)

private val AsphaltColor = Color(0xFF475569)
private val MixedColor = Color(0xFFA9825B)
private val OffRoadColor = Color(0xFF2F6B3A)

private data class TripHighlight(
    val label: String,
    val icon: ImageVector,
    val iconTint: Color,
    val iconContainer: Color,
    val trip: MotorcycleTripSummaryDTO
)

private data class TerrainItem(
    val key: String,
    val label: String,
    val icon: ImageVector,
    val color: Color
)

// Same keys the API sends in terrainStatistics
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
    onMotorcycleSelected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    onTripsClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {}
) {
    when (uiState) {

        MotorcyclesUiState.Loading -> {
            MotorcyclesLoading(
                modifier = modifier
            )
        }

        MotorcyclesUiState.Empty -> {
            MotorcyclesEmpty(
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
                onRetryClick = onRetryClick,
                onMotorcycleSelected = onMotorcycleSelected,
                onTripsClick = onTripsClick,
                onSettingsClick = onSettingsClick,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun MotorcyclesContent(
    uiState: MotorcyclesUiState.Success,
    onRetryClick: () -> Unit,
    onMotorcycleSelected: (Long) -> Unit,
    onTripsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedMotorcycle =
        uiState.motorcycles.first {
            it.id == uiState.selectedMotorcycleId
        }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 20.dp
            ),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {

        MotorcyclesTopBar(
            onSettingsClick = onSettingsClick
        )

        if (uiState.motorcycles.size > 1) {
            MotorcycleSelector(
                motorcycles = uiState.motorcycles,
                selectedMotorcycleId = uiState.selectedMotorcycleId,
                onMotorcycleSelected = onMotorcycleSelected
            )
        }

        MotorcycleHero(
            motorcycle = selectedMotorcycle
        )

        when (val statisticsState = uiState.statisticsUiState) {
            MotorcycleStatisticsUiState.Loading -> {
                StatisticsLoading()
            }

            is MotorcycleStatisticsUiState.Error -> {
                StatisticsError(
                    message = statisticsState.message,
                    onRetryClick = onRetryClick
                )
            }

            is MotorcycleStatisticsUiState.Success -> {
                StatisticsContent(
                    statistics = statisticsState.statistics,
                    onTripsClick = onTripsClick
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Top bar and selector
// ---------------------------------------------------------------------------

@Composable
private fun MotorcyclesTopBar(
    onSettingsClick: () -> Unit
) {
    val darkColor = MaterialTheme.colorScheme.onBackground
    val brandColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(R.drawable.ic_mototrack_symbol),
                contentDescription = null,
                modifier = Modifier.size(
                    width = 39.dp,
                    height = 24.dp
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = buildAnnotatedString {
                    withStyle(SpanStyle(color = darkColor)) {
                        append("Moto")
                    }
                    withStyle(SpanStyle(color = brandColor)) {
                        append("Track")
                    }
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.ExtraBold,
                fontStyle = FontStyle.Italic
            )
        }

        IconButton(
            onClick = onSettingsClick
        ) {
            Icon(
                imageVector = Icons.Outlined.Settings,
                contentDescription = "Configurações",
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun MotorcycleSelector(
    motorcycles: List<MotorcycleResponseDTO>,
    selectedMotorcycleId: Long,
    onMotorcycleSelected: (Long) -> Unit
) {
    val brandColor = MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        motorcycles.forEach { motorcycle ->
            FilterChip(
                selected = motorcycle.id == selectedMotorcycleId,
                onClick = {
                    onMotorcycleSelected(motorcycle.id)
                },
                label = {
                    Text("${motorcycle.brand} ${motorcycle.model}")
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = brandColor.copy(alpha = 0.12f),
                    selectedLabelColor = brandColor
                )
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Hero
// ---------------------------------------------------------------------------

@Composable
private fun MotorcycleHero(
    motorcycle: MotorcycleResponseDTO
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "${motorcycle.brand} ${motorcycle.model}",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Minha motocicleta",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            MistyMountains(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .align(Alignment.BottomCenter)
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
                    color = MaterialTheme.colorScheme.onSurface
                        .copy(alpha = 0.06f)
                ),
                maxLines = 1,
                autoSize = TextAutoSize.StepBased(
                    minFontSize = 40.sp,
                    maxFontSize = 160.sp
                )
            )

            Image(
                painter = painterResource(R.drawable.motorcycle_hero),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .align(Alignment.BottomCenter),
                contentScale = ContentScale.FillWidth
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "${motorcycle.year}  •  ${motorcycle.color}  •  " +
                    "${motorcycle.engineCapacity} cc",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Faint mountain range behind the bike, like the misty background of the mockup. */
@Composable
private fun MistyMountains(
    modifier: Modifier = Modifier
) {
    val tint = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        // Back range
        drawPolygon(
            tint.copy(alpha = 0.04f),
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
            tint.copy(alpha = 0.05f),
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

private fun DrawScope.drawPolygon(
    color: Color,
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
        color = color
    )
}

// ---------------------------------------------------------------------------
// Statistics
// ---------------------------------------------------------------------------

@Composable
private fun StatisticsContent(
    statistics: MotorcycleStatisticsResponseDTO,
    onTripsClick: () -> Unit
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
                onTripsClick = onTripsClick
            )

            TerrainCard(
                statistics = statistics
            )
        } else {
            NoCompletedTripsCard()
        }

        HistoryButton(
            onClick = onTripsClick
        )
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
            label = "km média",
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
    Card(
        modifier = modifier.height(88.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 6.dp,
                    vertical = 8.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                modifier = Modifier.size(17.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )

            Spacer(
                modifier = Modifier.height(3.dp)
            )

            Text(
                text = value,
                fontSize = 20.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(
                modifier = Modifier.height(1.dp)
            )

            Text(
                text = label,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Trip highlights
// ---------------------------------------------------------------------------

@Composable
private fun TripHighlightsCard(
    statistics: MotorcycleStatisticsResponseDTO,
    onTripsClick: () -> Unit
) {
    val highlights = listOfNotNull(
        statistics.longestTrip?.let {
            TripHighlight(
                label = "Maior viagem",
                icon = Icons.Outlined.EmojiEvents,
                iconTint = TrophyTint,
                iconContainer = TrophyContainer,
                trip = it
            )
        },
        statistics.lastCompletedTrip?.let {
            TripHighlight(
                label = "Última viagem",
                icon = Icons.Filled.LocationOn,
                iconTint = PinTint,
                iconContainer = PinContainer,
                trip = it
            )
        }
    )

    if (highlights.isEmpty()) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Destaques das Viagens",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.clickable(onClick = onTripsClick),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Ver todas",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    Text(
                        text = "›",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                highlights.forEach { highlight ->
                    TripHighlightRow(
                        highlight = highlight,
                        onClick = onTripsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun TripHighlightRow(
    highlight: TripHighlight,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(16.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant
                    .copy(alpha = 0.5f),
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(highlight.iconContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = highlight.icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = highlight.iconTint
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = highlight.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "${highlight.trip.origin} → ${highlight.trip.destination}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = formatDistance(highlight.trip.distanceKm),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = "›",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
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

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Terrenos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "Total: ${formatTripCount(totalTrips)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TerrainItems.forEach { terrain ->
                    TerrainRow(
                        item = terrain,
                        tripCount = statistics
                            .terrainStatistics[terrain.key]
                            ?.tripCount
                            ?: 0L,
                        totalTrips = totalTrips
                    )
                }
            }
        }
    }
}

@Composable
private fun TerrainRow(
    item: TerrainItem,
    tripCount: Long,
    totalTrips: Long
) {
    val progress =
        if (totalTrips > 0) {
            (tripCount.toFloat() / totalTrips.toFloat()).coerceIn(0f, 1f)
        } else {
            0f
        }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Box(
            modifier = Modifier
                .size(32.dp)
                .background(
                    color = item.color,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = item.icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = item.label,
            modifier = Modifier.width(68.dp),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Track and filled part of the bar
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(item.color)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Text(
            text = formatTripCount(tripCount),
            modifier = Modifier.width(64.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun NoCompletedTripsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Nenhuma viagem concluída ainda",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Quando você concluir viagens com esta moto, os destaques e os terrenos aparecem aqui.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HistoryButton(
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.onBackground,
            contentColor = MaterialTheme.colorScheme.background
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.align(Alignment.Center),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Map,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = "Ver histórico completo",
                    fontWeight = FontWeight.SemiBold
                )
            }

            Text(
                text = "›",
                modifier = Modifier.align(Alignment.CenterEnd),
                style = MaterialTheme.typography.titleLarge
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

        MotorcyclesTopBar(
            onSettingsClick = {}
        )

        SkeletonBlock(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(200.dp)
                .height(32.dp)
        )

        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            cornerRadius = 24.dp
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
                .height(88.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            repeat(3) {
                SkeletonBlock(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    cornerRadius = 16.dp
                )
            }
        }

        SkeletonBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            cornerRadius = 20.dp
        )
    }
}

@Composable
private fun SkeletonBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    Box(
        modifier = modifier.background(
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f),
            shape = RoundedCornerShape(cornerRadius)
        )
    )
}

@Composable
private fun StatisticsError(
    message: String,
    onRetryClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onRetryClick
            ) {
                Text(
                    text = "Tentar novamente",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun MotorcyclesEmpty(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.08f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.TwoWheeler,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Sua garagem está vazia",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Cadastre sua primeira moto para acompanhar quilômetros, viagens e terrenos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(
                        alpha = 0.08f
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.WifiOff,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Não foi possível carregar",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onRetryClick,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp),
            shape = RoundedCornerShape(50)
        ) {
            Icon(
                imageVector = Icons.Outlined.Refresh,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Tentar novamente",
                fontWeight = FontWeight.SemiBold
            )
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