package br.dev.guisleri.mototrack.ui.screen.trips.details.costs

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.offline.PendingRecord
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import java.time.Instant

/** A fuel stop or an expense, as one line of the trip's log. */
sealed interface TripRecord {
    val id: Long
    val createdAt: Instant
    val amount: Double

    data class Fuel(
        val entry: FuelEntryResponseDTO,
        override val createdAt: Instant
    ) : TripRecord {
        override val id: Long get() = entry.id
        override val amount: Double get() = entry.totalCost
    }

    data class Expense(
        val expense: TripExpenseResponseDTO,
        override val createdAt: Instant
    ) : TripRecord {
        override val id: Long get() = expense.id
        override val amount: Double get() = expense.amount
        val category: ExpenseCategory get() = ExpenseCategory.fromApi(expense.category)
    }

    /** Registered offline and still waiting to be sent. */
    data class Pending(
        val record: PendingRecord
    ) : TripRecord {
        override val id: Long get() = record.id.hashCode().toLong()
        override val createdAt: Instant get() = Instant.ofEpochMilli(record.createdAtMillis)
        override val amount: Double get() = record.amount
        val isFuel: Boolean get() = record.fuel != null
        val category: ExpenseCategory?
            get() = record.expense?.let { ExpenseCategory.fromApi(it.category) }
    }
}

/** Fuel stops and expenses together, the latest first. */
fun tripRecords(
    fuelEntries: List<FuelEntryResponseDTO>,
    expenses: List<TripExpenseResponseDTO>,
    pending: List<PendingRecord> = emptyList()
): List<TripRecord> =
    (
        fuelEntries.map { TripRecord.Fuel(it, parseInstantOrEpoch(it.createdAt)) } +
            expenses.map { TripRecord.Expense(it, parseInstantOrEpoch(it.createdAt)) } +
            pending.map { TripRecord.Pending(it) }
        )
        .sortedWith(
            compareByDescending<TripRecord> { it.createdAt }
                .thenByDescending { it is TripRecord.Fuel }
                .thenByDescending { it.id }
        )

internal fun parseInstantOrEpoch(value: String): Instant =
    runCatching { Instant.parse(value) }.getOrDefault(Instant.EPOCH)
