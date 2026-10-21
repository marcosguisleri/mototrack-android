package br.dev.guisleri.mototrack.ui.screen.trips.details

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.motorcycle.motorcycleType
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.CircleActionButton
import br.dev.guisleri.mototrack.ui.components.HeroPill
import br.dev.guisleri.mototrack.ui.components.PulsingDot
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.RouteTimeline
import br.dev.guisleri.mototrack.ui.components.ShimmerBlock
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.motorcycleColor
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.theme.EmberHeroBrush
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
import java.text.NumberFormat
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.Locale

private val PosterShape = RoundedCornerShape(32.dp)
private val SuccessOnDark = Color(0xFF8FD9A8)

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun TripDetailsScreen(
    uiState: TripDetailsUiState,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    onStartTripClick: () -> Unit,
    onCompleteTripClick: () -> Unit,
    onConfirmTripDateUpdate: () -> Unit,
    onDismissTripDateUpdate: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (uiState) {

        TripDetailsUiState.Loading -> {
            TripDetailsLoading(
                onBackClick = onBackClick,
                modifier = modifier
            )
        }

        is TripDetailsUiState.Error -> {
            TripDetailsError(
                message = uiState.message,
                onRetryClick = onRetryClick,
                onBackClick = onBackClick,
                modifier = modifier
            )
        }

        is TripDetailsUiState.Success -> {
            TripDetailsContent(
                trip = uiState.trip,
                isUpdatingStatus = uiState.isUpdatingStatus,
                actionErrorMessage = uiState.actionErrorMessage,
                onBackClick = onBackClick,
                onStartTripClick = onStartTripClick,
                onCompleteTripClick = onCompleteTripClick,
                modifier = modifier
            )

            if (uiState.requiresTripDateUpdateConfirmation) {
                TripDateUpdateConfirmationDialog(
                    tripDate = uiState.trip.tripDate,
                    completing = uiState.trip.status == "IN_PROGRESS",
                    onConfirm = onConfirmTripDateUpdate,
                    onDismiss = onDismissTripDateUpdate
                )
            }
        }
    }
}

@Composable
private fun TripDateUpdateConfirmationDialog(
    tripDate: String,
    completing: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Atualizar data da viagem?")
        },
        text = {
            Text(
                text = if (completing) {
                    "A data desta viagem é ${formatTripDate(tripDate)}, " +
                            "depois de hoje. Para concluí-la, a data da " +
                            "viagem será atualizada para hoje."
                } else {
                    "Esta viagem está planejada para " +
                            "${formatTripDate(tripDate)}. " +
                            "Para iniciá-la agora, a data da viagem " +
                            "será atualizada para hoje."
                }
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = if (completing) {
                        "Atualizar e concluir"
                    } else {
                        "Atualizar e iniciar"
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "Cancelar")
            }
        }
    )
}

@Composable
private fun TripDetailsContent(
    trip: TripResponseDTO,
    isUpdatingStatus: Boolean,
    actionErrorMessage: String?,
    onBackClick: () -> Unit,
    onStartTripClick: () -> Unit,
    onCompleteTripClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                start = 20.dp,
                end = 20.dp,
                top = 12.dp,
                bottom = 28.dp
            )
    ) {
        TripDetailsTopBar(
            onBackClick = onBackClick,
            onShareClick = {
                shareTrip(context, trip)
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        TripPoster(trip = trip)

        Spacer(modifier = Modifier.height(14.dp))

        TripNumbers(trip = trip)

        Spacer(modifier = Modifier.height(14.dp))

        TripCostsCard(trip = trip)

        Spacer(modifier = Modifier.height(24.dp))

        TripDetailsAction(
            status = trip.status,
            isUpdatingStatus = isUpdatingStatus,
            errorMessage = actionErrorMessage,
            onStartTripClick = onStartTripClick,
            onCompleteTripClick = onCompleteTripClick,
            onShareClick = {
                shareTrip(context, trip)
            }
        )

        Spacer(modifier = Modifier.height(28.dp))

        BrandFooter()
    }
}

// ---------------------------------------------------------------------------
// Top bar
// ---------------------------------------------------------------------------

@Composable
private fun TripDetailsTopBar(
    onBackClick: () -> Unit,
    onShareClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CircleActionButton(
            icon = Icons.AutoMirrored.Outlined.ArrowBack,
            contentDescription = "Voltar",
            onClick = onBackClick
        )

        Spacer(modifier = Modifier.weight(1f))

        if (onShareClick != null) {
            CircleActionButton(
                icon = Icons.Outlined.Share,
                contentDescription = "Compartilhar viagem",
                onClick = onShareClick
            )
        }
    }
}


