package br.dev.guisleri.mototrack.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.R
import br.dev.guisleri.mototrack.ui.theme.WarmCanvas
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmEmberDeep
import br.dev.guisleri.mototrack.ui.theme.WarmEmberOnDark
import br.dev.guisleri.mototrack.ui.theme.WarmEmberTint
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmSuccess
import kotlinx.coroutines.delay

private val GarageEdge = Color(0xFFE6DCCD)
private val GarageSlat = Color(0xFFF9D9BF)
private val GarageDark = Color(0xFF2E2723)
private val GarageDarker = Color(0xFF181310)

/**
 * A garage with its door rolling up to show the bike inside, under the
 * MotoTrack sign. The door opens once when it appears and the light inside
 * breathes slowly.
 */
@Composable
internal fun GarageIllustration(
    modifier: Modifier = Modifier
) {
    val open = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(350)
        open.animateTo(0.78f, tween(durationMillis = 1200, easing = FastOutSlowInEasing))
    }

    val glow by rememberInfiniteTransition(label = "garage-glow").animateFloat(
        initialValue = 0.28f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            RepeatMode.Reverse
        ),
        label = "garage-glow-value"
    )

    BoxWithConstraints(modifier = modifier) {
        val height = maxHeight
        val width = maxWidth

        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(WarmEmber.copy(alpha = 0.16f), Color.Transparent),
                    center = Offset(w * 0.5f, h * 0.66f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.5f, h * 0.66f)
            )

            drawOval(
                color = WarmInk.copy(alpha = 0.08f),
                topLeft = Offset(w * 0.06f, h * 0.9f),
                size = Size(w * 0.88f, h * 0.09f)
            )

            // A shrub on each side keeps the building from looking stuck to the ground.
            listOf(0.07f to 0.9f, 0.115f to 0.93f, 0.93f to 0.9f, 0.885f to 0.93f).forEach { (x, y) ->
                drawCircle(WarmSuccess.copy(alpha = 0.5f), radius = w * 0.045f, center = Offset(w * x, h * y))
            }

            val corner = CornerRadius(8.dp.toPx())

            drawRoundRect(
                color = WarmCanvas,
                topLeft = Offset(w * 0.12f, h * 0.38f),
                size = Size(w * 0.76f, h * 0.56f),
                cornerRadius = corner
            )
            drawRoundRect(
                color = GarageEdge,
                topLeft = Offset(w * 0.12f, h * 0.38f),
                size = Size(w * 0.76f, h * 0.56f),
                cornerRadius = corner,
                style = Stroke(width = 1.5.dp.toPx())
            )

            val roof = Path().apply {
                moveTo(w * 0.05f, h * 0.42f)
                lineTo(w * 0.5f, h * 0.07f)
                lineTo(w * 0.95f, h * 0.42f)
                close()
            }
            drawPath(roof, WarmEmber)
            drawPath(roof, WarmEmber, style = Stroke(width = 6.dp.toPx(), join = StrokeJoin.Round))
            drawLine(
                color = WarmEmberDeep,
                start = Offset(w * 0.05f, h * 0.42f),
                end = Offset(w * 0.95f, h * 0.42f),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )

            // The opening: dark inside, with the light the bike stands in.
            val doorLeft = w * 0.24f
            val doorTop = h * 0.52f
            val doorWidth = w * 0.52f
            val doorHeight = h * 0.42f

            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(GarageDark, GarageDarker),
                    startY = doorTop,
                    endY = doorTop + doorHeight
                ),
                topLeft = Offset(doorLeft, doorTop),
                size = Size(doorWidth, doorHeight),
                cornerRadius = CornerRadius(6.dp.toPx())
            )

            clipRect(doorLeft, doorTop, doorLeft + doorWidth, doorTop + doorHeight) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(WarmEmber.copy(alpha = glow), Color.Transparent),
                        center = Offset(w * 0.5f, doorTop + doorHeight),
                        radius = w * 0.3f
                    ),
                    radius = w * 0.3f,
                    center = Offset(w * 0.5f, doorTop + doorHeight)
                )
            }

            // The rolling door: slats from the top down to where it has got to.
            val coveredBottom = doorTop + doorHeight * (1f - open.value)
            val slatCount = 7
            val slatHeight = doorHeight / slatCount

            clipRect(doorLeft, doorTop, doorLeft + doorWidth, coveredBottom) {
                repeat(slatCount) { index ->
                    drawRect(
                        color = GarageSlat,
                        topLeft = Offset(doorLeft, doorTop + index * slatHeight),
                        size = Size(doorWidth, slatHeight - 1.5.dp.toPx())
                    )
                }
            }

            if (open.value < 0.98f) {
                drawRoundRect(
                    color = WarmEmber,
                    topLeft = Offset(doorLeft, coveredBottom - 4.dp.toPx()),
                    size = Size(doorWidth, 4.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx())
                )
            }

            drawRoundRect(
                color = GarageEdge,
                topLeft = Offset(doorLeft, doorTop),
                size = Size(doorWidth, doorHeight),
                cornerRadius = CornerRadius(6.dp.toPx()),
                style = Stroke(width = 2.dp.toPx())
            )
        }

        // The sign on the gable.
        Image(
            painter = painterResource(R.drawable.ic_mototrack_symbol),
            contentDescription = null,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = height * 0.2f)
                .clip(RoundedCornerShape(8.dp))
                .background(WarmCanvas)
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .size(width = 34.dp, height = 21.dp)
        )

        // The bike appears in the light as the door goes up.
        Icon(
            imageVector = Icons.Filled.TwoWheeler,
            contentDescription = null,
            tint = WarmEmberOnDark,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = height * 0.1f)
                .size(width * 0.22f)
                .graphicsLayer { alpha = ((open.value - 0.4f) / 0.38f).coerceIn(0f, 1f) }
        )
    }
}

