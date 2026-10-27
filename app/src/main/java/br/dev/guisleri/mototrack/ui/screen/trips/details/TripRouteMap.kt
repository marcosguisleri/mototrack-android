package br.dev.guisleri.mototrack.ui.screen.trips.details

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import br.dev.guisleri.mototrack.ui.components.formatDistance
import br.dev.guisleri.mototrack.ui.theme.WarmEmber
import br.dev.guisleri.mototrack.ui.theme.WarmHairline
import br.dev.guisleri.mototrack.ui.theme.WarmInk
import br.dev.guisleri.mototrack.ui.theme.WarmInkSoft

private val MapShape = RoundedCornerShape(28.dp)
private val LandColor = Color(0xFFEFE8DC)
private val ParkColor = Color(0xFFE2E6D0)
private val SeaColor = Color(0xFFD5E4E8)
private val RoadCasing = Color(0xFFE2D9C9)
private val TownColor = Color(0xFFCFC5B4)

// How far along the route the rider is, by status.
private const val IN_PROGRESS_FRACTION = 0.55f

private fun progressOf(status: String): Float =
    when (status) {
        "COMPLETED" -> 1f
        "IN_PROGRESS" -> IN_PROGRESS_FRACTION
        else -> 0f
    }

/**
 * An illustration of the trip's route, in the style of a map: the line is
 * drawn from the origin to the destination, solid for what is done and dashed
 * for what is ahead. It is invented from the place names, not the real road.
 */
@Composable
internal fun TripRouteMap(
    trip: TripResponseDTO,
    modifier: Modifier = Modifier
) {
    val layout = remember(trip.origin, trip.destination) {
        routeMapLayout(routeSeed(trip.origin, trip.destination))
    }

    val reveal = remember { Animatable(0f) }
    val pin = remember { Animatable(0f) }

    // The route draws itself, then the destination pin drops in.
    LaunchedEffect(layout, trip.status) {
        reveal.snapTo(0f)
        pin.snapTo(0f)

        reveal.animateTo(1f, tween(durationMillis = 1500, easing = FastOutSlowInEasing))
        pin.animateTo(
            1f,
            spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)
        )
    }

    val pulse by rememberInfiniteTransition(label = "map-pulse").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1600, easing = LinearEasing), RepeatMode.Restart),
        label = "map-pulse-value"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
            .clip(MapShape)
            .border(BorderStroke(1.dp, WarmHairline), MapShape)
            .semantics {
                contentDescription =
                    "Mapa ilustrativo do trajeto de ${trip.origin} até ${trip.destination}"
            }
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val density = LocalDensity.current
            val width = with(density) { maxWidth.toPx() }
            val height = with(density) { maxHeight.toPx() }

            val scene = remember(layout, width, height) { MapScene(layout, width, height) }
            val status = progressOf(trip.status)

            Canvas(modifier = Modifier.fillMaxSize()) {
                drawMap(
                    scene = scene,
                    status = trip.status,
                    statusProgress = status,
                    reveal = reveal.value,
                    pin = pin.value,
                    pulse = pulse
                )
            }

            val pinHeight = with(density) { PIN_RADIUS.dp.toPx() * 2.4f }
            val midpoint = scene.positionAt(0.5f)

            MapLabels(
                width = width,
                height = height,
                origin = scene.origin,
                originAbove = scene.origin.y > height * 0.82f,
                destination = scene.destination.copy(y = scene.destination.y - pinHeight),
                midpoint = midpoint,
                originText = trip.origin,
                destinationText = trip.destination,
                distanceText = formatDistance(trip.distanceKm),
                pinProgress = pin.value
            )
        }

        IllustrativeBadge(
            modifier = Modifier
                // On the sea side, away from the origin label.
                .align(if (layout.seaOnRight) Alignment.BottomEnd else Alignment.BottomStart)
                .padding(12.dp)
        )
    }
}

// ---------------------------------------------------------------------------
// Geometry in pixels
// ---------------------------------------------------------------------------

private const val PIN_RADIUS = 11f