// ---------------------------------------------------------------------------
// Poster: the motorcycle is the hero, the route is the story
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TripPoster(
    trip: TripResponseDTO
) {
    val inProgress = trip.status == "IN_PROGRESS"
    val accent = if (inProgress) Color.White else WarmEmberOnDark
    val chipColor = Color.White.copy(alpha = if (inProgress) 0.18f else 0.1f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(PosterShape)
            .background(if (inProgress) EmberHeroBrush else GraphiteHeroBrush)
    ) {
        if (!inProgress) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WarmEmber.copy(alpha = 0.38f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.12f, size.height),
                        radius = size.width * 0.85f
                    ),
                    radius = size.width * 0.85f,
                    center = Offset(size.width * 0.12f, size.height)
                )
            }
        }

        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
        )

        Icon(
            imageVector = Icons.Outlined.TwoWheeler,
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 64.dp, y = 36.dp)
                .size(180.dp),
            tint = Color.White.copy(alpha = 0.08f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusPill(status = trip.status)

                Text(
                    text = formatTripDate(trip.tripDate),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.75f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = trip.motorcycle.brand.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = trip.motorcycle.model,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                trip.motorcycle.motorcycleType?.let { type ->
                    SpecChip(
                        text = type.label,
                        containerColor = chipColor
                    )
                }

                SpecChip(
                    text = trip.motorcycle.year.toString(),
                    containerColor = chipColor
                )

                SpecChip(
                    text = "${trip.motorcycle.engineCapacity} cc",
                    containerColor = chipColor
                )

                SpecChip(
                    text = trip.motorcycle.color,
                    containerColor = chipColor,
                    swatch = motorcycleColor(trip.motorcycle.color)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.14f))
            )

            Spacer(modifier = Modifier.height(24.dp))

            RouteTimeline(
                origin = trip.origin,
                destination = trip.destination,
                textColor = Color.White,
                accentColor = accent,
                lineColor = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatKmNumber(trip.distanceKm),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 1,
                    modifier = Modifier.weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "km",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text(
                text = when (trip.status) {
                    "COMPLETED" -> "de estrada percorridos"
                    "IN_PROGRESS" -> "de estrada pela frente"
                    else -> "de estrada planejados"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun StatusPill(
    status: String
) {
    when (status) {
        "IN_PROGRESS" -> HeroPill(
            text = "EM ANDAMENTO",
            containerColor = Color.White.copy(alpha = 0.2f),
            contentColor = Color.White,
            leading = { PulsingDot(color = Color.White) }
        )

        "COMPLETED" -> HeroPill(
            text = "CONCLUÍDA",
            containerColor = SuccessOnDark.copy(alpha = 0.18f),
            contentColor = SuccessOnDark,
            leading = {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = SuccessOnDark
                )
            }
        )

        else -> HeroPill(
            text = "PLANEJADA",
            containerColor = WarmEmber.copy(alpha = 0.22f),
            contentColor = WarmEmberOnDark,
            leading = {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = WarmEmberOnDark
                )
            }
        )
    }
}

@Composable
private fun SpecChip(
    text: String,
    containerColor: Color,
    swatch: Color? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(
                horizontal = 12.dp,
                vertical = 7.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        swatch?.let { color ->
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
            )

            Spacer(modifier = Modifier.width(7.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Numbers
// ---------------------------------------------------------------------------

@Composable
private fun TripNumbers(
    trip: TripResponseDTO
) {
    val (timeValue, timeLabel) = timeStat(trip)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        NumberTile(
            icon = Icons.Outlined.Terrain,
            value = formatTerrain(trip.terrain),
            label = "Terreno",
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )

        NumberTile(
            icon = Icons.Outlined.CalendarMonth,
            value = formatTripDate(trip.tripDate),
            label = if (trip.status == "PLANNED") "Partida" else "Data",
            modifier = Modifier
                .weight(1.25f)
                .fillMaxHeight()
        )

        NumberTile(
            icon = Icons.Outlined.Schedule,
            value = timeValue,
            label = timeLabel,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        )
    }
}

@Composable
private fun NumberTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(22.dp)
            )
            .padding(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WarmMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Costs (sample data, see TripCostSample)
// ---------------------------------------------------------------------------

@Composable
private fun TripCostsCard(
    trip: TripResponseDTO
) {
    val costs = sampleCostFor(trip)
    val isCompleted = trip.status == "COMPLETED"

    val fuelWeight = costs.fuelCost.toFloat().coerceAtLeast(0.01f)
    val tollWeight = costs.tollCost.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "GASTOS",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = WarmEmber
            )

            Text(
                text = if (isCompleted) "Registrado" else "Estimado",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmHairline)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = WarmInkSoft
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = formatMoney(costs.total),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isCompleted) "gastos no total" else "total estimado",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "${formatMoney(costs.costPerKm)}/km",
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmEmberTint)
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmEmberDeep
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(50))
        ) {
            Box(
                modifier = Modifier
                    .weight(fuelWeight)
                    .fillMaxHeight()
                    .background(WarmEmber)
            )

            if (tollWeight > 0f) {
                Box(
                    modifier = Modifier
                        .weight(tollWeight)
                        .fillMaxHeight()
                        .background(WarmInk)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        CostRow(
            icon = Icons.Outlined.LocalGasStation,
            dotColor = WarmEmber,
            title = "Combustível",
            detail = "${formatLiters(costs.fuelLiters)} de gasolina",
            value = formatMoney(costs.fuelCost)
        )

        Spacer(modifier = Modifier.height(14.dp))

        CostRow(
            icon = Icons.Outlined.Toll,
            dotColor = WarmInk,
            title = "Pedágios",
            detail = when (costs.tollCount) {
                0 -> "Nenhuma praça"
                1 -> "1 passagem"
                else -> "${costs.tollCount} passagens"
            },
            value = formatMoney(costs.tollCost)
        )

        if (SHOW_SAMPLE_COSTS_NOTICE) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Valores de exemplo, calculados a partir da distância.",
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted
            )
        }
    }
}

