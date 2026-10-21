package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.EaseInCubic
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.EaseOutCubic
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlin.random.Random

private const val START_DURATION_MS = 2200
private const val COMPLETE_DURATION_MS = 2800

private val Scrim = Color(0xF2181310)
private val ConfettiColors = listOf(
    WarmEmber,
    WarmEmberOnDark,
    Color(0xFF7BD39A),
    Color.White,
    Color(0xFFFFCBA1)
)

/** Part of [progress] between [from] and [to], as 0..1 eased by [easing]. */
private fun segment(
    progress: Float,
    from: Float,
    to: Float,
    easing: Easing = LinearEasing
): Float = easing.transform(((progress - from) / (to - from)).coerceIn(0f, 1f))

/**
 * Full-screen moment that marks a trip being started or finished. It plays once,
 * can be skipped with a tap and calls [onFinished] when it is over.
 */
@Composable
internal fun TripCelebrationOverlay(
    celebration: TripCelebration,
    trip: TripResponseDTO,
    onFinished: () -> Unit
) {
    val progress = remember(celebration) { Animatable(0f) }
    val haptics = LocalHapticFeedback.current

    LaunchedEffect(celebration) {
        haptics.performHapticFeedback(HapticFeedbackType.Confirm)

        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = when (celebration) {
                    TripCelebration.STARTED -> START_DURATION_MS
                    TripCelebration.COMPLETED -> COMPLETE_DURATION_MS
                },
                easing = LinearEasing
            )
        )

        onFinished()
    }

    val p = progress.value
    val envelope = minOf(segment(p, 0f, 0.08f), 1f - segment(p, 0.92f, 1f))

    Box(
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer { alpha = envelope }
            .background(Scrim)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
    ) {
        when (celebration) {
            TripCelebration.STARTED -> StartScene(p, trip)
            TripCelebration.COMPLETED -> CompleteScene(p, trip)
        }
    }
}

// ---------------------------------------------------------------------------
// Start: rings pulse while the bike rides through the screen
// ---------------------------------------------------------------------------

