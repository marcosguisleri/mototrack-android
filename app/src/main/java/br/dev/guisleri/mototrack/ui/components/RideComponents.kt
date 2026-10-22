package br.dev.guisleri.mototrack.ui.components

import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.clickable
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmCard
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft
import br.dev.guisleri.mototrack.ui.theme.SunriseHeroBrush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.runtime.getValue
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Route
import androidx.compose.material.icons.outlined.Terrain
import androidx.compose.material.icons.outlined.TwoWheeler
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmSkeleton
import br.dev.guisleri.mototrack.ui.theme.WarmSkeletonHighlight

/** Origin and destination joined by a dashed line. */
@Composable
internal fun RouteTimeline(
    origin: String,
    destination: String,
    textColor: Color,
    accentColor: Color,
    lineColor: Color
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .border(2.dp, accentColor, CircleShape)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = origin,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Box(
            modifier = Modifier
                .width(20.dp)
                .height(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = lineColor,
                    start = Offset(size.width / 2, 0f),
                    end = Offset(size.width / 2, size.height),
                    strokeWidth = size.width,
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(6f, 6f)
                    )
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.width(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = accentColor
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = destination,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = textColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
internal fun FactChip(
    icon: ImageVector,
    text: String,
    containerColor: Color,
    contentColor: Color
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
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = contentColor.copy(alpha = 0.85f)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun TripFacts(
    distance: String,
    terrain: String,
    date: String,
    motorcycleName: String,
    chipColor: Color,
    contentColor: Color
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FactChip(Icons.Outlined.CalendarMonth, date, chipColor, contentColor)
        FactChip(Icons.Outlined.Route, distance, chipColor, contentColor)
        FactChip(Icons.Outlined.Terrain, terrain, chipColor, contentColor)
        FactChip(Icons.Outlined.TwoWheeler, motorcycleName, chipColor, contentColor)
    }
}

@Composable
internal fun PulsingDot(
    color: Color
) {
    val transition = rememberInfiniteTransition(label = "live-dot")

    val scale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 2.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "live-dot-scale"
    )

    val alpha by transition.animateFloat(
        initialValue = 0.55f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "live-dot-alpha"
    )

    Box(
        modifier = Modifier.size(14.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                    this.alpha = alpha
                }
                .background(color, CircleShape)
        )

        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
    }
}

@Composable
internal fun ShimmerBlock(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp
) {
    val transition = rememberInfiniteTransition(label = "skeleton")

    val offset by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton-offset"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        WarmSkeleton,
                        WarmSkeletonHighlight,
                        WarmSkeleton
                    ),
                    start = Offset(offset, 0f),
                    end = Offset(offset + 400f, 200f)
                )
            )
    )
}

/** Soft curvy road drawn behind the hero content. */
@Composable
internal fun RoadDecoration(
    modifier: Modifier = Modifier,
    color: Color
) {
    Canvas(modifier = modifier) {
        val road = Path().apply {
            moveTo(size.width * 0.55f, size.height * 1.05f)
            cubicTo(
                size.width * 1.0f, size.height * 0.78f,
                size.width * 0.25f, size.height * 0.45f,
                size.width * 0.92f, -size.height * 0.05f
            )
        }

        drawPath(
            path = road,
            color = color.copy(alpha = 0.07f),
            style = Stroke(
                width = 34.dp.toPx(),
                cap = StrokeCap.Round
            )
        )

        drawPath(
            path = road,
            color = color.copy(alpha = 0.16f),
            style = Stroke(
                width = 2.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(18f, 16f)
                )
            )
        )
    }
}

@Composable
internal fun HeroPill(
    text: String,
    containerColor: Color,
    contentColor: Color,
    leading: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(containerColor)
            .padding(
                horizontal = 12.dp,
                vertical = 6.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 1.2.sp,
            color = contentColor
        )
    }
}

@Composable
internal fun RideButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        )
    ) {
        Text(
            text = text,
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
}

@Composable
internal fun RideOutlinedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 54.dp),
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.5.dp, color.copy(alpha = 0.55f)),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = color
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = text,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
internal fun SunriseIllustration(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        drawCircle(
            color = WarmEmber.copy(alpha = 0.9f),
            radius = h * 0.26f,
            center = Offset(w * 0.68f, h * 0.42f)
        )

        drawPolygon(
            color = WarmEmber.copy(alpha = 0.28f),
            points = listOf(
                Offset(w * 0.05f, h),
                Offset(w * 0.4f, h * 0.38f),
                Offset(w * 0.78f, h)
            )
        )

        drawPolygon(
            color = WarmEmberDeep.copy(alpha = 0.45f),
            points = listOf(
                Offset(w * 0.45f, h),
                Offset(w * 0.78f, h * 0.55f),
                Offset(w * 1.05f, h)
            )
        )
    }
}

internal fun DrawScope.drawPolygon(
    color: Color,
    points: List<Offset>
) {
    val path = Path().apply {
        moveTo(points.first().x, points.first().y)
        points.drop(1).forEach { point ->
            lineTo(point.x, point.y)
        }
        close()
    }

    drawPath(path = path, color = color)
}

@Composable
internal fun PlanTripHero(
    isFirstTrip: Boolean,
    onPlanTripClick: () -> Unit,
    onRegisterCompletedTripClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(SunriseHeroBrush)
    ) {
        SunriseIllustration(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(width = 150.dp, height = 96.dp)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp)
        ) {
            Spacer(modifier = Modifier.height(64.dp))

            Text(
                text = if (isFirstTrip) {
                    "Vamos viajar?"
                } else {
                    "Planeje sua próxima rota"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = WarmInk
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = if (isFirstTrip) {
                    "Planeje sua primeira viagem ou registre uma que você já fez e comece o seu histórico."
                } else {
                    "Que tal preparar a próxima aventura? Ou registrar uma viagem que você já fez?"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = WarmInkSoft
            )

            Spacer(modifier = Modifier.height(20.dp))

            RideButton(
                text = if (isFirstTrip) {
                    "Planejar minha primeira viagem"
                } else {
                    "Planejar viagem"
                },
                onClick = onPlanTripClick,
                containerColor = WarmInk,
                contentColor = Color.White
            )

            Spacer(modifier = Modifier.height(10.dp))

            RideOutlinedButton(
                text = "Registrar viagem feita",
                icon = Icons.Outlined.TaskAlt,
                onClick = onRegisterCompletedTripClick,
                color = WarmInk
            )
        }
    }
}

@Composable
internal fun NoMotorcycleHero(
    onAddMotorcycleClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(WarmCard)
            .border(
                border = BorderStroke(1.dp, WarmHairline),
                shape = RoundedCornerShape(28.dp)
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GarageIllustration(
            modifier = Modifier.size(width = 200.dp, height = 150.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sua garagem está vazia",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = WarmInk,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Antes de criar sua primeira viagem, cadastre a sua moto.",
            style = MaterialTheme.typography.bodyMedium,
            color = WarmInkSoft,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        RideButton(
            text = "Cadastrar moto",
            onClick = onAddMotorcycleClick,
            containerColor = WarmInk,
            contentColor = Color.White
        )
    }
}

@Composable
internal fun CircleActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(WarmCard)
            .border(BorderStroke(1.dp, WarmHairline), CircleShape)
            .clickable(
                onClickLabel = contentDescription,
                role = Role.Button,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(22.dp),
            tint = WarmInk
        )
    }
}