@Composable
private fun CostRow(
    icon: ImageVector,
    dotColor: Color,
    title: String,
    detail: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(WarmEmberTint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = WarmEmber
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, CircleShape)
                )

                Spacer(modifier = Modifier.width(6.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = WarmInk
                )
            }

            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = WarmMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Action and footer
// ---------------------------------------------------------------------------

@Composable
private fun TripDetailsAction(
    status: String,
    isUpdatingStatus: Boolean,
    errorMessage: String?,
    onStartTripClick: () -> Unit,
    onCompleteTripClick: () -> Unit,
    onShareClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))
        }

        when (status) {
            "PLANNED" -> ActionButton(
                text = "Iniciar viagem",
                isLoading = isUpdatingStatus,
                containerColor = WarmInk,
                contentColor = Color.White,
                onClick = onStartTripClick
            )

            "IN_PROGRESS" -> ActionButton(
                text = "Concluir viagem",
                isLoading = isUpdatingStatus,
                containerColor = WarmEmber,
                contentColor = Color.White,
                onClick = onCompleteTripClick
            )

            "COMPLETED" -> ActionButton(
                text = "Compartilhar viagem",
                isLoading = false,
                containerColor = WarmInk,
                contentColor = Color.White,
                icon = Icons.Outlined.Share,
                onClick = onShareClick
            )
        }
    }
}