@Composable
private fun StartScene(
    p: Float,
    trip: TripResponseDTO
) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val iconSize = 104.dp

        // Enters from the left, stays a beat in the middle, leaves to the right.
        val enter = segment(p, 0.02f, 0.32f, EaseOutCubic)
        val exit = segment(p, 0.7f, 0.98f, EaseInCubic)
        val translationX = (enter - 1f) * widthPx + exit * widthPx

        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f - 40.dp.toPx())

            repeat(3) { index ->
                val ring = segment(p, 0.18f + index * 0.12f, 0.8f, FastOutSlowInEasing)

                if (ring > 0f && ring < 1f) {
                    drawCircle(
                        color = WarmEmber.copy(alpha = (1f - ring) * 0.7f),
                        radius = 60.dp.toPx() + ring * 150.dp.toPx(),
                        center = center,
                        style = Stroke(width = 3.dp.toPx())
                    )
                }
            }

            // Speed lines trail the bike while it moves.
            val speed = enter.let { 1f - it } + exit
            if (speed > 0.02f) {
                repeat(3) { index ->
                    val y = center.y + (index - 1) * 22.dp.toPx()
                    val x = center.x + translationX
                    drawLine(
                        color = WarmEmberOnDark.copy(alpha = 0.55f * speed.coerceAtMost(1f)),
                        start = Offset(x - 70.dp.toPx() - index * 20.dp.toPx(), y),
                        end = Offset(x - 40.dp.toPx(), y),
                        strokeWidth = 3.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }
        }

        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .graphicsLayer { this.translationX = translationX }
                    .size(iconSize)
                    .clip(CircleShape)
                    .background(WarmEmber),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.TwoWheeler,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(56.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            CelebrationText(
                title = "Boa viagem!",
                subtitle = "${trip.origin} → ${trip.destination}",
                visibility = segment(p, 0.28f, 0.42f) * (1f - segment(p, 0.72f, 0.9f))
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Complete: a check draws itself and confetti bursts
// ---------------------------------------------------------------------------

private class ConfettiPiece(
    val angle: Float,
    val speed: Float,
    val size: Float,
    val spin: Float,
    val color: Color
)

@Composable
private fun CompleteScene(
    p: Float,
    trip: TripResponseDTO
) {
    val confetti = remember {
        val random = Random(7)

        List(44) {
            ConfettiPiece(
                // Mostly upwards and sideways, so it falls back down over the card.
                angle = (-Math.PI * (0.05 + random.nextDouble() * 0.9)).toFloat(),
                speed = 260f + random.nextFloat() * 520f,
                size = 6f + random.nextFloat() * 8f,
                spin = (random.nextFloat() - 0.5f) * 720f,
                color = ConfettiColors[random.nextInt(ConfettiColors.size)]
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val origin = Offset(size.width / 2f, size.height / 2f - 40.dp.toPx())

            drawCheck(origin, p)
            drawConfetti(origin, p, confetti)
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(top = 190.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CelebrationText(
                title = "Viagem concluída!",
                subtitle = "${formatDistance(trip.distanceKm)} · ${trip.origin} → ${trip.destination}",
                visibility = segment(p, 0.3f, 0.45f)
            )
        }
    }
}

private fun DrawScope.drawCheck(
    center: Offset,
    p: Float
) {
    val radius = 62.dp.toPx() * segment(p, 0.02f, 0.24f, EaseOutBack)

    if (radius <= 0f) return

    drawCircle(color = WarmSuccess, radius = radius, center = center)

    // The check is drawn along its two strokes, as far as the animation has got.
    val start = center + Offset(-0.42f, 0.02f) * radius
    val bottom = center + Offset(-0.12f, 0.32f) * radius
    val end = center + Offset(0.44f, -0.28f) * radius

    val first = hypot(bottom.x - start.x, bottom.y - start.y)
    val second = hypot(end.x - bottom.x, end.y - bottom.y)
    val drawn = segment(p, 0.16f, 0.36f, FastOutSlowInEasing) * (first + second)

    val path = Path().apply {
        moveTo(start.x, start.y)

        if (drawn <= first) {
            val t = drawn / first
            lineTo(start.x + (bottom.x - start.x) * t, start.y + (bottom.y - start.y) * t)
        } else {
            lineTo(bottom.x, bottom.y)
            val t = (drawn - first) / second
            lineTo(bottom.x + (end.x - bottom.x) * t, bottom.y + (end.y - bottom.y) * t)
        }
    }

    drawPath(
        path = path,
        color = Color.White,
        style = Stroke(
            width = 9.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun DrawScope.drawConfetti(
    origin: Offset,
    p: Float,
    pieces: List<ConfettiPiece>
) {
    val time = segment(p, 0.2f, 1f) * 1.9f
    val fade = 1f - segment(p, 0.65f, 0.95f)

    if (time <= 0f || fade <= 0f) return

    val gravity = 900.dp.toPx()

    pieces.forEach { piece ->
        val speed = piece.speed.dp.toPx()
        val x = origin.x + cos(piece.angle) * speed * time
        val y = origin.y + sin(piece.angle) * speed * time + 0.5f * gravity * time * time
        val side = piece.size.dp.toPx()

        rotate(degrees = piece.spin * time, pivot = Offset(x, y)) {
            drawRect(
                color = piece.color.copy(alpha = fade),
                topLeft = Offset(x - side / 2f, y - side / 2f),
                size = Size(side, side * 0.6f)
            )
        }
    }
}

@Composable
private fun CelebrationText(
    title: String,
    subtitle: String,
    visibility: Float
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 32.dp)
            .graphicsLayer {
                alpha = visibility
                translationY = (1f - visibility) * 24.dp.toPx()
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )
    }
}
