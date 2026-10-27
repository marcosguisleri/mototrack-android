package br.dev.guisleri.mototrack.data.maintenance

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.maintenanceDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "motorcycle_maintenance"
)

/**
 * Maintenance is kept on the device, like the photos and the reminders: the backend
 * has no such concept. Motorcycle ids are global, so another account never reaches
 * these entries.
 */
interface MaintenanceStore {

    fun observe(motorcycleId: Long): Flow<MotorcycleMaintenance>

    /** The maintenance of every motorcycle that has any, by motorcycle id. */
    suspend fun all(): Map<Long, MotorcycleMaintenance>

    suspend fun update(
        motorcycleId: Long,
        change: (MotorcycleMaintenance) -> MotorcycleMaintenance
    )
}

class DataStoreMaintenanceStore internal constructor(
    private val dataStore: DataStore<Preferences>
) : MaintenanceStore {

    constructor(context: Context) : this(context.applicationContext.maintenanceDataStore)

    private val json = Json { ignoreUnknownKeys = true }

    private companion object {
        const val KEY_PREFIX = "maintenance_"
    }

    private fun keyOf(motorcycleId: Long) = stringPreferencesKey("$KEY_PREFIX$motorcycleId")

    override fun observe(motorcycleId: Long): Flow<MotorcycleMaintenance> =
        dataStore.data.map { preferences -> decode(preferences[keyOf(motorcycleId)]) }

    override suspend fun all(): Map<Long, MotorcycleMaintenance> =
        dataStore.data.first().asMap().mapNotNull { (key, value) ->
            val id = key.name.removePrefix(KEY_PREFIX).toLongOrNull()
                ?.takeIf { key.name.startsWith(KEY_PREFIX) }
                ?: return@mapNotNull null

            id to decode(value as? String)
        }.toMap()

    override suspend fun update(
        motorcycleId: Long,
        change: (MotorcycleMaintenance) -> MotorcycleMaintenance
    ) {
        dataStore.edit { preferences ->
            val key = keyOf(motorcycleId)
            val updated = change(decode(preferences[key]))

            preferences[key] = json.encodeToString(MotorcycleMaintenance.serializer(), updated)
        }
    }

    // A value that cannot be read is treated as empty rather than crashing the screen.
    private fun decode(raw: String?): MotorcycleMaintenance =
        raw
            ?.let { runCatching { json.decodeFromString(MotorcycleMaintenance.serializer(), it) }.getOrNull() }
            ?: MotorcycleMaintenance()
}
