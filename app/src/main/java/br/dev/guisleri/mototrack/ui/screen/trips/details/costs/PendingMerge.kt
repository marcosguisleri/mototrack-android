package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import br.dev.guisleri.mototrack.data.model.trip.ExpenseCategoryCostDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelCostDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.offline.PendingRecord
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * The summary the server gave plus what was registered offline and is still to be
 * sent, so the rider sees the real total at once. With nothing pending it is the
 * server's own summary.
 */
fun mergePending(
    remote: TripCostSummaryResponseDTO?,
    pending: List<PendingRecord>,
    distanceKm: Double?
): TripCostSummaryResponseDTO? {
    if (pending.isEmpty()) return remote

    val base = remote ?: TripCostSummaryResponseDTO(FuelCostDTO(entries = 0), emptyList())

    val fuel = pending.mapNotNull { it.fuel }
    val expenses = pending.mapNotNull { it.expense }

    val mergedFuel =
        if (fuel.isEmpty()) {
            base.fuel
        } else {
            FuelCostDTO(
                entries = base.fuel.entries + fuel.size,
                liters = (base.fuel.liters ?: 0.0) + fuel.sumOf { it.liters },
                totalCost = (base.fuel.totalCost ?: 0.0) + fuel.sumOf { it.totalCost }
            )
        }

    val byCategory = base.expenses.associateBy { it.category }.toMutableMap()
    expenses.groupBy { it.category }.forEach { (category, items) ->
        val existing = byCategory[category]

        byCategory[category] = ExpenseCategoryCostDTO(
            category = category,
            count = (existing?.count ?: 0) + items.size,
            total = (existing?.total ?: 0.0) + items.sumOf { it.amount }
        )
    }

    val mergedExpenses = byCategory.values.sortedBy { ExpenseCategory.fromApi(it.category).ordinal }

    val total = (base.totalCost ?: 0.0) + pending.sumOf { it.amount }

    return TripCostSummaryResponseDTO(
        fuel = mergedFuel,
        expenses = mergedExpenses,
        totalCost = total,
        costPerKm = distanceKm?.takeIf { it > 0 }?.let {
            BigDecimal(total / it).setScale(2, RoundingMode.HALF_UP).toDouble()
        }
    )
}
