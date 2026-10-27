package br.dev.guisleri.mototrack.data.offline

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

private val Context.pendingDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "pending_records"
)

/** The records waiting to be sent, oldest first. They survive closing the app. */
interface PendingRecordStore {

    fun observe(): Flow<List<PendingRecord>>

    suspend fun all(): List<PendingRecord>

    suspend fun add(record: PendingRecord)

    /** Replaces the record with the same id, keeping its place in the queue. */
    suspend fun replace(record: PendingRecord)

    suspend fun remove(id: String)

    suspend fun removeForTrip(tripId: Long)

    suspend fun clear()
}

class DataStorePendingRecordStore internal constructor(
    private val dataStore: DataStore<Preferences>
) : PendingRecordStore {

    constructor(context: Context) : this(context.applicationContext.pendingDataStore)

    private val key = stringPreferencesKey("records")
    private val json = Json { ignoreUnknownKeys = true }
    private val serializer = ListSerializer(PendingRecord.serializer())

    override fun observe(): Flow<List<PendingRecord>> =
        dataStore.data.map { decode(it[key]) }

    override suspend fun all(): List<PendingRecord> = observe().first()

    override suspend fun add(record: PendingRecord) = change { it + record }

    override suspend fun replace(record: PendingRecord) =
        change { list -> list.map { if (it.id == record.id) record else it } }

    override suspend fun remove(id: String) = change { list -> list.filterNot { it.id == id } }

    override suspend fun removeForTrip(tripId: Long) =
        change { list -> list.filterNot { it.tripId == tripId } }

    override suspend fun clear() = change { emptyList() }

    private suspend fun change(transform: (List<PendingRecord>) -> List<PendingRecord>) {
        dataStore.edit { preferences ->
            preferences[key] = json.encodeToString(serializer, transform(decode(preferences[key])))
        }
    }

    // A value that cannot be read is treated as an empty queue rather than a crash.
    private fun decode(raw: String?): List<PendingRecord> =
        raw?.let { runCatching { json.decodeFromString(serializer, it) }.getOrNull() }.orEmpty()
}
