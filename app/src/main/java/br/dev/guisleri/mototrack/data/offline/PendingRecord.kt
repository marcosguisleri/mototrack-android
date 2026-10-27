package br.dev.guisleri.mototrack.data.offline

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import kotlinx.serialization.Serializable

/**
 * A fuel stop or an expense the rider registered without a connection, kept on the
 * device until it can be sent. Exactly one of [fuel] and [expense] is set.
 */
@Serializable
data class PendingRecord(
    val id: String,
    val tripId: Long,
    val createdAtMillis: Long,
    val fuel: FuelEntryRequestDTO? = null,
    val expense: TripExpenseRequestDTO? = null
) {
    val amount: Double get() = fuel?.totalCost ?: expense?.amount ?: 0.0
}
