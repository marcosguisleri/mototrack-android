package br.dev.guisleri.mototrack.data.offline

import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import retrofit2.HttpException
import java.io.IOException

/** What a round of sending did, for telling the rider. */
data class PendingSyncEvent(
    val sent: Int,
    // Records the server refused for good (the trip is gone, the data is not valid).
    val dropped: Int
)

/**
 * Sends the records that were registered offline. One round at a time, oldest first,
 * stopping at the first sign that there is still no connection so the order is kept.
 */
class PendingRecordSyncer(
    private val store: PendingRecordStore,
    private val repository: TripCostRepository
) {

    private val mutex = Mutex()

    private val _events = MutableSharedFlow<PendingSyncEvent>(extraBufferCapacity = 8)

    val events: SharedFlow<PendingSyncEvent> = _events.asSharedFlow()

    suspend fun flush() {
        var sent = 0
        var dropped = 0

        mutex.withLock {
            for (record in store.all()) {
                try {
                    send(record)
                    store.remove(record.id)
                    sent++

                } catch (exception: CancellationException) {
                    throw exception

                } catch (exception: IOException) {
                    // Still offline: try again later.
                    break

                } catch (exception: HttpException) {
                    if (exception.isFinal()) {
                        store.remove(record.id)
                        dropped++
                    } else {
                        // A server or session problem: it may pass, keep the record.
                        break
                    }

                } catch (exception: Exception) {
                    break
                }
            }
        }

        if (sent > 0 || dropped > 0) {
            _events.tryEmit(PendingSyncEvent(sent = sent, dropped = dropped))
        }
    }

    private suspend fun send(record: PendingRecord) {
        record.fuel?.let { repository.addFuelEntry(record.tripId, it) }
        record.expense?.let { repository.addExpense(record.tripId, it) }
    }

    // The request itself is wrong (not found, not allowed, not valid): sending it again changes nothing.
    private fun HttpException.isFinal(): Boolean =
        code() in 400..499 && code() !in RETRYABLE_CLIENT_CODES

    private companion object {
        // Session expired, timeout and rate limit: not the record's fault.
        val RETRYABLE_CLIENT_CODES = setOf(401, 408, 429)
    }
}

/** What the app needs to keep records registered offline and to send them later. */
class OfflineServices(
    val store: PendingRecordStore,
    val syncer: PendingRecordSyncer
)
