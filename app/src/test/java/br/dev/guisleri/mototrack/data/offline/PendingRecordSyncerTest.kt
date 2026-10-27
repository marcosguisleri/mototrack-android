package br.dev.guisleri.mototrack.data.offline

import br.dev.guisleri.mototrack.data.model.trip.FuelEntryRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.FuelEntryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripCostSummaryResponseDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseRequestDTO
import br.dev.guisleri.mototrack.data.model.trip.TripExpenseResponseDTO
import br.dev.guisleri.mototrack.data.remote.trip.TripCostApiService
import br.dev.guisleri.mototrack.data.repository.TripCostRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

internal class InMemoryPendingStore(initial: List<PendingRecord> = emptyList()) : PendingRecordStore {
    private val state = MutableStateFlow(initial)

    override fun observe(): Flow<List<PendingRecord>> = state
    override suspend fun all() = state.value
    override suspend fun add(record: PendingRecord) { state.value = state.value + record }
    override suspend fun replace(record: PendingRecord) {
        state.value = state.value.map { if (it.id == record.id) record else it }
    }
    override suspend fun remove(id: String) { state.value = state.value.filterNot { it.id == id } }
    override suspend fun removeForTrip(tripId: Long) {
        state.value = state.value.filterNot { it.tripId == tripId }
    }
    override suspend fun clear() { state.value = emptyList() }
}

class PendingRecordSyncerTest {

    private fun fuel(id: String, trip: Long = 1) = PendingRecord(
        id = id, tripId = trip, createdAtMillis = id.hashCode().toLong(),
        fuel = FuelEntryRequestDTO(10.0, 60.0)
    )

    private fun expense(id: String, trip: Long = 1) = PendingRecord(
        id = id, tripId = trip, createdAtMillis = 0,
        expense = TripExpenseRequestDTO("TOLL", 8.9, null)
    )

    @Test
    fun sendsEverythingInOrderAndEmptiesTheQueue() = runTest {
        val api = RecordingApi()
        val store = InMemoryPendingStore(listOf(fuel("a"), expense("b"), fuel("c", trip = 2)))
        val syncer = PendingRecordSyncer(store, TripCostRepository(api))

        syncer.flush()

        assertEquals(listOf("fuel:1", "expense:1", "fuel:2"), api.sent)
        assertTrue(store.all().isEmpty())
    }

    @Test
    fun stopsAtTheFirstOfflineAndKeepsTheRest() = runTest {
        val api = RecordingApi().apply { failOn = 2; failure = IOException("offline") }
        val store = InMemoryPendingStore(listOf(fuel("a"), fuel("b"), fuel("c")))

        PendingRecordSyncer(store, TripCostRepository(api)).flush()

        assertEquals(1, api.sent.size)
        assertEquals(listOf("b", "c"), store.all().map { it.id })
    }

    @Test
    fun aRecordTheServerRefusesForGoodIsDroppedAndTheNextOneIsSent() = runTest {
        val api = RecordingApi().apply {
            failOn = 1
            failure = HttpException(Response.error<Any>(404, "".toResponseBody()))
        }
        val store = InMemoryPendingStore(listOf(fuel("a"), fuel("b")))
        val syncer = PendingRecordSyncer(store, TripCostRepository(api))
        val events = mutableListOf<PendingSyncEvent>()

        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            syncer.events.collect { events += it }
        }
        syncer.flush()
        collector.cancel()

        assertTrue(store.all().isEmpty())
        assertEquals(listOf(PendingSyncEvent(sent = 1, dropped = 1)), events)
    }

    @Test
    fun aServerErrorOrAnExpiredSessionKeepsTheRecord() = runTest {
        for (code in listOf(500, 503, 401, 429)) {
            val api = RecordingApi().apply {
                failOn = 1
                failure = HttpException(Response.error<Any>(code, "".toResponseBody()))
            }
            val store = InMemoryPendingStore(listOf(fuel("a")))

            PendingRecordSyncer(store, TripCostRepository(api)).flush()

            assertEquals("code $code", listOf("a"), store.all().map { it.id })
        }
    }

    @Test
    fun nothingToSendSaysNothing() = runTest {
        val syncer = PendingRecordSyncer(InMemoryPendingStore(), TripCostRepository(RecordingApi()))
        val events = mutableListOf<PendingSyncEvent>()

        val collector = launch(UnconfinedTestDispatcher(testScheduler)) {
            syncer.events.collect { events += it }
        }
        syncer.flush()
        collector.cancel()

        assertTrue(events.isEmpty())
    }

    @Test
    fun storeFiltersAndReplacesByRecord() = runTest {
        val store = InMemoryPendingStore(listOf(fuel("a"), fuel("b", trip = 2)))

        store.replace(fuel("a").copy(fuel = FuelEntryRequestDTO(5.0, 30.0)))
        store.removeForTrip(2)

        assertEquals(30.0, store.all().single().amount, 0.0)
        assertEquals(listOf("a"), store.observe().first().map { it.id })
    }

    private class RecordingApi : TripCostApiService {
        val sent = mutableListOf<String>()
        var failOn: Int? = null
        var failure: Exception? = null
        private var calls = 0

        private fun attempt(label: String) {
            calls++
            if (failOn == calls) throw failure!!
            sent += label
        }

        override suspend fun addFuelEntry(tripId: Long, request: FuelEntryRequestDTO): FuelEntryResponseDTO {
            attempt("fuel:$tripId")
            return FuelEntryResponseDTO(1, request.liters, request.totalCost, 6.0, "2026-10-27T10:00:00Z")
        }

        override suspend fun addExpense(tripId: Long, request: TripExpenseRequestDTO): TripExpenseResponseDTO {
            attempt("expense:$tripId")
            return TripExpenseResponseDTO(1, request.category, request.amount, null, "2026-10-27T10:00:00Z")
        }

        override suspend fun getCostSummary(tripId: Long): TripCostSummaryResponseDTO = error("unused")
        override suspend fun getFuelEntries(tripId: Long): List<FuelEntryResponseDTO> = error("unused")
        override suspend fun updateFuelEntry(tripId: Long, fuelEntryId: Long, request: FuelEntryRequestDTO): FuelEntryResponseDTO = error("unused")
        override suspend fun deleteFuelEntry(tripId: Long, fuelEntryId: Long) = error("unused")
        override suspend fun getExpenses(tripId: Long): List<TripExpenseResponseDTO> = error("unused")
        override suspend fun updateExpense(tripId: Long, expenseId: Long, request: TripExpenseRequestDTO): TripExpenseResponseDTO = error("unused")
        override suspend fun deleteExpense(tripId: Long, expenseId: Long) = error("unused")
    }
}
