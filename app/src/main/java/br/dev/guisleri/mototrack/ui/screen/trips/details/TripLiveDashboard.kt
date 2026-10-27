package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Toll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.HeroPill
import br.dev.guisleri.mototrack.ui.components.PulsingDot
import br.dev.guisleri.mototrack.ui.components.RoadDecoration
import br.dev.guisleri.mototrack.ui.components.RouteTimeline
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.components.formatLiters
import br.dev.guisleri.mototrack.ui.components.formatMoney
import br.dev.guisleri.mototrack.ui.components.formatTerrain
import br.dev.guisleri.mototrack.ui.theme.EmberHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.delay

private val HeroShape = RoundedCornerShape(32.dp)

/**
 * The current instant, ticking every second while the screen is on. Stops with the
 * lifecycle, so a trip left open in the background costs no battery.
 */
@Composable
internal fun rememberNow(active: Boolean): State<Instant> {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    return produceState(initialValue = Instant.now(), key1 = active, key2 = lifecycle) {
        if (!active) return@produceState

        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            while (true) {
                value = Instant.now()
                delay(1_000)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Hero: the clock of the trip
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TripLiveHero(
    trip: TripResponseDTO,
    now: Instant,
    onAdjustTimesClick: () -> Unit
) {
    val elapsed = tripElapsed(trip, now)
    val zone = ZoneId.systemDefault()
    val startedAt = parseInstant(trip.startedAt)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(HeroShape)
            .background(EmberHeroBrush)
    ) {
        RoadDecoration(
            modifier = Modifier.matchParentSize(),
            color = Color.White
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
                HeroPill(
                    text = "AO VIVO",
                    containerColor = Color.White.copy(alpha = 0.2f),
                    contentColor = Color.White,
                    leading = { PulsingDot(color = Color.White) }
                )

                Text(
                    text = dayOfTrip(trip, now, zone),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }

            Spacer(modifier = Modifier.height(26.dp))

            Text(
                text = "${trip.motorcycle.brand} ${trip.motorcycle.model}".uppercase(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = Color.White.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = elapsed?.let(::formatClock) ?: "--:--:--",
                // Fixed-width digits, so the clock does not shiver as it runs.
                style = MaterialTheme.typography.displayMedium.copy(fontFeatureSettings = "tnum"),
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )

            Text(
                text = startedAt
                    ?.let { "em estrada desde ${formatStartedAt(it, now, zone)}" }
                    ?: "horário de início não registrado",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.78f)
            )

            TextButton(onClick = onAdjustTimesClick) {
                Text(
                    text = if (startedAt == null) "Informar horário de início" else "Ajustar horário de início",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.18f))
            )

            Spacer(modifier = Modifier.height(20.dp))

            RouteTimeline(
                origin = trip.origin,
                destination = trip.destination,
                textColor = Color.White,
                accentColor = Color.White,
                lineColor = Color.White.copy(alpha = 0.45f)
            )

            Spacer(modifier = Modifier.height(18.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LiveChip(Icons.Outlined.Route, formatDistance(trip.distanceKm))
                LiveChip(null, formatTerrain(trip.terrain))
            }
        }
    }
}

@Composable
private fun LiveChip(icon: ImageVector?, text: String) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = Color.White
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            maxLines = 1
        )
    }
}

private fun dayOfTrip(trip: TripResponseDTO, now: Instant, zone: ZoneId): String {
    val first = parseInstant(trip.startedAt)?.atZone(zone)?.toLocalDate()
        ?: runCatching { LocalDate.parse(trip.tripDate) }.getOrNull()
        ?: return ""

    val day = ChronoUnit.DAYS.between(first, now.atZone(zone).toLocalDate()) + 1

    return "Dia ${day.coerceAtLeast(1)}"
}

private fun formatStartedAt(startedAt: Instant, now: Instant, zone: ZoneId): String {
    val zoned = startedAt.atZone(zone)
    val time = zoned.format(DateTimeFormatter.ofPattern("HH:mm"))

    return when (zoned.toLocalDate()) {
        now.atZone(zone).toLocalDate() -> time
        now.atZone(zone).toLocalDate().minusDays(1) -> "ontem, $time"
        else -> zoned.format(DateTimeFormatter.ofPattern("dd/MM, HH:mm"))
    }
}

// ---------------------------------------------------------------------------
// Quick actions
// ---------------------------------------------------------------------------

/** The three things a rider does on the road, one tap away. */
@Composable
internal fun TripQuickActions(
    onFuelClick: () -> Unit,
    onTollClick: () -> Unit,
    onExpenseClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickAction(Icons.Outlined.LocalGasStation, "Abastecer", onFuelClick, Modifier.weight(1f))
        QuickAction(Icons.Outlined.Toll, "Pedágio", onTollClick, Modifier.weight(1f))
        QuickAction(Icons.Outlined.AddCircleOutline, "Gasto", onExpenseClick, Modifier.weight(1f))
    }
}

@Composable
private fun QuickAction(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.heightIn(min = 76.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = WarmInk,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(
            horizontal = 8.dp,
            vertical = 12.dp
        )
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Live numbers
// ---------------------------------------------------------------------------

@Composable
internal fun TripLiveStatsGrid(stats: TripLiveStats) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            LiveStatTile(
                icon = Icons.Outlined.Payments,
                value = formatMoney(stats.totalCost ?: 0.0),
                label = "Total gasto",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            LiveStatTile(
                icon = Icons.Outlined.Route,
                value = stats.costPerKm?.let { "${formatMoney(it)}/km" } ?: "—",
                label = "Custo por km",
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
            LiveStatTile(
                icon = Icons.Outlined.LocalGasStation,
                value = stats.liters?.let(::formatLiters) ?: "—",
                label = stats.averagePricePerLiter
                    ?.let { "combustível · ${formatMoney(it)}/L" }
                    ?: "combustível",
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )

            LiveStatTile(
                icon = Icons.Outlined.Toll,
                value = if (stats.tollCount == 0L) "—" else formatMoney(stats.tollCost),
                label = when (stats.tollCount) {
                    0L -> "pedágios"
                    1L -> "pedágio · 1 passagem"
                    else -> "pedágios · ${stats.tollCount} passagens"
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
        }
    }
}

@Composable
private fun LiveStatTile(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(22.dp))
            .background(WarmCard)
            .border(BorderStroke(1.dp, WarmHairline), RoundedCornerShape(22.dp))
            .padding(14.dp)
    ) {
        IconBadge(icon = icon, size = 34.dp)

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = WarmMuted,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}
