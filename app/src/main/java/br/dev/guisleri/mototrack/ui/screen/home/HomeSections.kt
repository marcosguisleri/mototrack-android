package br.dev.guisleri.mototrack.ui.screen.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.data.model.motorcycle.MotorcycleResponseDTO
import br.dev.guisleri.mototrack.data.model.motorcycle.motorcycleType
import br.dev.guisleri.mototrack.ui.components.BrandMark
import br.dev.guisleri.mototrack.ui.components.MotorcycleHeroImage
import br.dev.guisleri.mototrack.ui.components.formatNumber
import br.dev.guisleri.mototrack.ui.components.motorcycleColor
import br.dev.guisleri.mototrack.ui.theme.GraphiteHeroBrush
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.WarmMuted
import java.text.NumberFormat
import br.dev.guisleri.mototrack.ui.screen.profile.RiderLevel
import java.time.LocalDate
import java.util.Locale

private const val EARTH_CIRCUMFERENCE_KM = 40_075.0
private val MonthInitials = listOf("J", "F", "M", "A", "M", "J", "J", "A", "S", "O", "N", "D")
private val MonthNames = listOf(
    "jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"
)

// ---------------------------------------------------------------------------
// Top: the rider's own motorcycle
// ---------------------------------------------------------------------------

@Composable
internal fun HomeWordmark() {
    BrandMark()
}

/** "Boa noite, Marcos": the name is highlighted, and the greeting works without it. */
@Composable
internal fun HomeGreeting(
    userName: String?
) {
    val greeting = remember { greetingForNow() }
    val firstName = remember(userName) { firstNameOf(userName) }

    Text(
        text = buildAnnotatedString {
            withStyle(SpanStyle(color = WarmInk)) {
                append(greeting)
            }

            if (firstName != null) {
                withStyle(SpanStyle(color = WarmInk)) {
                    append(", ")
                }
                withStyle(SpanStyle(color = WarmEmber)) {
                    append(firstName)
                }
            }
        },
        style = MaterialTheme.typography.headlineLarge,
        fontWeight = FontWeight.ExtraBold
    )
}

/** Greeting beside the default motorcycle, followed by its name and specs. */
@Composable
internal fun HomeTopSection(
    subtitle: String,
    userName: String?,
    motorcycle: MotorcycleResponseDTO,
    onGarageClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        HomeWordmark()

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            WarmEmber.copy(alpha = 0.20f),
                            Color.Transparent
                        ),
                        center = Offset(size.width * 0.66f, size.height * 0.72f),
                        radius = size.width * 0.5f
                    ),
                    radius = size.width * 0.5f,
                    center = Offset(size.width * 0.66f, size.height * 0.72f)
                )
            }

            MotorcycleHeroImage(
                type = motorcycle.motorcycleType,
                colorName = motorcycle.color,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .align(Alignment.BottomEnd)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(top = 4.dp)
            ) {
                HomeGreeting(userName = userName)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmInkSoft
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .clickable(
                    onClickLabel = "Abrir garagem",
                    role = Role.Button,
                    onClick = onGarageClick
                )
                .padding(
                    horizontal = 4.dp,
                    vertical = 4.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = motorcycle.brand.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 2.sp,
                    color = WarmEmber,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = motorcycle.model,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = WarmInk,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = WarmMuted
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            motorcycle.motorcycleType?.let { type ->
                SpecPill(text = type.label)
            }

            SpecPill(text = "${motorcycle.engineCapacity} cc")

            SpecPill(
                text = motorcycle.color,
                swatch = motorcycleColor(motorcycle.color)
            )
        }
    }
}

@Composable
private fun SpecPill(
    text: String,
    swatch: Color? = null
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(WarmCard)
            .border(
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(50)
            )
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        swatch?.let { color ->
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, CircleShape)
                    .border(1.dp, WarmHairline, CircleShape)
            )

            Spacer(modifier = Modifier.width(6.dp))
        }

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = WarmInkSoft,
            maxLines = 1
        )
    }
}

// ---------------------------------------------------------------------------
// Road stats: dark card with the year by month and the next milestone
// ---------------------------------------------------------------------------