/** The layout scaled to the card, with the paths the canvas draws every frame. */
private class MapScene(
    layout: RouteMapLayout,
    private val width: Float,
    private val height: Float
) {
    private fun MapPoint.px() = Offset(x * width, y * height)

    val routePoints = layout.route.map { it.px() }
    val origin: Offset = routePoints.first()
    val destination: Offset = routePoints.last()

    val route: Path = smoothPath(routePoints)
    private val measure = PathMeasure().apply { setPath(route, false) }
    val length: Float = measure.length

    val coast: Path = smoothPath(layout.coast.map { it.px() })

    val sea: Path = Path().apply {
        addPath(coast)

        val edge = if (layout.seaOnRight) width else 0f
        lineTo(edge, height * 1.05f)
        lineTo(edge, -height * 0.05f)
        close()
    }

    val seaDirection = if (layout.seaOnRight) 1f else -1f

    val roads: List<Path> = layout.roads.map { road -> smoothPath(road.map { it.px() }) }

    val parks = layout.parks
    val towns = layout.towns.map { it.px() }
    val sizePx = Size(width, height)

    fun positionAt(fraction: Float): Offset =
        measure.getPosition(length * fraction.coerceIn(0f, 1f))

    fun segment(from: Float, to: Float): Path? {
        val start = length * from.coerceIn(0f, 1f)
        val end = length * to.coerceIn(0f, 1f)

        if (end - start < 1f) return null

        return Path().also { measure.getSegment(start, end, it, true) }
    }

    fun parkCenter(park: MapPark) = park.center.px()
}

/** A smooth curve through [points] (Catmull-Rom turned into Bézier segments). */
private fun smoothPath(points: List<Offset>): Path {
    val path = Path()

    if (points.isEmpty()) return path

    path.moveTo(points[0].x, points[0].y)

    for (i in 0 until points.size - 1) {
        val before = points[(i - 1).coerceAtLeast(0)]
        val from = points[i]
        val to = points[i + 1]
        val after = points[(i + 2).coerceAtMost(points.size - 1)]

        path.cubicTo(
            from.x + (to.x - before.x) / 6f,
            from.y + (to.y - before.y) / 6f,
            to.x - (after.x - from.x) / 6f,
            to.y - (after.y - from.y) / 6f,
            to.x,
            to.y
        )
    }

    return path
}

// ---------------------------------------------------------------------------
// Drawing
// ---------------------------------------------------------------------------

