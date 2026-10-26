package br.dev.guisleri.mototrack.data.place

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.io.File

/**
 * The municipalities of Brazil: downloaded from IBGE once and kept in a file, so
 * from then on the suggestions need no network.
 */
class CityCatalog(
    private val api: IbgeApiService,
    private val cacheFile: File,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val mutex = Mutex()
    private var index: CityIndex? = null

    private val serializer = ListSerializer(City.serializer())

    /** The cities, from memory, the file or IBGE, in that order. Throws if none is available. */
    suspend fun load(): CityIndex = mutex.withLock {
        index?.let { return it }

        val cities = readCache() ?: download()

        CityIndex(cities).also { index = it }
    }

    private suspend fun readCache(): List<City>? = withContext(ioDispatcher) {
        runCatching {
            if (!cacheFile.exists()) return@runCatching null

            Json.decodeFromString(serializer, cacheFile.readText()).takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    private suspend fun download(): List<City> {
        val cities = api.getMunicipalities().mapNotNull { municipality ->
            municipality.uf?.let { City(municipality.id, municipality.nome, it) }
        }

        withContext(ioDispatcher) {
            runCatching {
                val temporary = File(cacheFile.parentFile, "${cacheFile.name}.tmp")
                cacheFile.parentFile?.mkdirs()
                temporary.writeText(Json.encodeToString(serializer, cities))
                temporary.renameTo(cacheFile)
            }
        }

        return cities
    }
}
