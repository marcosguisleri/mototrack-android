package br.dev.guisleri.mototrack.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.motorcycleDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "motorcycle_preferences"
)

/**
 * Device-level choices about the user's motorcycles.
 *
 * The default motorcycle is a UI preference kept on the device (the backend has
 * no such concept). Motorcycle ids are global, so an id that does not belong to
 * the signed-in user is simply never found in their list and gets ignored.
 */
interface MotorcyclePreferences {

    val defaultMotorcycleId: Flow<Long?>

    suspend fun setDefaultMotorcycleId(motorcycleId: Long)
}

class DataStoreMotorcyclePreferences internal constructor(
    private val dataStore: DataStore<Preferences>
) : MotorcyclePreferences {

    constructor(context: Context) : this(context.applicationContext.motorcycleDataStore)

    private companion object {
        val DEFAULT_MOTORCYCLE_ID = longPreferencesKey("default_motorcycle_id")
    }

    override val defaultMotorcycleId: Flow<Long?> =
        dataStore.data.map { preferences ->
            preferences[DEFAULT_MOTORCYCLE_ID]
        }

    override suspend fun setDefaultMotorcycleId(motorcycleId: Long) {
        dataStore.edit { preferences ->
            preferences[DEFAULT_MOTORCYCLE_ID] = motorcycleId
        }
    }
}