private fun DrawScope.drawMap(
    scene: MapScene,
    status: String,
    statusProgress: Float,
    reveal: Float,
    pin: Float,
    pulse: Float
) {
    drawRect(LandColor)

    scene.parks.forEach { park ->
        val center = scene.parkCenter(park)
        val radiusX = park.radiusX * scene.sizePx.width
        val radiusY = park.radiusY * scene.sizePx.height

        rotate(park.degrees, pivot = center) {
            drawOval(
                color = ParkColor,
                topLeft = Offset(center.x - radiusX, center.y - radiusY),
                size = Size(radiusX * 2f, radiusY * 2f)
            )
        }
    }

    // Sea, with two faint swells parallel to the shore, and the shore itself.
    drawPath(scene.sea, SeaColor)

    repeat(2) { index ->
        translate(left = (14f + index * 16f).dp.toPx() * scene.seaDirection) {
            drawPath(
                path = scene.coast,
                color = Color.White.copy(alpha = 0.32f - index * 0.1f),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }
    }

    drawPath(
        path = scene.coast,
        color = Color.White.copy(alpha = 0.85f),
        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
    )

    // Roads: the first two are main roads, the rest minor ones.
    scene.roads.forEachIndexed { index, road ->
        val main = index < 2

        drawPath(
            path = road,
            color = RoadCasing,
            style = Stroke(width = (if (main) 6f else 3.5f).dp.toPx(), cap = StrokeCap.Round)
        )

        drawPath(
            path = road,
            color = Color.White.copy(alpha = if (main) 0.95f else 0.8f),
            style = Stroke(width = (if (main) 4f else 2f).dp.toPx(), cap = StrokeCap.Round)
        )
    }

    scene.towns.forEach { town ->
        drawCircle(TownColor, radius = 3.dp.toPx(), center = town)
    }

    drawRoute(scene, status, statusProgress, reveal)
    drawOrigin(scene.origin)

    if (status == "IN_PROGRESS" && reveal >= statusProgress) {
        drawRider(scene.positionAt(statusProgress), pulse)
    }

    drawDestination(scene.destination, pin)
}

private fun DrawScope.drawRoute(
    scene: MapScene,
    status: String,
    statusProgress: Float,
    reveal: Float
) {
    val width = 5.dp.toPx()
    val dashed = Stroke(
        width = width,
        cap = StrokeCap.Round,
        join = StrokeJoin.Round,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 11.dp.toPx()))
    )

    // A white edge under the whole line keeps it readable over roads and parks.
    scene.segment(0f, reveal)?.let { whole ->
        drawPath(
            whole,
            Color.White,
            style = Stroke(width = width + 5.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    scene.segment(0f, minOf(reveal, statusProgress))?.let { done ->
        drawPath(
            done,
            WarmEmber,
            style = Stroke(width = width, cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }

    // Ahead of the rider, or the whole trip while it is only planned.
    scene.segment(statusProgress, reveal)?.let { ahead ->
        drawPath(ahead, WarmEmber.copy(alpha = if (status == "PLANNED") 0.9f else 0.65f), style = dashed)
    }
}

private fun DrawScope.drawOrigin(at: Offset) {
    drawCircle(WarmEmber.copy(alpha = 0.18f), radius = 17.dp.toPx(), center = at)
    drawCircle(Color.White, radius = 9.dp.toPx(), center = at)
    drawCircle(WarmEmber, radius = 7.dp.toPx(), center = at, style = Stroke(width = 4.dp.toPx()))
}

private fun DrawScope.drawRider(at: Offset, pulse: Float) {
    drawCircle(
        color = WarmEmber.copy(alpha = (1f - pulse) * 0.35f),
        radius = (10f + pulse * 18f).dp.toPx(),
        center = at
    )
    drawCircle(Color.White, radius = 9.dp.toPx(), center = at)
    drawCircle(WarmEmber, radius = 6.dp.toPx(), center = at)
}

private fun DrawScope.drawDestination(tip: Offset, progress: Float) {
    if (progress <= 0f) return

    val radius = PIN_RADIUS.dp.toPx()
    val center = Offset(tip.x, tip.y - radius * 2.1f)

    drawOval(
        color = Color.Black.copy(alpha = 0.18f * progress.coerceAtMost(1f)),
        topLeft = Offset(tip.x - radius * 0.9f, tip.y - radius * 0.25f),
        size = Size(radius * 1.8f, radius * 0.5f)
    )

    // The pin drops in: it grows from its tip, overshooting a little.
    scale(scaleX = progress, scaleY = progress, pivot = tip) {
        val body = Path().apply {
            moveTo(tip.x, tip.y)
            cubicTo(
                center.x - radius * 1.1f, center.y + radius * 1.0f,
                center.x - radius, center.y + radius * 0.4f,
                center.x - radius, center.y
            )
            arcTo(
                Rect(center.x - radius, center.y - radius, center.x + radius, center.y + radius),
                180f, 180f, false
            )
            cubicTo(
                center.x + radius, center.y + radius * 0.4f,
                center.x + radius * 1.1f, center.y + radius * 1.0f,
                tip.x, tip.y
            )
            close()
        }

        drawPath(body, WarmInk)
        drawCircle(Color.White, radius = radius * 0.42f, center = center)
    }
}

// ---------------------------------------------------------------------------
// Labels
// ---------------------------------------------------------------------------

@Composable
private fun MapLabels(
    width: Float,
    height: Float,
    origin: Offset,
    originAbove: Boolean,
    destination: Offset,
    midpoint: Offset,
    originText: String,
    destinationText: String,
    distanceText: String,
    pinProgress: Float
) {
    val density = LocalDensity.current
    val margin = with(density) { 10.dp.toPx() }
    val gap = with(density) { 14.dp.toPx() }

    Layout(
        modifier = Modifier.fillMaxSize(),
        content = {
            MapChip(text = originText)

            Box(modifier = Modifier.graphicsLayer { alpha = pinProgress.coerceIn(0f, 1f) }) {
                MapChip(text = destinationText)
            }

            Box(modifier = Modifier.graphicsLayer { alpha = pinProgress.coerceIn(0f, 1f) }) {
                MapChip(text = distanceText, emphasized = true)
            }
        }
    ) { measurables, constraints ->
        val placeables = measurables.map { it.measure(constraints.copy(minWidth = 0, minHeight = 0)) }

        layout(constraints.maxWidth, constraints.maxHeight) {
            fun place(index: Int, centerX: Float, top: Float) {
                val placeable = placeables[index]

                placeable.place(
                    x = (centerX - placeable.width / 2f)
                        .coerceIn(margin, (width - placeable.width - margin).coerceAtLeast(margin))
                        .toInt(),
                    y = top.coerceIn(margin, (height - placeable.height - margin).coerceAtLeast(margin)).toInt()
                )
            }

            place(
                0,
                origin.x,
                if (originAbove) origin.y - gap - placeables[0].height else origin.y + gap
            )
            place(1, destination.x, destination.y - gap - placeables[1].height)
            place(2, midpoint.x, midpoint.y - placeables[2].height / 2f)
        }
    }
}

@Composable
private fun MapChip(
    text: String,
    emphasized: Boolean = false
) {
    val shape = RoundedCornerShape(50)

    Box(
        modifier = Modifier
            .shadow(if (emphasized) 4.dp else 3.dp, shape, ambientColor = Color(0x33000000))
            .clip(shape)
            .background(if (emphasized) WarmEmber else Color.White)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.widthIn(max = 132.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.ExtraBold,
            color = if (emphasized) Color.White else WarmInk,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun IllustrativeBadge(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(Color.White.copy(alpha = 0.9f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = WarmInkSoft
        )

        Spacer(modifier = Modifier.width(5.dp))

        Text(
            text = "Trajeto ilustrativo",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = WarmInkSoft
        )
    }
}