@Composable
internal fun RoadStatsCard(
    totalDistanceKm: Double,
    distanceThisYearKm: Double,
    completedTrips: Long,
    motorcycleCount: Long,
    monthlyKm: DoubleArray?,
    level: RiderLevel? = null
) {
    val today = remember { LocalDate.now() }
    val worldLaps = totalDistanceKm / EARTH_CIRCUMFERENCE_KM
    val milestone = remember(totalDistanceKm) { milestoneFor(totalDistanceKm) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(GraphiteHeroBrush)
            .padding(22.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SUA ESTRADA",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.6.sp,
                color = WarmEmberOnDark
            )

            Text(
                text = today.year.toString(),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.1f))
                    .padding(
                        horizontal = 10.dp,
                        vertical = 4.dp
                    ),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            verticalAlignment = Alignment.Bottom
        ) {
            Text(
                text = formatNumber(totalDistanceKm),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "km",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        Text(
            text = if (completedTrips == 0L) {
                "Sua quilometragem começa na primeira viagem concluída."
            } else {
                "percorridos em viagens concluídas"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White.copy(alpha = 0.7f)
        )

        formatWorldLaps(worldLaps)?.let { text ->
            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = text,
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(WarmEmber.copy(alpha = 0.2f))
                    .padding(
                        horizontal = 12.dp,
                        vertical = 6.dp
                    ),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = WarmEmberOnDark
            )
        }

        if (monthlyKm != null && monthlyKm.any { it > 0.0 }) {
            Spacer(modifier = Modifier.height(24.dp))

            MonthlyBars(
                monthlyKm = monthlyKm,
                currentMonthIndex = today.monthValue - 1
            )
        }

        level?.let {
            Spacer(modifier = Modifier.height(22.dp))

            LevelRow(level = it)
        }

        Spacer(modifier = Modifier.height(22.dp))

        MilestoneRow(milestone = milestone)

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.12f))
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DarkStat(
                icon = Icons.Outlined.Speed,
                value = "${formatNumber(distanceThisYearKm)} km",
                label = "Este ano",
                modifier = Modifier.weight(1.4f)
            )

            DarkStat(
                icon = Icons.Outlined.Route,
                value = NumberFormat
                    .getIntegerInstance(Locale.forLanguageTag("pt-BR"))
                    .format(completedTrips),
                label = "Viagens",
                modifier = Modifier.weight(1f)
            )

            DarkStat(
                icon = Icons.Outlined.TwoWheeler,
                value = motorcycleCount.toString(),
                label = if (motorcycleCount == 1L) "Moto" else "Motos",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MonthlyBars(
    monthlyKm: DoubleArray,
    currentMonthIndex: Int
) {
    val maxKm = monthlyKm.max().coerceAtLeast(1.0)
    val bestMonth = monthlyKm.indices.maxByOrNull { monthlyKm[it] } ?: 0

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(96.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            monthlyKm.forEachIndexed { index, km ->
                val fraction by animateFloatAsState(
                    targetValue = (km / maxKm).toFloat().coerceAtLeast(0.04f),
                    label = "month-bar"
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(fraction)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            when {
                                index == currentMonthIndex && km > 0 -> WarmEmber
                                index > currentMonthIndex -> Color.White.copy(alpha = 0.06f)
                                km > 0 -> Color.White.copy(alpha = 0.32f)
                                else -> Color.White.copy(alpha = 0.1f)
                            }
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MonthInitials.forEachIndexed { index, initial ->
                Text(
                    text = initial,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = if (index == currentMonthIndex) {
                        FontWeight.ExtraBold
                    } else {
                        FontWeight.Normal
                    },
                    color = if (index == currentMonthIndex) {
                        WarmEmberOnDark
                    } else {
                        Color.White.copy(alpha = 0.5f)
                    },
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Melhor mês: ${MonthNames[bestMonth]} · ${formatNumber(monthlyKm[bestMonth])} km",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun LevelRow(level: RiderLevel) {
    val fraction by animateFloatAsState(targetValue = level.fraction, label = "level")

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Nível ${level.level} · ${level.name}",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = level.nextXp?.let { "${formatNumber(level.xp.toDouble())} / ${formatNumber(it.toDouble())} XP" }
                    ?: "${formatNumber(level.xp.toDouble())} XP",
                style = MaterialTheme.typography.labelMedium,
                color = WarmEmberOnDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(Color.White.copy(alpha = 0.85f))
            )
        }
    }
}

@Composable
private fun MilestoneRow(
    milestone: MilestoneProgress
) {
    val fraction by animateFloatAsState(
        targetValue = milestone.fraction,
        label = "milestone"
    )

    val target = NumberFormat
        .getIntegerInstance(Locale.forLanguageTag("pt-BR"))
        .format(milestone.targetKm)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Próximo marco: $target km",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )

            Text(
                text = "faltam ${formatNumber(milestone.remainingKm)} km",
                style = MaterialTheme.typography.labelMedium,
                color = WarmEmberOnDark
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.White.copy(alpha = 0.12f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction.coerceAtLeast(0.02f))
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(WarmEmber)
            )
        }
    }
}

@Composable
private fun DarkStat(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = Color.White.copy(alpha = 0.55f)
        )

        Spacer(modifier = Modifier.height(8.dp))

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

private fun formatWorldLaps(
    laps: Double
): String? {
    return when {
        laps < 0.01 -> null

        laps < 1.0 ->
            "${(laps * 100).toInt()}% de uma volta ao mundo"

        else -> {
            val formatter = NumberFormat.getNumberInstance(
                Locale.forLanguageTag("pt-BR")
            ).apply {
                minimumFractionDigits = 1
                maximumFractionDigits = 1
            }

            "${formatter.format(laps)} voltas ao mundo"
        }
    }
}
