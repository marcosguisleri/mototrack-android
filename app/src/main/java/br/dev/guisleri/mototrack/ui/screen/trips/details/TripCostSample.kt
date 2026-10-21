package br.dev.guisleri.mototrack.ui.screen.trips.details

import br.dev.guisleri.mototrack.data.model.trip.TripResponseDTO
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

/**
 * TEMPORARY sample numbers.
 *
 * The backend has no fuel or toll data yet, so the trip details screen shows
 * values derived from the distance and the motorcycle. Replace this file (and
 * the call in TripDetailsScreen) once FuelEntry and TollEntry exist.
 */
internal const val SHOW_SAMPLE_COSTS_NOTICE = true

private const val FUEL_PRICE_PER_LITER = 6.19
private const val TOLL_PRICE = 8.9
private const val KM_PER_TOLL = 85.0

internal data class TripCostSample(
    val fuelLiters: Double,
    val fuelCost: Double,
    val tollCount: Int,
    val tollCost: Double,
    val distanceKm: Double
) {
    val total: Double get() = fuelCost + tollCost

    val costPerKm: Double
        get() = if (distanceKm > 0) total / distanceKm else 0.0
}

internal fun sampleCostFor(trip: TripResponseDTO): TripCostSample {
    val kmPerLiter = when {
        trip.motorcycle.engineCapacity <= 300 -> 35.0
        trip.motorcycle.engineCapacity <= 600 -> 27.0
        trip.motorcycle.engineCapacity <= 1000 -> 21.0
        else -> 17.0
    }

    val liters = trip.distanceKm / kmPerLiter

    val tollCount = when (trip.terrain) {
        "OFF_ROAD" -> 0
        "MIXED" -> (trip.distanceKm / (KM_PER_TOLL * 2)).roundToInt()
        else -> (trip.distanceKm / KM_PER_TOLL).roundToInt()
    }.coerceIn(0, 12)

    return TripCostSample(
        fuelLiters = liters,
        fuelCost = liters * FUEL_PRICE_PER_LITER,
        tollCount = tollCount,
        tollCost = tollCount * TOLL_PRICE,
        distanceKm = trip.distanceKm
    )
}

internal fun formatMoney(value: Double): String =
    NumberFormat
        .getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
        .format(value)
        .replace(' ', ' ')
