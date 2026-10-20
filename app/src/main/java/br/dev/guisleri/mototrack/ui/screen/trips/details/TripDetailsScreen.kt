package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val InProgressContainer =
    Color(0xFFE7F6EC)

private val InProgressContent =
    Color(0xFF16803D)

private val CompletedContainer =
    Color(0xFFE9EEF3)

private val CompletedContent =
    Color(0xFF475569)

private val RouteColor =
    Color(0xFF00A6A6)

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
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Atualizar data da viagem?"
            )
        },
        text = {
            Text(
                text =
                    "Esta viagem está planejada para " +
                            "${formatTripDate(tripDate)}. " +
                            "Para iniciá-la agora, a data da viagem " +
                            "será atualizada para hoje."
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(
                    text = "Atualizar e iniciar"
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar"
                )
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
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(
                rememberScrollState()
            )
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        TripDetailsTopBar(
            onBackClick = onBackClick
        )

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        TripDetailsStatusBadge(
            status = trip.status
        )

        Spacer(
            modifier = Modifier.height(18.dp)
        )

        TripDetailsRouteHeader(
            origin = trip.origin,
            destination = trip.destination
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )

        TripDetailsRoutePreview()

        Spacer(
            modifier = Modifier.height(20.dp)
        )

        TripDetailsInfoCard(
            trip = trip
        )

        TripDetailsAction(
            status = trip.status,
            isUpdatingStatus = isUpdatingStatus,
            errorMessage = actionErrorMessage,
            onStartTripClick = onStartTripClick,
            onCompleteTripClick =
                onCompleteTripClick
        )

        Spacer(
            modifier = Modifier.height(24.dp)
        )
    }
}