/**
 * A road winding towards the horizon, with a pin that drops in at the end of
 * it: the first trip is out there.
 */
@Composable
internal fun FirstTripIllustration(
    modifier: Modifier = Modifier
) {
    val pin = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        delay(300)
        pin.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)
        )
    }

    Canvas(modifier = modifier.clip(RoundedCornerShape(24.dp))) {
        val w = size.width
        val h = size.height
        val horizon = h * 0.62f

        // A small framed scene: sunrise sky over the land the road crosses.
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFE3CC), Color(0xFFFFF4E8)),
                startY = 0f,
                endY = horizon
            ),
            size = Size(w, horizon)
        )

        drawCircle(
            color = WarmEmber.copy(alpha = 0.9f),
            radius = h * 0.17f,
            center = Offset(w * 0.8f, h * 0.3f)
        )

        drawPath(
            Path().apply {
                moveTo(w * 0.02f, horizon)
                lineTo(w * 0.3f, h * 0.3f)
                lineTo(w * 0.58f, horizon)
                close()
            },
            WarmEmber.copy(alpha = 0.26f)
        )
        drawPath(
            Path().apply {
                moveTo(w * 0.4f, horizon)
                lineTo(w * 0.7f, h * 0.44f)
                lineTo(w * 1f, horizon)
                close()
            },
            WarmEmberDeep.copy(alpha = 0.4f)
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFD6B8), WarmEmberTint),
                startY = horizon,
                endY = h
            ),
            topLeft = Offset(0f, horizon),
            size = Size(w, h - horizon)
        )

        val vanishing = Offset(w * 0.6f, horizon)

        val road = Path().apply {
            moveTo(w * 0.14f, h)
            cubicTo(w * 0.3f, h * 0.86f, w * 0.72f, h * 0.8f, vanishing.x - w * 0.012f, horizon)
            lineTo(vanishing.x + w * 0.012f, horizon)
            cubicTo(w * 0.8f, h * 0.82f, w * 0.5f, h * 0.88f, w * 0.48f, h)
            close()
        }
        drawPath(road, WarmInk.copy(alpha = 0.92f))

        val centerLine = Path().apply {
            moveTo(w * 0.31f, h)
            cubicTo(w * 0.4f, h * 0.86f, w * 0.74f, h * 0.8f, vanishing.x, horizon)
        }
        drawPath(
            centerLine,
            Color.White.copy(alpha = 0.85f),
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 9.dp.toPx()))
            )
        )

        // The pin stands on the horizon where the road ends.
        val radius = h * 0.075f
        val tip = vanishing
        val center = Offset(tip.x, tip.y - radius * 2.1f)

        scale(scale = pin.value, pivot = tip) {
            val body = Path().apply {
                moveTo(tip.x, tip.y)
                cubicTo(
                    center.x - radius * 1.1f, center.y + radius,
                    center.x - radius, center.y + radius * 0.4f,
                    center.x - radius, center.y
                )
                arcTo(
                    androidx.compose.ui.geometry.Rect(
                        center.x - radius, center.y - radius, center.x + radius, center.y + radius
                    ),
                    180f, 180f, false
                )
                cubicTo(
                    center.x + radius, center.y + radius * 0.4f,
                    center.x + radius * 1.1f, center.y + radius,
                    tip.x, tip.y
                )
                close()
            }

            drawPath(body, WarmEmber)
            drawCircle(Color.White, radius = radius * 0.42f, center = center)
        }
    }
}
