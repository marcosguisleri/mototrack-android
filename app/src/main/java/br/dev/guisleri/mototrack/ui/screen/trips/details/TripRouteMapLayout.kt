package br.dev.guisleri.mototrack.ui.screen.trips.details

import kotlin.random.Random

/** A point of the illustrative map, with both coordinates between 0 and 1. */
internal data class MapPoint(val x: Float, val y: Float)

internal data class MapPark(
    val center: MapPoint,
    val radiusX: Float,
    val radiusY: Float,
    val degrees: Float
)

internal data class RouteMapLayout(
    /** From the origin to the destination, with the bends in between. */
    val route: List<MapPoint>,
    /** The shore, from top to bottom; the sea is on the side [seaOnRight] says. */
    val coast: List<MapPoint>,
    val seaOnRight: Boolean,
    val parks: List<MapPark>,
    val roads: List<List<MapPoint>>,
    val towns: List<MapPoint>
) {
    val origin: MapPoint get() = route.first()
    val destination: MapPoint get() = route.last()
}

/** The same two places always give the same seed, and so the same map. */
internal fun routeSeed(origin: String, destination: String): Long =
    "${origin.trim().lowercase()}|${destination.trim().lowercase()}".hashCode().toLong()

/**
 * An invented but stable map for a trip: a coast, parks, roads and a route that
 * bends its way from the origin to the destination, always over land.
 */
internal fun routeMapLayout(seed: Long): RouteMapLayout {
    val random = Random(seed)
    val mirrored = random.nextBoolean()

    fun Random.between(from: Float, to: Float) = from + nextFloat() * (to - from)

    val origin = MapPoint(random.between(0.13f, 0.22f), random.between(0.68f, 0.78f))
    val destination = MapPoint(random.between(0.55f, 0.67f), random.between(0.32f, 0.42f))

    // Bends: points along the straight line pushed to alternating sides.
    val firstSide = if (random.nextBoolean()) 1f else -1f
    val route = buildList {
        add(origin)

        listOf(0.25f, 0.5f, 0.75f).forEachIndexed { index, t ->
            val side = if (index % 2 == 0) firstSide else -firstSide
            val push = random.between(0.07f, 0.15f) * side

            add(
                MapPoint(
                    x = (origin.x + (destination.x - origin.x) * t + push * 0.6f)
                        .coerceIn(0.08f, 0.7f),
                    y = (origin.y + (destination.y - origin.y) * t + push)
                        .coerceIn(0.12f, 0.88f)
                )
            )
        }

        add(destination)
    }

    val coastBase = random.between(0.8f, 0.84f)
    val coast = listOf(-0.05f, 0.2f, 0.4f, 0.6f, 0.8f, 1.05f).map { y ->
        MapPoint(coastBase + random.between(-0.05f, 0.05f), y)
    }

    val parks = List(3) {
        MapPark(
            center = MapPoint(random.between(0.08f, 0.5f), random.between(0.12f, 0.88f)),
            radiusX = random.between(0.08f, 0.16f),
            radiusY = random.between(0.06f, 0.1f),
            degrees = random.between(-30f, 30f)
        )
    }

    val roads = List(6) {
        val startsAtTop = random.nextBoolean()

        listOf(
            MapPoint(random.between(0f, 0.7f), if (startsAtTop) -0.05f else 1.05f),
            MapPoint(random.between(0.1f, 0.7f), random.between(0.35f, 0.65f)),
            MapPoint(random.between(0.2f, 0.78f), if (startsAtTop) 1.05f else -0.05f)
        )
    }

    val towns = List(7) {
        MapPoint(random.between(0.05f, 0.72f), random.between(0.08f, 0.92f))
    }

    fun MapPoint.flip() = if (mirrored) copy(x = 1f - x) else this

    return RouteMapLayout(
        route = route.map { it.flip() },
        coast = coast.map { it.flip() },
        seaOnRight = !mirrored,
        parks = parks.map { it.copy(center = it.center.flip(), degrees = if (mirrored) -it.degrees else it.degrees) },
        roads = roads.map { road -> road.map { it.flip() } },
        towns = towns.map { it.flip() }
    )
}
