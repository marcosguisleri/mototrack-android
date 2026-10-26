package br.dev.guisleri.mototrack.data.place

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.PI
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

data class Coordinates(val latitude: Double, val longitude: Double)

/** [approximate] is true when it is a straight line scaled to roads, not a real route. */
data class RouteDistance(val km: Double, val approximate: Boolean)

private const val EARTH_RADIUS_KM = 6_371.0

// Roads are longer than the straight line between two cities, by about a quarter in Brazil.
private const val ROAD_FACTOR = 1.25

/** Great-circle distance between two points, in km. */
fun straightLineKm(from: Coordinates, to: Coordinates): Double {
    fun rad(degrees: Double) = degrees * PI / 180

    val dLat = rad(to.latitude - from.latitude)
    val dLon = rad(to.longitude - from.longitude)
    val a = sin(dLat / 2).pow(2) +
        cos(rad(from.latitude)) * cos(rad(to.latitude)) * sin(dLon / 2).pow(2)

    return 2 * EARTH_RADIUS_KM * asin(sqrt(a))
}

private val STATE_NAMES = mapOf(
    "AC" to "Acre", "AL" to "Alagoas", "AP" to "Amapá", "AM" to "Amazonas",
    "BA" to "Bahia", "CE" to "Ceará", "DF" to "Distrito Federal", "ES" to "Espírito Santo",
    "GO" to "Goiás", "MA" to "Maranhão", "MT" to "Mato Grosso", "MS" to "Mato Grosso do Sul",
    "MG" to "Minas Gerais", "PA" to "Pará", "PB" to "Paraíba", "PR" to "Paraná",
    "PE" to "Pernambuco", "PI" to "Piauí", "RJ" to "Rio de Janeiro", "RN" to "Rio Grande do Norte",
    "RS" to "Rio Grande do Sul", "RO" to "Rondônia", "RR" to "Roraima", "SC" to "Santa Catarina",
    "SP" to "São Paulo", "SE" to "Sergipe", "TO" to "Tocantins"
)

/**
 * How far apart two cities are by road. The cities are placed by their urban centre
 * (so a huge municipality is not measured from the middle of its territory), the
 * road distance comes from a routing service, and with no route the straight line
 * stands in, marked as approximate.
 */
class RouteDistanceService(
    private val geocoding: GeocodingApiService,
    private val ibge: IbgeApiService,
    private val osrm: OsrmApiService
) {

    private val known = ConcurrentHashMap<Long, Coordinates>()

    /** Null when either city cannot be placed (no connection, nothing found). */
    suspend fun distanceBetween(origin: City, destination: City): RouteDistance? = coroutineScope {
        if (origin.id == destination.id) return@coroutineScope null

        val from = async { coordinatesOf(origin) }
        val to = async { coordinatesOf(destination) }

        val start = from.await() ?: return@coroutineScope null
        val end = to.await() ?: return@coroutineScope null

        val road = roadKm(start, end)

        if (road != null) {
            RouteDistance(road.roundedKm(), approximate = false)
        } else {
            RouteDistance((straightLineKm(start, end) * ROAD_FACTOR).roundedKm(), approximate = true)
        }
    }

    private suspend fun roadKm(from: Coordinates, to: Coordinates): Double? =
        try {
            val response = osrm.route("${from.longitude},${from.latitude};${to.longitude},${to.latitude}")

            response.routes.firstOrNull()?.takeIf { response.code == "Ok" }?.distance?.div(1_000)
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }

    private suspend fun coordinatesOf(city: City): Coordinates? {
        known[city.id]?.let { return it }

        val found = urbanCentre(city) ?: municipalityCentre(city)

        found?.let { known[city.id] = it }
        return found
    }

    private suspend fun urbanCentre(city: City): Coordinates? =
        try {
            val state = STATE_NAMES[city.uf]?.let(::normalizePlace)
            val name = normalizePlace(city.name)

            geocoding.search(city.name).results.orEmpty()
                .filter { normalizePlace(it.name) == name && normalizePlace(it.admin1.orEmpty()) == state }
                .maxByOrNull { it.population ?: 0 }
                ?.let { Coordinates(it.latitude, it.longitude) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }

    private suspend fun municipalityCentre(city: City): Coordinates? =
        try {
            ibge.getMunicipalityMetadata(city.id).firstOrNull()?.centroide
                ?.let { Coordinates(it.latitude, it.longitude) }
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            null
        }

    private fun Double.roundedKm(): Double = (this * 10).roundToInt() / 10.0
}

/** What the trip form needs to suggest cities and fill the distance. */
class PlaceServices(
    val cities: CityCatalog,
    val routes: RouteDistanceService
)
