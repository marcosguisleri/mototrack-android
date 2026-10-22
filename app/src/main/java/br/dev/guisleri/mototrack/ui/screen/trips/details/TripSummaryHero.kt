package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.RouteTimeline
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatMoney
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.components.formatTripDate
import br.dev.guisleri.mototrack.ui.screen.trips.details.costs.TripCostsUiState
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SummaryShape = RoundedCornerShape(32.dp)

/**
 * How a finished trip turned out, at a glance: what it cost first, then the
 * figures of the ride. The route and the motorcycle stay, smaller, as context.
 */
@Composable
internal fun TripSummaryHero(
    trip: TripResponseDTO,
    costs: TripCostsUiState,
    stats: TripLiveStats,
    elapsed: Duration?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SummaryShape)
            .background(GraphiteHeroBrush)
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WarmEmber.copy(alpha = 0.38f), Color.Transparent),
                    center = Offset(size.width * 0.12f, size.height),
                    radius = size.width * 0.9f
                ),
                radius = size.width * 0.9f,
                center = Offset(size.width * 0.12f, size.height)
            )
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
            tint = Color.White.copy(alpha = 0.07f)
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

            Spacer(modifier = Modifier.height(24.dp))

            RouteTimeline(
                origin = trip.origin,
                destination = trip.destination,
                textColor = Color.White,
                accentColor = WarmEmberOnDark,
                lineColor = Color.White.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = listOfNotNull(
                    "${trip.motorcycle.brand} ${trip.motorcycle.model}",
                    formatTerrain(trip.terrain),
                    tripTimeLine(trip)
                ).joinToString(" · "),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(22.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.14f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "TOTAL GASTO",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = WarmEmberOnDark
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = when {
                    stats.totalCost != null -> formatMoney(stats.totalCost)
                    costs.hasData -> formatMoney(0.0)
                    else -> "—"
                },
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            stats.costPerKm?.let { perKm ->
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${formatMoney(perKm)} por km",
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(WarmEmber.copy(alpha = 0.28f))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = WarmEmberOnDark,
                    maxLines = 1
                )
            }

            if (stats.totalCost == null && costs.hasData) {
                Text(
                    text = "Nenhum gasto registrado nesta viagem.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.65f)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStat(
                        value = formatDistance(trip.distanceKm),
                        label = "Distância",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    SummaryStat(
                        value = elapsed?.let(::formatDuration) ?: "—",
                        label = "Tempo de estrada",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SummaryStat(
                        value = stats.kmPerLiter?.let { "${formatDecimal(it, 1)} km/L" } ?: "—",
                        label = "Consumo médio",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )

                    SummaryStat(
                        value = stats.averageSpeedKmh?.let { "${formatDecimal(it, 0)} km/h" } ?: "—",
                        label = "Velocidade média",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.09f))
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.6f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** "partiu 12:05 · chegou 12:12", or just the one that was recorded. */
private fun tripTimeLine(trip: TripResponseDTO): String? {
    val zone = ZoneId.systemDefault()
    val started = parseInstant(trip.startedAt)
    val finished = parseInstant(trip.completedAt)

    fun format(instant: Instant, other: Instant?): String {
        val zoned = instant.atZone(zone)
        val sameDay = other == null ||
            zoned.toLocalDate() == other.atZone(zone).toLocalDate()

        return zoned.format(
            DateTimeFormatter.ofPattern(if (sameDay) "HH:mm" else "dd/MM HH:mm")
        )
    }

    return when {
        started != null && finished != null ->
            "${format(started, finished)} → ${format(finished, started)}"

        started != null -> "partiu ${format(started, null)}"
        finished != null -> "chegou ${format(finished, null)}"
        else -> null
    }
}