@Composable
private fun TripDetailsTopBar(
    onBackClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBackClick
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Outlined.ArrowBack,
                contentDescription = "Voltar",
                tint =
                    MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(
            modifier = Modifier.width(4.dp)
        )

        Text(
            text = "Detalhes da viagem",
            style =
                MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color =
                MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TripDetailsStatusBadge(
    status: String
) {
    val containerColor: Color
    val contentColor: Color

    when (status) {

        "PLANNED" -> {
            containerColor =
                MaterialTheme.colorScheme.primary
                    .copy(
                        alpha = 0.12f
                    )

            contentColor =
                MaterialTheme.colorScheme.primary
        }

        "IN_PROGRESS" -> {
            containerColor =
                InProgressContainer

            contentColor =
                InProgressContent
        }

        "COMPLETED" -> {
            containerColor =
                CompletedContainer

            contentColor =
                CompletedContent
        }

        else -> {
            containerColor =
                MaterialTheme.colorScheme
                    .surfaceVariant

            contentColor =
                MaterialTheme.colorScheme
                    .onSurfaceVariant
        }
    }

    Box(
        modifier = Modifier
            .background(
                color = containerColor,
                shape =
                    RoundedCornerShape(50)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            )
    ) {
        Text(
            text =
                formatTripStatus(status)
                    .uppercase(),
            style =
                MaterialTheme.typography
                    .labelMedium,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@Composable
private fun TripDetailsRouteHeader(
    origin: String,
    destination: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        TripDetailsRoutePoint(
            label = "Origem",
            location = origin
        )

        Box(
            modifier = Modifier
                .padding(
                    start = 11.dp
                )
                .width(2.dp)
                .height(24.dp)
                .background(
                    MaterialTheme.colorScheme
                        .primary
                        .copy(
                            alpha = 0.35f
                        )
                )
        )

        TripDetailsRoutePoint(
            label = "Destino",
            location = destination
        )
    }
}

@Composable
private fun TripDetailsRoutePoint(
    label: String,
    location: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment =
            Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(
                    color =
                        MaterialTheme.colorScheme
                            .primary
                            .copy(
                                alpha = 0.12f
                            ),
                    shape = CircleShape
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color =
                            MaterialTheme.colorScheme
                                .primary,
                        shape = CircleShape
                    )
            )
        }

        Spacer(
            modifier = Modifier.width(12.dp)
        )

        Column {
            Text(
                text = label,
                style =
                    MaterialTheme.typography
                        .labelMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Text(
                text = location,
                style =
                    MaterialTheme.typography
                        .titleLarge,
                fontWeight = FontWeight.Bold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TripDetailsRoutePreview() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme
                    .primary
                    .copy(
                        alpha = 0.06f
                    )
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val width = size.width
                val height = size.height

                val roadColor =
                    Color(0xFF64748B)
                        .copy(
                            alpha = 0.10f
                        )

                drawLine(
                    color = roadColor,
                    start = Offset(
                        0f,
                        height * 0.22f
                    ),
                    end = Offset(
                        width,
                        height * 0.52f
                    ),
                    strokeWidth =
                        2.dp.toPx()
                )

                drawLine(
                    color = roadColor,
                    start = Offset(
                        width * 0.18f,
                        0f
                    ),
                    end = Offset(
                        width * 0.62f,
                        height
                    ),
                    strokeWidth =
                        2.dp.toPx()
                )

                drawLine(
                    color = roadColor,
                    start = Offset(
                        0f,
                        height * 0.82f
                    ),
                    end = Offset(
                        width,
                        height * 0.38f
                    ),
                    strokeWidth =
                        2.dp.toPx()
                )

                drawLine(
                    color = roadColor,
                    start = Offset(
                        width * 0.7f,
                        0f
                    ),
                    end = Offset(
                        width * 0.95f,
                        height
                    ),
                    strokeWidth =
                        2.dp.toPx()
                )

                val route =
                    Path().apply {
                        moveTo(
                            width * 0.12f,
                            height * 0.75f
                        )

                        cubicTo(
                            width * 0.30f,
                            height * 0.20f,
                            width * 0.60f,
                            height * 0.80f,
                            width * 0.88f,
                            height * 0.25f
                        )
                    }

                drawPath(
                    path = route,
                    color = RouteColor,
                    style = Stroke(
                        width =
                            6.dp.toPx(),
                        cap =
                            StrokeCap.Round
                    )
                )
            }

            TripDetailsRouteMarker(
                text = "A",
                modifier = Modifier
                    .align(
                        Alignment.BottomStart
                    )
                    .padding(
                        start = 20.dp,
                        bottom = 38.dp
                    )
            )

            TripDetailsRouteMarker(
                text = "B",
                modifier = Modifier
                    .align(
                        Alignment.TopEnd
                    )
                    .padding(
                        top = 38.dp,
                        end = 20.dp
                    )
            )

            Box(
                modifier = Modifier
                    .align(
                        Alignment.BottomCenter
                    )
                    .padding(
                        bottom = 14.dp
                    )
                    .background(
                        color =
                            MaterialTheme.colorScheme
                                .surface
                                .copy(
                                    alpha = 0.92f
                                ),
                        shape =
                            RoundedCornerShape(50)
                    )
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    )
            ) {
                Text(
                    text =
                        "Visualização da rota",
                    style =
                        MaterialTheme.typography
                            .labelSmall,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TripDetailsRouteMarker(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .background(
                color = RouteColor,
                shape = CircleShape
            ),
        contentAlignment =
            Alignment.Center
    ) {
        Text(
            text = text,
            style =
                MaterialTheme.typography
                    .labelMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun TripDetailsInfoCard(
    trip: TripResponseDTO
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation =
            CardDefaults.cardElevation(
                defaultElevation = 1.dp
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                TripDetailsFact(
                    icon =
                        Icons.Outlined.CalendarMonth,
                    label = "Data da viagem",
                    value =
                        formatTripDate(
                            trip.tripDate
                        ),
                    modifier =
                        Modifier.weight(1f)
                )

                TripDetailsFact(
                    icon =
                        Icons.Outlined.Route,
                    label = "Distância",
                    value =
                        formatDistance(
                            trip.distanceKm
                        ),
                    modifier =
                        Modifier.weight(1f)
                )
            }

            HorizontalDivider(
                modifier =
                    Modifier.padding(
                        vertical = 18.dp
                    ),
                color =
                    MaterialTheme.colorScheme
                        .outlineVariant
                        .copy(
                            alpha = 0.5f
                        )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.spacedBy(
                        12.dp
                    )
            ) {
                TripDetailsFact(
                    icon =
                        Icons.Outlined.Terrain,
                    label = "Terreno",
                    value =
                        formatTerrain(
                            trip.terrain
                        ),
                    modifier =
                        Modifier.weight(1f)
                )

                TripDetailsFact(
                    icon =
                        Icons.Outlined.TwoWheeler,
                    label = "Motocicleta",
                    value =
                        "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    modifier =
                        Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TripDetailsFact(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment =
            Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    color =
                        MaterialTheme.colorScheme
                            .primary
                            .copy(
                                alpha = 0.10f
                            ),
                    shape =
                        RoundedCornerShape(
                            10.dp
                        )
                ),
            contentAlignment =
                Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier =
                    Modifier.size(20.dp),
                tint =
                    MaterialTheme.colorScheme
                        .primary
            )
        }

        Spacer(
            modifier = Modifier.width(10.dp)
        )

        Column {
            Text(
                text = label,
                style =
                    MaterialTheme.typography
                        .bodySmall,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant
            )

            Spacer(
                modifier =
                    Modifier.height(2.dp)
            )

            Text(
                text = value,
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                fontWeight =
                    FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme
                        .onSurface,
                maxLines = 2,
                overflow =
                    TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TripDetailsAction(
    status: String,
    isUpdatingStatus: Boolean,
    errorMessage: String?,
    onStartTripClick: () -> Unit,
    onCompleteTripClick: () -> Unit
) {
    when (status) {

        "PLANNED" -> {
            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Button(
                onClick = onStartTripClick,
                enabled =
                    !isUpdatingStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 54.dp
                    ),
                shape =
                    RoundedCornerShape(50)
            ) {
                if (isUpdatingStatus) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                    )
                } else {
                    Text(
                        text =
                            "Iniciar viagem",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        "IN_PROGRESS" -> {
            Spacer(
                modifier =
                    Modifier.height(24.dp)
            )

            Button(
                onClick =
                    onCompleteTripClick,
                enabled =
                    !isUpdatingStatus,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(
                        min = 54.dp
                    ),
                shape =
                    RoundedCornerShape(50)
            ) {
                if (isUpdatingStatus) {
                    CircularProgressIndicator(
                        modifier =
                            Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color =
                            MaterialTheme
                                .colorScheme
                                .onPrimary
                    )
                } else {
                    Text(
                        text =
                            "Concluir viagem",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }

        "COMPLETED" -> {
            Spacer(
                modifier =
                    Modifier.height(20.dp)
            )

            Text(
                text =
                    "Esta viagem foi concluída.",
                modifier =
                    Modifier.fillMaxWidth(),
                style =
                    MaterialTheme.typography
                        .bodyMedium,
                color =
                    MaterialTheme.colorScheme
                        .onSurfaceVariant,
                textAlign =
                    TextAlign.Center
            )
        }
    }

    if (errorMessage != null) {
        Spacer(
            modifier =
                Modifier.height(10.dp)
        )

        Text(
            text = errorMessage,
            modifier =
                Modifier.fillMaxWidth(),
            style =
                MaterialTheme.typography
                    .bodySmall,
            color =
                MaterialTheme.colorScheme.error,
            textAlign =
                TextAlign.Center
        )
    }
}

@Composable
private fun TripDetailsLoading(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        TripDetailsTopBar(
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment =
                Alignment.Center
        ) {
            CircularProgressIndicator()
        }
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
            .padding(
                horizontal = 20.dp,
                vertical = 16.dp
            )
    ) {
        TripDetailsTopBar(
            onBackClick = onBackClick
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment =
                Alignment.Center
        ) {
            Column(
                modifier =
                    Modifier.fillMaxWidth(),
                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(
                            color =
                                MaterialTheme
                                    .colorScheme
                                    .primary
                                    .copy(
                                        alpha = 0.08f
                                    ),
                            shape =
                                CircleShape
                        ),
                    contentAlignment =
                        Alignment.Center
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.WifiOff,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(50.dp),
                        tint =
                            MaterialTheme
                                .colorScheme
                                .primary
                    )
                }

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Text(
                    text =
                        "Não foi possível carregar a viagem",
                    style =
                        MaterialTheme.typography
                            .titleLarge,
                    fontWeight =
                        FontWeight.Bold,
                    textAlign =
                        TextAlign.Center
                )

                Spacer(
                    modifier =
                        Modifier.height(8.dp)
                )

                Text(
                    text = message,
                    style =
                        MaterialTheme.typography
                            .bodyMedium,
                    color =
                        MaterialTheme.colorScheme
                            .onSurfaceVariant,
                    textAlign =
                        TextAlign.Center
                )

                Spacer(
                    modifier =
                        Modifier.height(24.dp)
                )

                Button(
                    onClick =
                        onRetryClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = 52.dp
                        ),
                    shape =
                        RoundedCornerShape(
                            50
                        )
                ) {
                    Icon(
                        imageVector =
                            Icons.Outlined.Refresh,
                        contentDescription =
                            null,
                        modifier =
                            Modifier.size(20.dp)
                    )

                    Spacer(
                        modifier =
                            Modifier.width(8.dp)
                    )

                    Text(
                        text =
                            "Tentar novamente",
                        fontWeight =
                            FontWeight.SemiBold
                    )
                }
            }
        }
    }
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
                    Locale.forLanguageTag(
                        "pt-BR"
                    )
                )
            )
            .replace(
                ".",
                ""
            )
    }.getOrDefault(date)
}

private fun formatDistance(
    distanceKm: Double
): String {
    val formatter =
        NumberFormat.getNumberInstance(
            Locale.forLanguageTag(
                "pt-BR"
            )
        ).apply {
            minimumFractionDigits = 0
            maximumFractionDigits = 1
        }

    return "${formatter.format(distanceKm)} km"
}

private fun formatTerrain(
    terrain: String
): String {
    return when (terrain) {
        "ASPHALT" ->
            "Asfalto"

        "MIXED" ->
            "Misto"

        "OFF_ROAD" ->
            "Off-road"

        else ->
            terrain
    }
}

private fun formatTripStatus(
    status: String
): String {
    return when (status) {
        "PLANNED" ->
            "Planejada"

        "IN_PROGRESS" ->
            "Em andamento"

        "COMPLETED" ->
            "Concluída"

        else ->
            status
    }
}