@Composable
private fun ActionButton(
    text: String,
    isLoading: Boolean,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    icon: ImageVector = Icons.AutoMirrored.Outlined.ArrowForward
) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 58.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.7f),
            disabledContentColor = contentColor
        )
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = contentColor
            )

            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "Atualizando...",
                fontWeight = FontWeight.Bold
            )
        } else {
            Text(
                text = text,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Small signature at the end, so a screenshot carries the brand. */
@Composable
private fun BrandFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = buildAnnotatedString {
                withStyle(SpanStyle(color = WarmInk)) {
                    append("MOTO")
                }
                withStyle(SpanStyle(color = WarmEmber)) {
                    append("TRACK")
                }
            },
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Cada quilômetro conta uma história.",
            style = MaterialTheme.typography.bodySmall,
            color = WarmMuted
        )
    }
}

// ---------------------------------------------------------------------------
// Loading and error
// ---------------------------------------------------------------------------

@Composable
private fun TripDetailsLoading(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            )
    ) {
        TripDetailsTopBar(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(16.dp))

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp),
            cornerRadius = 32.dp
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
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

        Spacer(modifier = Modifier.height(14.dp))

        ShimmerBlock(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            cornerRadius = 28.dp
        )
    }
}

@Composable
private fun TripDetailsError(
    message: String,
    onRetryClick: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(
                horizontal = 20.dp,
                vertical = 12.dp
            )
    ) {
        TripDetailsTopBar(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Não foi possível carregar a viagem",
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
// Helpers
// ---------------------------------------------------------------------------

private fun shareTrip(
    context: Context,
    trip: TripResponseDTO
) {
    val verb = when (trip.status) {
        "COMPLETED" -> "Fiz esta viagem"
        "IN_PROGRESS" -> "Estou nesta viagem"
        else -> "Vou fazer esta viagem"
    }

    val text = buildString {
        appendLine("🏍️ $verb: ${trip.origin} → ${trip.destination}")
        appendLine(
            "${formatKmNumber(trip.distanceKm)} km · ${formatTerrain(trip.terrain)} · " +
                    formatTripDate(trip.tripDate)
        )
        appendLine("${trip.motorcycle.brand} ${trip.motorcycle.model}")
        appendLine()
        append("Registrado no MotoTrack")
    }

    val send = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }

    context.startActivity(Intent.createChooser(send, "Compartilhar viagem"))
}

/** Value and caption of the third number tile, which depends on the status. */
private fun timeStat(trip: TripResponseDTO): Pair<String, String> {
    val date = runCatching { LocalDate.parse(trip.tripDate) }.getOrNull()
        ?: return "—" to ""
    val days = ChronoUnit.DAYS.between(LocalDate.now(), date)

    return when (trip.status) {
        "PLANNED" -> when {
            days < 0 -> "Atrasada" to "data passada"
            days == 0L -> "Hoje" to "dia da partida"
            days == 1L -> "Amanhã" to "dia da partida"
            else -> "$days dias" to "para a partida"
        }

        "IN_PROGRESS" -> {
            val day = (-days + 1).coerceAtLeast(1)
            "Dia $day" to "de estrada"
        }

        else -> when {
            days == 0L -> "Hoje" to "concluída"
            days == -1L -> "Ontem" to "concluída"
            days < -1 -> "há ${-days} dias" to "concluída"
            else -> "—" to ""
        }
    }
}

private fun formatKmNumber(distanceKm: Double): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 0
        maximumFractionDigits = 1
    }

    return formatter.format(distanceKm)
}

private fun formatLiters(liters: Double): String {
    val formatter = NumberFormat.getNumberInstance(
        Locale.forLanguageTag("pt-BR")
    ).apply {
        minimumFractionDigits = 1
        maximumFractionDigits = 1
    }

    return "${formatter.format(liters)} L"
}

